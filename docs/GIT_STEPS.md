# Git steps for YOUR existing repository (Tasks 5 and 6)

Your repo already exists (Task 4): `https://github.com/Harshal-Patil41/microfinance-repayment-tracker`, default branch `develop`, with a ruleset.
Because of the ruleset, **every change goes through a branch and a pull request (PR)**. Do not push to `develop` or `main` directly.
Actions marked 🔑 need your GitHub login (browser). Nothing here has been run for you.

## 0. One-time preparation
```
cd <your local clone of the repo>
git status                       (must be clean)
git switch develop
git pull
```
Unzip the new project somewhere else, e.g. `C:\work\v2` (it creates `C:\work\v2\tracker`). Call that folder SRC below.
**Do NOT copy** `README.md` or `.gitignore` (you keep yours). Everything else is copied, in step 1.

## 1. Task 5: first feature branch (US1: add borrower)
```
git switch -c feature/borrower-entry
git rm -r src pom.xml
robocopy C:\work\v2\tracker . /E /XF README.md .gitignore /XD .git target
```
(robocopy prints a summary and returns a non-zero code on success; that is normal.)
Now stage ONLY this feature's files (the other files stay untracked on purpose):

```
git add pom.xml
git add src/main/java/com/mfi/tracker/TrackerApplication.java
git add src/main/java/com/mfi/tracker/ServletInitializer.java
git add src/main/java/com/mfi/tracker/config/AppConfig.java
git add src/main/java/com/mfi/tracker/exception
git add src/main/java/com/mfi/tracker/controller/WebAdvice.java
git add src/main/java/com/mfi/tracker/controller/BorrowerController.java
git add src/main/java/com/mfi/tracker/model/Borrower.java
git add src/main/java/com/mfi/tracker/repository/BorrowerRepository.java
git add src/main/java/com/mfi/tracker/service/BorrowerService.java
git add src/main/resources/application.properties
git add src/main/resources/application-prod.properties
git add src/main/resources/application-selenium.properties
git add src/main/resources/static
git add src/main/resources/templates/error.html
git add src/main/resources/templates/fragments
git add src/main/resources/templates/borrowers.html
git add src/main/resources/templates/borrower-form.html
git add src/test/java/com/mfi/tracker/BorrowerWebTest.java
```
Check, build, commit, push:
```
git status                      (only the files above should be green/staged)
mvn clean test                  (builds the WHOLE folder, staged or not; expect BUILD SUCCESS)
git commit -m "feat: add borrower entry with validation (US1)"
git push -u origin feature/borrower-entry
```
🔑 On GitHub: **Compare & pull request**, base = `develop`, title = the commit message, description = tick the PR checklist from `docs/BRANCHING.md`
and write `Closes #1` (check that issue #1 is really US1). Take a screenshot of the PR, then **Merge pull request** (squash or merge commit), screenshot the merged state, delete the branch.
Back in the terminal:
```
git switch develop
git pull
```
(If git complains that untracked files would be overwritten, run `git switch develop` first; tracked files of the old branch disappear, then pull.)

Note: Maven compiles every file in the folder, including the not-yet-staged ones, so a green build proves the finished app works; it does not separately prove that each partial branch compiles alone. That is fine for the demo.

## 2. Task 6: remaining features, each the same way
For each block: create the branch from an up-to-date `develop`, stage the listed files, run `mvn clean test`, commit, push, PR, merge, `git switch develop`, `git pull`.

### 2.1 `feature/loan-schedule`
```
git switch -c feature/loan-schedule
git add src/main/java/com/mfi/tracker/model/Loan.java
git add src/main/java/com/mfi/tracker/model/Repayment.java
git add src/main/java/com/mfi/tracker/model/Frequency.java
git add src/main/java/com/mfi/tracker/model/RepaymentStatus.java
git add src/main/java/com/mfi/tracker/repository/LoanRepository.java
git add src/main/java/com/mfi/tracker/repository/RepaymentRepository.java
git add src/main/java/com/mfi/tracker/dto/LoanForm.java
git add src/main/java/com/mfi/tracker/dto/LoanRow.java
git add src/main/java/com/mfi/tracker/service/LoanService.java
git add src/main/java/com/mfi/tracker/service/RepaymentService.java
git add src/main/java/com/mfi/tracker/controller/LoanController.java
git add src/main/resources/templates/loan-form.html
git add src/main/resources/templates/loan-detail.html
git add src/test/java/com/mfi/tracker/ScheduleGenerationTest.java
mvn clean test
git commit -m "feat: create loan and generate repayment schedule (US2)"
git push -u origin feature/loan-schedule
```
🔑 PR into `develop` (`Closes #…` for the matching issue; US6/US7 have no issue yet, create one or skip the line), merge, then `git switch develop` and `git pull`.

### 2.2 `feature/record-payment`
```
git switch -c feature/record-payment
git add src/main/java/com/mfi/tracker/controller/RepaymentController.java
git add src/main/resources/templates/repayments.html
mvn clean test
git commit -m "feat: record payments and list repayments by status (US3, US6)"
git push -u origin feature/record-payment
```
🔑 PR into `develop` (`Closes #…` for the matching issue; US6/US7 have no issue yet, create one or skip the line), merge, then `git switch develop` and `git pull`.

### 2.3 `feature/dashboard-indicators`
```
git switch -c feature/dashboard-indicators
git add src/main/java/com/mfi/tracker/dto/Indicators.java
git add src/main/java/com/mfi/tracker/service/DashboardService.java
git add src/main/java/com/mfi/tracker/controller/DashboardController.java
git add src/main/java/com/mfi/tracker/config/DataSeeder.java
git add src/main/resources/templates/dashboard.html
git add src/main/resources/templates/about.html
git add src/test/java/com/mfi/tracker/DashboardAndStatusTest.java
git add src/test/java/com/mfi/tracker/PaymentRulesTest.java
mvn clean test
git commit -m "feat: searchable dashboard, indicators and seed data (US4, US5)"
git push -u origin feature/dashboard-indicators
```
🔑 PR into `develop` (`Closes #…` for the matching issue; US6/US7 have no issue yet, create one or skip the line), merge, then `git switch develop` and `git pull`.

### 2.4 `feature/alerts`
```
git switch -c feature/alerts
git add src/main/java/com/mfi/tracker/dto/AlertRow.java
git add src/main/java/com/mfi/tracker/dto/AlertReport.java
git add src/main/java/com/mfi/tracker/dto/MissingSchedule.java
git add src/main/java/com/mfi/tracker/service/AlertService.java
git add src/main/java/com/mfi/tracker/controller/AlertController.java
git add src/main/resources/templates/alerts.html
git add src/test/java/com/mfi/tracker/AlertRulesTest.java
git add src/test/java/com/mfi/tracker/SmokeTest.java
mvn clean test
git commit -m "feat: alert and exception view (US7)"
git push -u origin feature/alerts
```
🔑 PR into `develop` (`Closes #…` for the matching issue; US6/US7 have no issue yet, create one or skip the line), merge, then `git switch develop` and `git pull`.

**After 2.4 the application is complete** (dashboard, indicators, seed data). Run it once from `develop`: `mvn spring-boot:run` and open http://localhost:8081/.

### 2.5 Deliberate merge conflict and resolution (Task 6 evidence)
```
git switch develop
git switch -c feature/conflict-a
```
Open `src/main/resources/templates/about.html`, change `<dd>1.0.0</dd>` to `<dd>1.0.0-a</dd>`, save.
```
git commit -am "chore: show build label A on about page"
git switch develop
git switch -c feature/conflict-b
```
Open the same file, change `<dd>1.0.0</dd>` to `<dd>1.0.0-b</dd>`, save.
```
git commit -am "chore: show build label B on about page"
git switch feature/conflict-a
git merge feature/conflict-b
```
Git reports **CONFLICT**. Screenshot `git status` and the `<<<<<<<` markers in the file. Edit the file so it contains one final line, for example `<dd>1.0.0</dd>`, then:
```
git add src/main/resources/templates/about.html
git commit -m "merge: resolve about page version label conflict"
git push -u origin feature/conflict-a
```
🔑 PR `feature/conflict-a` into `develop`, merge. (Delete `feature/conflict-b` without merging.)

### 2.6 DevOps files branch (Selenium, Docker, Jenkinsfile, Ansible, docs)

```
git switch develop
git pull
git switch -c chore/devops-files
git add selenium-tests
git add Dockerfile
git add compose.yaml
git add .dockerignore
git add Jenkinsfile
git add ansible
git add docs
git commit -m "chore: add Selenium tests, Dockerfile, Jenkinsfile, Ansible and docs"
git push -u origin chore/devops-files
```
🔑 PR into `develop`, merge, then `git switch develop` and `git pull`.

### 2.7 Release and tag v1.0.0
🔑 On GitHub open a PR **`develop` into `main`**, title `release: v1.0.0`, merge it. Then:
```
git switch main
git pull
git tag -a v1.0.0 -m "MVP release"
git push origin v1.0.0
```
🔑 Optional: GitHub > Releases > Draft a new release > choose tag `v1.0.0`. Also update your README run section: `mvn spring-boot:run` on port 8081, `java -jar target/tracker.war`.
(If the ruleset blocks tag creation from the command line, create the tag by publishing a Release on GitHub.)

## 3. Evidence checklist (Tasks 5 and 6)
PR page with checklist · merged PR · `git log --oneline --graph --all` · conflict markers · resolution commit · tag page · updated backlog (move US1..US7 to Done on your board).
