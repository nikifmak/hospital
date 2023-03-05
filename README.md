# Hospital Management System

## How to run 
```shell
./mvnw clean spring-boot:run
```

## How to test
```shell
./mvnw clean test
```

## H2 console
On dev mode connect to h2 console at
JDBC URL: jdbc:h2:mem:hospital
```shell
open http://localhost:8080/api/h2-console
```

## Assigment
```shell
Problem Definition

A busy hospital has a list of dates that a doctor is available to see patients. Their process is manual and error prone leading to overbooking. They also have a hard time visualizing all of the available time for a doctor for specific dates.

Assignment Task

Create a REST API that enables a simple scheduling system that manages doctor availabilities and allows patients to book appointments.

In order to keep project scope as small as possible, you will implement a subset of the API as described in section “Rest API”.

Data Model

Define a set of data models that include:

✅a way to track and book appointments
✅a way to track patients
✅a way to track doctors
✅a way to track a doctor's working hours and days
 

REST API

Implement the following two (2) functionalities, plus one (1) optional (please do not implement anything more or less):

✅Find a doctor's working hours
✅Book a doctor appointment
Optionally, create and update the list of doctor's working hours
 
Non-Functional Requirements

Tech stack: Java 11, Spring Boot, Maven or Gradle
Use JPA 2.x and Spring Data for persistence
All API calls are unsecured, you don’t need to implement security
Write comments within the source code (inline) that will help describe the design at code level (comments in persistence model, API operations, etc.). You don’t have to create Swagger/OpenAPI file that describes the API, this is not important.
Use whatever database you prefer (i.e., in-memory H2)
 
Deliverables

Upload code to a GitHub public project and send the link
Add unit tests for API operations that cover happy and unhappy paths.
 
Should you have questions on the assignment please let us know.
```