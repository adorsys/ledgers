# Ledgers Sample App
This is a standalone deployment of the ledgers deposit account application. This module is not meant to be productively used. It can be deployed to explore the functionality of the ledgers module on a swagger rest interface.

In order to run, follow theses steps.


## Building and Running

We offer two options: h2 with an external file at ~/ledgersdbs/ledgers-app and postgres started with docker compose using /ledgers-app/docker-compose-postgres.yml

## Building

```
	> git clone https://git.adorsys.de/adorsys/xs2a/ledgers.git
	> cd ledgers
	> mvn clean install
```

### Running with H2

H2 database files are found in: ~/ledgersdbs/

```
	> cd ledgers-app
	> mvn spring-boot:run -Dspring.profiles.active=h2
```

### Running with postgres

Start the postgres database using the attached docker compose file.

```
	> cd ledgers-app
	> docker-compose -f docker-compose-postgres.yml up
```

Running with the postgres profile.

```
	> cd ledgers-app
	> mvn spring-boot:run -Dspring.profiles.active=postgres
```

### Running with embedded SMTP server
The application sends SCA auth codes via email. You can start the app with an embedded SMTP server that will log out 
sent emails by activating mock-smtp maven profile.
```
    > mvn clean install -P mock-smtp
```

### Provisioning with Test Data postgres

The application automatically provision the database with sample data. To turn this functionality off while developing, use:

```
> mvn spring-boot:run -Dspring.profiles.active=postgres -Dledgers.mockbank.data.load=true

```

## Visiting the API

[http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html#/)

Sample user and pin for preloaded users are provided on the swagger documentation.


## Security

The application is protected using spring security web and method security annotations.

user the following cure request to obtain the an access token:

```
	> curl -X POST "http://localhost:8088/users/authorise2?login=marion.mueller&pin=12345&role=CUSTOMER" -H "accept: */*"
```

Call will return an access token. Then add the access token to any other curl request to access other endpoints.

The following call will return the list of accounts connected to the banking user marion.mueller:

```
	> curl -X GET "http://localhost:8088/accounts" -H "accept: */*" -H "Authorization: Bearer <ACCESS_TOKE>"
```

Get a look at the swagger documentation for other authentication flows.

