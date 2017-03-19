# Portal

## Maven usage:

Use following commands to manage project build:

* `mvn clean package` to make development build
* `mvn clean package -Puat` to make a build for UAT environment (with special application.properties file)
* `mvn clean package -Pprod` to make a build for production environment (with special application.properties file)

## Liquibase usage:

Use following commands to manage database:

* `mvn liquibase:status` to see the current state of the changelog against real database state
* `mvn liquibase:update` to apply all new changes from the changelog to the database
* `mvn liquibase:rollback -Dliquibase:rollbackCount=1` to revert changes by last changeset
* `mvn liquibase:diff` to generate a changelog between Entity Model and the database (useful for development)
