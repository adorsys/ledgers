# ledgers-db

Ledgers database revisions are managed with liquibase. This module contains schema and data migrations.

## Running
When h2 profile is used all migrations are executed on application start using spring-liquibase integration.

With postgres you should apply migrations using maven liquibase plugin.

`mvn liquibase:update`

## Test data
Change sets that insert test data should be marked with _test_ context to make it possible to run only schema changes (e.g. `mvn liquibase:update -Dliquibase.contexts=prod`). Note that if you don't specify a context [all migrations will be executed](https://www.liquibase.org/documentation/contexts.html). 
