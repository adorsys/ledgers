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

http://localhost:8088/swagger-ui.html#/

### Testing API with Postman json collections
 
 For testing API of xs2a it is used Postman https://www.getpostman.com/
 Environment jsons with global parameter’s sets and Collections of jsons for imitation of processes flows are stored in /scripts/tests/postman folder.
 To import Postman collections and environments follow next steps:
 1.     Download Postman jsons with collections and environments to your local machine.
 2.     Open Postman, press button “Import”.
 3.     Choose “Import file” to import one json or “Import folder” to import all jsons within the folder, then press button “Choose Files” or “Choose Folders” and open necessary files/folders.
 4.     To change settings of environments - go to “Manage Environments”, press the environment name and change variables.
 
 To start testing with Postman collections it is necessary to have all services running.
 

