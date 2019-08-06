# Portal

## Build project:

Use following commands to manage project build:

* `mvn clean package -Denv=dev` to make development build
* `mvn clean package -Denv=uat` to make a build for UAT environment (with special application.properties file)
* `mvn clean package -Denv=prod` to make a build for production environment (with special application.properties file)

Note that there is the only `dev` folder with environment settings is present in the code repository (`src/main/resources/env/dev`),
so to use any other environments for the different purposes, first need to create folder with configuration settings

## Database maintenance:

Use following commands to manage database:

* `mvn liquibase:status` to see the current state of the changelog against real database state
* `mvn liquibase:update` to apply all new changes from the changelog to the database
* `mvn liquibase:rollback -Dliquibase:rollbackCount=1` to revert changes by last changeset
* `mvn liquibase:diff` to generate a changelog between Entity Model and the database (useful for development)
* `mysqldump --user=<user> --password=<password> <database> --result-file=<file.sql>` to make database sql dump
* `mysql --user=<user> --password=<password> <database> < <file.sql>` to restore/create database from sql dump 

## Register as Windows service for standalone run (without Servlet Container or Application Server):

1. Prepare `winsw.xml` with project run configuration
2. Put it together with `winsw.exe` into service root

* `winsw.exe install` to register service
* `winsw.exe uninstall` to unregister service
