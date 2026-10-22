# How the application matches your Task 1-4 reports

| Report item | Where in the application | Status |
|---|---|---|
| FR1 Add borrower (name, 10-digit phone, village) | `BorrowerController`, `Borrower`, `/borrowers/new` | matches |
| FR2/FR3 Loan with amount, start date, installments, WEEKLY/MONTHLY; schedule generated | `LoanService.create` | matches (BR2 last installment absorbs rounding, BR3 due dates) |
| FR4 Pay against an installment, > 0 and <= still due | `RepaymentService.pay`, `POST /repayments/{id}/pay` | matches |
| FR5 Status PAID / PENDING / OVERDUE (BR4-BR6) | `RepaymentService.statusOf`, refreshed on every read | matches |
| FR6/FR7 Dashboard loan table; search by name and village | `/dashboard?q=`, id `search-box` | matches |
| FR8 Four indicators (BR8, BR9) | `DashboardService`; ids `total-disbursed`, `total-collected`, `overdue-count`, `collection-rate` | matches |
| FR9 Click Paid / Pending / Overdue | `/repayments?status=`; ids `paid-link`, `pending-link`, `overdue-link` | matches |
| FR10 Alert: overdue by MORE than 7 days (BR7) | `AlertService`, `/alerts` | matches |
| FR11 Invalid payments and missing schedule | `AlertService` | matches (the seed data deliberately contains both) |
| FR12 Validation with clear messages | bean validation + service rules | matches |
| FR13 Dummy data at start-up | `DataSeeder` | **differs in mechanism**: Java seeder instead of `data.sql`, so dates are relative to today and the overdue demo works on any day |
| FR14 Health endpoint | `/actuator/health` | matches |
| Route list (Task 3 section 6) | all routes exist | matches; extras: `/borrowers` (list, needed by US1 "appears in the list"), `/about`, `GET /loans` redirects to the dashboard |
| Package `com.mfi.tracker`, `tracker.war`, H2, Thymeleaf, Actuator | same | matches |
| Ports 8081 dev / 8082 container / 8080 Jenkins | same; Selenium instance uses 8083 | matches (+8083) |
| "Spring Boot 4.x, Tomcat 11" | **Spring Boot 3.3.5** (Tomcat 10.1 embedded) | **differs**: 3.3.5 was proven to build on your machine. If you deploy the WAR to an *external* Tomcat, use **Tomcat 10.1** (Tomcat 11 is for Spring Boot 4). Running `java -jar tracker.war`, Docker and Ansible are unaffected. |
| "Docker image from a Tomcat 11 base image" | JRE base image running the executable WAR | **differs**; same result, simpler |
| Selenium journeys J1-J5 (+ alerts) | `selenium-tests` | matches |

Say this in the viva if asked: "The report was written before implementation; I kept the functional design exactly and changed only the Spring Boot version and the seeding mechanism, for the reasons above."
