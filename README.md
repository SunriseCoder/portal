# Portal

## Build project:

Use following commands to manage project build:

* `mvn clean package -Denv=dev` to make development build
* `mvn clean package -Denv=uat` to make a build for UAT environment (with special application.properties file)
* `mvn clean package -Denv=prod` to make a build for production environment (with special application.properties file)

## Database maintenance:

Use following commands to manage database:

* `mvn liquibase:status` to see the current state of the changelog against real database state
* `mvn liquibase:update` to apply all new changes from the changelog to the database
* `mvn liquibase:rollback -Dliquibase:rollbackCount=1` to revert changes by last changeset
* `mvn liquibase:diff` to generate a changelog between Entity Model and the database (useful for development)
* `mysqldump --user=<user> --password=<password> <database> --result-file=<file.sql>` to make database sql dump
* `mysql --user=<user> --password=<password> <database> < <file.sql>` to restore/create database from sql dump 

## Register as Windows service:

1. Prepare `winsw.xml` with project run configuration
2. Put it together with `winsw.exe` into service root


* `winsw.exe install` to register service
* `winsw.exe uninstall` to unregister service

## Troubleshooting

MySQL 8 Server Time

Problem:
* java.sql.SQLException: The server time zone value 'RTZ 2 (çèìà)' is unrecognized or represents more than one time zone. You must configure either the server or JDBC driver (via the serverTimezone configuration property) to use a more specifc time zone value if you want to utilize time zone support.

Solution:
1. Execute script [scripts/sql/set_server_time.sql](scripts/sql/set_server_time.sql)
2. Be sure, that `SELECT NOW();` returns correct time
