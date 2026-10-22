# Microfinance Repayment Tracker

A Spring Boot web application that tracks microfinance loans and repayments,
built as a DevOps course project (Git, Jenkins, Selenium, Docker, Ansible).

## Tech stack
Java 17, Spring Boot, Maven, Thymeleaf, H2, Tomcat 11

## Run locally
    mvn clean package
    mvn spring-boot:run

App: http://localhost:8081/  |  Health: http://localhost:8081/actuator/health

## Branching
- `main`: stable, tagged releases
- `develop`: integration branch
- `feature/<short-name>`: one branch per piece of work, merged into `develop` by pull request

## Commit messages
Use `feat:`, `fix:`, `test:`, `docs:` or `chore:` followed by a short description.

## Project documents
See the `docs/` folder.

## Author
Harshal Patil, CMPN-A, Vidyalankar Institute of Technology