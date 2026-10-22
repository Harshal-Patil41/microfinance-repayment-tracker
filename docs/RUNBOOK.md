# Runbook: run, test, containerise, provision

Ports: **8081** local app · **8082** Docker container · **8083** Selenium test instance · **8080** Jenkins (reserved).
Commands are for Windows Command Prompt / PowerShell unless marked (WSL).

## 1. Build and unit tests
```
mvn clean test
```
Expect `BUILD SUCCESS` (25 tests: borrower form, schedule generation, payment rules, dashboard figures, alerts, page smoke tests).

## 2. Run the application
```
mvn package -DskipTests
java -jar target\tracker.war                    (recommended)
mvn spring-boot:run                              (alternative while developing)
```
Open http://localhost:8081/ (redirects to `/dashboard`) and http://localhost:8081/actuator/health (`{"status":"UP"}`).
Dummy data is loaded at start-up (11 loans, 42 installments). If 8081 is busy: `set PORT=9091` first.

## 3. Selenium tests (needs Chrome)
Window 1 (isolated instance, port 8083, in-memory data):
```
java -jar target\tracker.war --spring.profiles.active=selenium
```
Window 2:
```
mvn -f selenium-tests\pom.xml test
```
Expect 13 tests in 6 classes (J1 add borrower, J2 add loan, J3 search, J4 overdue list, J5 dashboard totals and payment, J6 alerts), **0 failures**.
Add `-Dheadless=false` to watch the browser. Reports: `selenium-tests\target\surefire-reports\*.xml`. Failure screenshots: `selenium-tests\target\screenshots\`.
Re-running against the same instance is fine; restart Window 1 for a clean state.

## 4. Docker (Docker Desktop must be running)
```
docker build -t microfinance-tracker:1.0.0 .
docker compose up -d
docker ps                                         STATUS shows (healthy) after about 40 s
docker logs -f microfinance-tracker
```
Open http://localhost:8082/. Persistence demo: pay an installment, run `docker compose down`, then `docker compose up -d`: the payment is still there.
`docker compose down -v` also deletes the data volume. Cleanup: `docker rmi microfinance-tracker:1.0.0`.

## 5. Jenkins (Windows service, port 8080)
Install JDK 17, Jenkins LTS, Git, Maven, Docker Desktop, Chrome. Plugins: Pipeline, Git, GitHub, JUnit, Credentials Binding.
New Item > Pipeline > "Pipeline script from SCM" > Git > your repo URL > branch `*/develop` > script path `Jenkinsfile`.
Build with parameters (`DEPLOY_ENV`, `DEPLOY`, `PUSH_IMAGE`). A webhook needs a public Jenkins URL; the Jenkinsfile polls every 5 minutes instead.
Docker Hub push (optional): credential id `dockerhub-creds` (username + access token); tick `PUSH_IMAGE`.
*Not verified by the author: the Jenkinsfile was syntax-balanced but never run on a Jenkins server.*

## 6. Ansible (WSL2 Ubuntu or any Linux; Docker must be available on the target)
```
pip install ansible
ansible-galaxy collection install -r ansible/requirements.yml
docker build -t microfinance-tracker:1.0.0 .       (on the target host)
cd ansible
ansible-playbook playbook.yml --syntax-check
ansible-playbook playbook.yml --ask-become-pass    first run: several "changed"
ansible-playbook playbook.yml --ask-become-pass    second run: changed=0
curl http://localhost:8082/actuator/health
ansible-playbook playbook.yml -e app_version=1.0.1 --ask-become-pass   new release (image 1.0.1 must exist)
ansible-playbook playbook.yml -e app_version=1.0.0 --ask-become-pass   ROLLBACK to the previous version
```
`--syntax-check` and a two-run idempotency test of the user/directory/template tasks were executed by the author in a sandbox (second run `changed=0`). The Docker tasks were not.

## 7. Evidence to capture (put files in `screenshots/`)
Dashboard · borrower form with error · loan schedule · payment success and rejection · overdue list · alerts · `mvn clean test` BUILD SUCCESS ·
Selenium result · `docker ps` healthy · Jenkins stage view and console · Ansible first and second run · rollback run.
