# Ledgers
[![Build Status](https://travis-ci.com/adorsys/xs2a.svg?branch=develop)](https://travis-ci.com/adorsys/ledgers)

Banking Booking System kept simple!

## Features

### Deposit Account Implementation

Simple implementation of a deposit account that supports:

* Account management
    * Creating a banking deposit account
    * List transactions
* Initiating and executing payments 
    * Single payment
    * Future dated payments
    * Periodic payments (scheduller pending)
    * Bulk payments (batch and non batch)
* Cash deposit (Still pending)
* Statement and Balances
    * Account Statement
    * Trial Balances
* String Customer Authentication
    * Authorizing payment initiation
    * Authorizing transactions listings
    * Support for multiple SCA Methods
        * 1:n E-Mails
        * 1:n mobile phones  
    

### Double Entry Accounting : Ledgers Posting

This is simple implementation of the double entry accounting with some additional features like:

* Classical functionality of an accounting module
    * Journaling of Transactions
    * Account Balance Inquiry
    * Account Statement Reporting
* Some innovative accounting functionalities
    * Read only journal entries
    * No down time for time based closing operations (day, month, year)
    * Asynchronous balance computation for parallel processing of journal entries
    * Detaching processing time and entry effective time, allowing the storage of future and/or passt transactions
* Some technical innovations
    * Securing integrity of entries using hash chains
    * Spring based JPA module embedding for transactional integrity
    * Allowing the horizontal partitioning of the module using eventual consistency techniques
    * Allowing the vertical partitioning (time based) of entries to increase the throughput of parallel write operations

### Configuration

The deposit account module needs a chart of account. You can find a sample chart of account at: [Sample Chart Of Accounts](ledgers-deposit-account/ledgers-deposit-account-service-impl/src/test/resources/de/adorsys/ledgers/deposit/api/service/impl/mockbank/sample_coa_banking.yml)

### Dumy SCA and User Management

We provide a sample SCA and user management application no to be used in a productive environment.

### Middleware Module

This is a sort of online banking application exposes the deposit account functionality to an online environment enabling the following workflow:
* Create deposit accounts
* Create banking users
* Initiate Payments
* Check balances
* Read payment transactions 

This is a sample test data file for the deposit account [Sample Test Data](ledgers-deposit-account/ledgers-deposit-account-service-impl/src/test/resources/de/adorsys/ledgers/deposit/api/service/impl/mockbank/use_case_newbank_no_overriden_tx.yml)

## Who we are
[adorsys](https://adorsys.de/en/index.html) is a company who works ever since the very beginning of PSD2 with its requirements and implicit tasks.
We help banks to be PSD2 complaint (technical and legal terms). To speed up the process we provide this open source XS2A interface, specified by Berlin Group,
that can be connected to your middleware system.
You can check your readiness for PSD2 Compliance and other information via [our Web-site](https://adorsys.de/en/psd2.html).


## Getting Started

See below for a short technical introduction of the module. More to find in [These instructions](doc/GETTING_STARTED.md). The instruction below will surely get you a copy of the project up and running on your local machine for development and testing purposes. 

### Dependencies

Ledgers is heavily dependents on spring for now. We are still working on a more inclusive dependency management.

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


### Visiting the Database

when started with the h2 profile, you can use the web browser to visit database tables on the url [http://localhost:8088/h2-console/](http://localhost:8088/h2-console/) . make sure you use the following connection properties:

Driver Class : org.h2.Driver
JDBC URL: jdbc:h2:mem:ledgers
User Name: sa
Password: sa

Press Connect button and you can explore the data model.

More on this to come...

## Module structure

We user Spring dependencies to assemble module. Each module contains following dependency management artefact.

The ledgers application is built to be fully extensible and embeddable into other JPA applixations.


| Artifact | Description |
|------------|-------------|
| `@EnableModuleName` | Annotation used to select an implementation module among alternatives. This will generally be dropped on a Spring Application class. |
| `@ModuleNameConfiguration` | Main spring configuration class for the module. Might include other modules, scan entities, initialize resources. |
| `@ModuleNameBasePackage` | Marker class used to document package scanning for a module. We will generally me stuff like: `@ComponentScan(basePackageClasses = DepositAccountServiceBasePackage.class)`. The package of this call must allow for scanning of all spring components in the module. |

You can easily use features by adding following annotations to your spring `@Configuration` class:

| Annotation | Description |
|------------|-------------|
| `@EnableDepositAccountService` | Enables the deposit account service module.|
| `@EnableLedgersMiddlewareRest` | Enables the the Ledger middleware rest application. |
| `@EnableLedgersMiddlewareService` | Enables the Ledger middleware service. |
| `@EnablePostingService` | Enables the postings service module.   |
| `@EnableSCAService` | Enables The SCA service module  |
| `@EnableUserManagementService` | Enables the a user management service  |

Following JPA module are automatically included in the corresponding service modules so they generally do not need to be considered while assembling modules. 
 
| Annotation | Description |
|------------|-------------|
| `@EnableDepositAccountRepository`| Enables the deposit account JPA module. |
| `@EnablePostingsReporitory`  | Enables the ledgers posting repository module.                 |
| `@EnableSCARepository` | Enables the sca repository module |
| `@EnableUserManagmentRepository`   | Enables a user management module. |

## Brief architecture documentation
Available in [the documentation](doc/architecture/README.md)

## Deployment

Dockerfiles will be provided to allow to put the build artifacts into a docker images. Not available for now.

More details see in [instruction](doc/deployment.md)

## Built With

* [Java, version 1.8](http://java.oracle.com) - The main language of implementation
* [Maven, version 3.0](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Spring boot as core Java framework

## Development and contributing

Please read [CONTRIBUTING](doc/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Release notes
  
  * [Release notes](doc/architecture/release-notes/releasenotes.md) 
 
## Roadmap
 
 * [Roadmap](doc/roadmap.md) - The up-to-date project's roadmap

## Authors

* **[Francis Pouatcha](mailto:fpo@adorsys.de)** - *Initial work* - [adorsys](https://www.adorsys.de)

See also the list of [contributors](doc/contributors.md) who participated in this project.

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details
