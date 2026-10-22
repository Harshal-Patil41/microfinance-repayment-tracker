# Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Port 8081 was already in use` | another app (or an old run) is using it | close the old window, or `set PORT=9091` then run again |
| `dir pom.xml` says File Not Found | zip unpacked into a folder inside a folder | `cd` into the inner folder |
| `mvn` not found / wrong Java | Path or JAVA_HOME not set | open a NEW terminal; check `java -version` shows 17 |
| Whitelabel/500 page | read the stack trace; `mvn test` names the failing page | send the first `Caused by` line |
| Selenium: SessionNotCreated / driver | Chrome missing or no internet on first run | install Chrome; set `CHROME_BIN` if non-standard |
| Selenium: connection refused | selenium instance not running | start `java -jar target\tracker.war --spring.profiles.active=selenium` and check http://localhost:8083/actuator/health |
| Selenium J6 expects 6 late alerts but finds other numbers | seed data was changed or the instance has old state | restart the selenium instance |
| Docker: `error during connect` | Docker Desktop not running | start Docker Desktop and wait for "Engine running" |
| Container `unhealthy` | still starting, or crashed | `docker logs microfinance-tracker`; allow about 40 s |
| Data lost after restart | used `docker compose down -v` | omit `-v` |
| Jenkins `docker not found` | agent has no Docker / service account lacks access | install Docker Desktop; run the Jenkins service as your user |
| Ansible `community.docker` missing | collection not installed | `ansible-galaxy collection install -r ansible/requirements.yml` |
| Ansible "Image ... not found" | image not on the target | `docker build -t microfinance-tracker:1.0.0 .` on the target |
| Git: push rejected / PR required | branch ruleset on `develop`/`main` | push a feature branch and open a pull request |
| Git: "untracked working tree files would be overwritten" | same files exist untracked and in the branch you pull | `git switch` away first (tracked files are removed), then pull |
