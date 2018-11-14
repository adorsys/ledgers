# Spring based implementation of a ledger

## Providing following features

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

## Who we are
[adorsys](https://adorsys.de/en/index.html) is a company who works ever since the very beginning of PSD2 with its requirements and implicit tasks.
We help banks to be PSD2 complaint (technical and legal terms). To speed up the process we provide this open source XS2A interface, specified by Berlin Group,
that can be connected to your middleware system.
You can check your readiness for PSD2 Compliance and other information via [our Web-site](https://adorsys.de/en/psd2.html).


## Getting Started

[These instructions](doc/GETTING_STARTED.md) will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Brief architecture documentation
Available in [the documentation](doc/architecture/README.md)

## Deployment

Dockerfiles provided in the project allow to put the build artifacts into a docker images. Those images are to be
configured through your environment (documentation follows) to interact properly.

More details see in [instruction](doc/deployment.md)

## Built With

* [Java, version 1.8](http://java.oracle.com) - The main language of implementation
* [Maven, version 3.0](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Spring boot as core Java framework

## Development and contributing

Please read [CONTRIBUTING](doc/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Release notes
  
  * [Release notes](doc/releasenotes.md) 
 
### Testing API with Postman json collections
 
 For testing API of xs2a it is used Postman https://www.getpostman.com/
 Environment jsons with global parameter’s sets and Collections of jsons for imitation of processes flows are stored in /scripts/tests/postman folder.
 To import Postman collections and environments follow next steps:
 1.     Download Postman jsons with collections and environments to your local machine.
 2.     Open Postman, press button “Import”.
 3.     Choose “Import file” to import one json or “Import folder” to import all jsons within the folder, then press button “Choose Files” or “Choose Folders” and open necessary files/folders.
 4.     To change settings of environments - go to “Manage Environments”, press the environment name and change variables.
 
 To start testing with Postman collections it is necessary to have all services running.
 
## Roadmap
 
 * [Roadmap](doc/roadmap.md) - The up-to-date project's roadmap

## Authors

* **[Francis Pouatcha](mailto:fpo@adorsys.de)** - *Initial work* - [adorsys](https://www.adorsys.de)

See also the list of [contributors](doc/contributors.md) who participated in this project.

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details
    