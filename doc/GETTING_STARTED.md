# Getting started


## Development

### Dependencies

Ledgers is heavily dependent on spring for now.

### Building and Running

```
	> git clone https://git.adorsys.de/adorsys/xs2a/ledgers.git
	> cd ledgers
	> mvn clean install
	> cd ledgers-app
	> mvn spring-boot:run -Dspring.profiles.active=h2
```

This will start the ledgers app with the embedded h2 database.

### Visiting the API

http://localhost:8088/swagger-ui.html#/


### Visiting the Database

when started with the h2 profile, you can use the web browser to visit database tables on the url http://localhost:8088/h2-console/ . make sure you use the following connection properties:

Driver Class : org.h2.Driver
JDBC URL: jdbc:h2:mem:ledgers
User Name: sa
Password: sa

Press Connect button and you can explore the data model.

More on this to come...
