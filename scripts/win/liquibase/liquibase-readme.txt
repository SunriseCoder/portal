To apply new changes manually:
1. liquibase status
2. liquibase updateCountSQL 1
   or
   liquibase updateSQL
3. liquibase updateCount 1
   or
   liquibase update

If something went wrong:
1. liquibase status
2. liquibase rollbackCountSQL 1
3. liquibase rollbackCount 1
