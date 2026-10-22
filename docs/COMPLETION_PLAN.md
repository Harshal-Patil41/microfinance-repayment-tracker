# Completion plan (work in this order; stop and ask for help at the first failure)

| Step | Time box | Do | Done when |
|---|---|---|---|
| 1 | 20 min | Unzip, run `mvn clean test` in the new `tracker` folder, then `mvn spring-boot:run` | BUILD SUCCESS; http://localhost:8081/ shows the dashboard with ₹55,500.00 disbursed and 8 overdue |
| 2 | 25 min | Selenium: `docs/RUNBOOK.md` section 3 | 13 tests, 0 failures; screenshot |
| 3 | 45 min | Git Tasks 5 and 6: `docs/GIT_STEPS.md` (branches, PRs, conflict, tag v1.0.0) | PR and merge screenshots, tag on GitHub |
| 4 | 30 min | Docker: RUNBOOK section 4 (start Docker Desktop first) | `(healthy)` and the app on port 8082 |
| 5 | 40 min | Jenkins (only if time): RUNBOOK section 5, then a green build with archived `tracker.war`. Otherwise skip and rehearse explaining the Jenkinsfile | Stage view screenshot |
| 6 | 20 min | Ansible in WSL: `--syntax-check`, two runs if Docker is available there | `changed=0` on run 2, or just the syntax-check |
| 7 | 20 min | Update status in your task reports, put screenshots in `screenshots/`, rehearse `docs/VIVA.md` once aloud | You can present 7 minutes without reading |

If time runs out, the minimum credible set for the viva is steps 1, 2, 3 and 7, plus one honest sentence for what was not run.
