# Extension and deployment


### Building and Running

```
	> git clone https://github.com/adorsys/ledgers.git
	> cd ledgers
	> mvn clean install
	> cd ledgers-app
	> mvn spring-boot:run -Dspring.profiles.active=h2
```

This will start the ledgers app with the embedded h2 database.

### Visiting the API

[http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html#/)
