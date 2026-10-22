# Demo script (about 7 minutes) and viva questions

## Before the demo
Start the app in one window (`java -jar target\tracker.war`, port 8081) and keep these tabs open: http://localhost:8081/dashboard, your GitHub repo, Jenkins (if installed), the Jenkinsfile and `ansible/playbook.yml` in VS Code, and a terminal in the project folder.

| Time | What you do | What you say | What the audience should see |
|---|---|---|---|
| 0:00 | Open the dashboard | "Loan officers cannot quickly see who has repaid or is late. This app shows it, and the whole delivery is automated." | Total disbursed ₹55,500.00, collected ₹16,650.00, overdue 8, collection rate 55.5% |
| 0:45 | Type `rav` in the search box, then `Rampur` | "Search matches name or village, partial and case-insensitive." | Only Ravi Kumar's loan; then Asha Patil's loans |
| 1:30 | Add borrower: leave the name empty, phone `123` | "Validation is on the server." | Red messages "Name is required", "Phone must be exactly 10 digits" |
| 2:00 | Create loan: Asha Patil, 1200, 4 installments, weekly | "The schedule is generated automatically; the last installment absorbs rounding." | 4 rows of ₹300.00, all PENDING |
| 2:40 | Pay 100 on installment 1, then 999999 | "Payments must be above 0 and not above what is still due." | Success message; then red "exceeds the amount still due" |
| 3:20 | Click the Overdue card | "Two clicks or fewer to the overdue list." | 8 OVERDUE rows (Ravi, Meena, Suresh, Farah) |
| 4:00 | Open Alerts | "Late means more than 7 days; invalid and incomplete data are flagged too." | 6 late, 2 invalid payments (Farah), 1 missing schedule (Lakshmi) |
| 4:40 | Run `mvn clean test`, then show the Selenium result | "25 unit and integration tests, 13 browser tests in 6 journeys." | BUILD SUCCESS, 0 failures |
| 5:30 | `docker compose up -d`, `docker ps`; open port 8082 | "Same package in a non-root container, with a health check and a data volume." | `(healthy)` |
| 6:00 | Show the Jenkinsfile stages and the Ansible playbook; run `--syntax-check` | "Jenkins gates the deploy on tests; Ansible is idempotent: the second run reports changed=0; rollback is the same playbook with an older version." | Explanation. Show real logs only for what you actually ran |

**Be honest about scope.** If Jenkins or Ansible were not run end to end, say: "I configured and syntax-checked them; I ran X and Y." Examiners respect that far more than a claim they can disprove with one question.

## Viva questions with short answers
1. **What problem does the project solve?** Repayments are tracked on paper or spreadsheets, so late payments are noticed late and totals are hand-calculated. The app shows status and totals instantly.
2. **Why Spring Boot?** It auto-configures web, JPA, validation and an embedded Tomcat, so `java -jar` runs the app and tests are simple.
3. **Why did you use Spring Boot 3.3.5 when the report says 4.x?** 3.3.5 builds and runs on Java 17 on my machine; the functional design is unchanged. For an external Tomcat I would use 10.1.
4. **Explain the layers.** Controller handles URLs and forms, service holds business rules, repository talks to the database, templates render HTML.
5. **How is the repayment schedule generated?** Installment = amount / n rounded to 2 decimals; the last installment takes the rounding difference; due dates are start + 7 days per installment (weekly) or + 1 month per installment (monthly).
6. **How are statuses decided?** PAID if paid >= due; OVERDUE if not paid and the due date is before today; otherwise PENDING. They are refreshed whenever a page reads data.
7. **What is an alert?** An OVERDUE installment more than 7 days past due; plus invalid payments (negative or above the amount due) and loans with fewer schedule rows than installments.
8. **How do you prevent overpayment?** The service checks amount > 0 and amount <= amount still due; the controller shows the message.
9. **Why H2?** Zero setup, in-memory for tests; a file mode in Docker keeps data in a volume. It is not for heavy production use.
10. **Why a Maven wrapper / Maven?** Standard lifecycle (clean, test, package) that Jenkins runs unchanged.
11. **Explain your Git workflow.** `main` holds releases, `develop` integrates, one `feature/*` branch per user story, merged by pull request with a checklist; releases are tagged `v1.0.0`.
12. **What is a merge conflict? How did you resolve it?** Two branches changed the same line; I edited the file to the right content, `git add`, committed the merge.
13. **CI vs CD?** CI builds and tests every change; CD automatically delivers a passing build (here, into a container).
14. **What does the Jenkinsfile do?** Checkout, validate, unit tests, package and archive the WAR, Selenium tests, Docker build, optional push, deploy, health check; failure prints logs and tries to roll back.
15. **How do failing tests stop deployment?** Stages run in order; a failed stage fails the build and later stages (Docker, Deploy) never run.
16. **How are Selenium tests kept stable?** Element ids and `data-testid`, explicit waits, seed data with known values, relative assertions, a separate in-memory instance on port 8083, screenshots on failure.
17. **Why a Docker image and what makes it safe?** The same package runs everywhere; multi-stage build, non-root user, health check, one published port, data in a named volume.
18. **What is Ansible and what is idempotency?** Agentless configuration management with YAML playbooks. Idempotent means repeating the playbook changes nothing the second time (`changed=0`).
19. **What are handlers?** Tasks that run only when notified by a changed task, for example recreating the container only when the compose file changed.
20. **How do you roll back?** Re-run the playbook with the previous `app_version`; Jenkins also redeploys the previous tag if the health check fails. Data persists in the volume.
21. **How are secrets handled?** None are in the repo. Docker Hub login would use a Jenkins credential; Ansible secrets would use ansible-vault.
22. **What are the limitations and next steps?** No login, H2, single branch, no SMS. Next: roles and login, MySQL, reminders, report export, monitoring.
