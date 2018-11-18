# Ledger

## Posting and Double Entry Accounting Basic

For a datamodel see: [Posting and Double Entry Accounting Basic](postings.md)


# Microservices and Transactional Boundaries

The accounting module is designed with following goals:

- Allow for isolated used as a micro service
- Allow for embedding in another service container

Each of this approaches presents advantages and drawbacks.

## Microservice

Using the accounting module as an isolated service runtime allows for clean separation between service components. 

But this operational mode brings the complexity of cutting transaction boundaries. So each request to the accounting module will have to be considered a proper transaction.

Having spited transaction boundaries will force the designer team find another way of enforcing consistency through the whole application landscape. View the fact that this accounting module dela only with book keeping functionality.


## Embedded Libs

Embedding the account module into the product module will allow us keep a common transaction boundary between the consuming product module and the account module.

This will also remove the whole transactional complexity while modeling the interaction between the accounting module and the product module.

Additionally, using this mode can allow for a natural extension of each domain model, by allowing a accounting extension module to run in the same transaction context with the core module. 

## Multimodule Spring Service

This page will be used to analyze how to run the accounting module embedded inside another module. We are dealing with following question.

1- Is it possible to run a spring boot application inside another runtime without moving it's Application-Class away?

2- Most services are accessible over http. How do i design an abstraction layer that allow an embedding module to directly access the service layer?
  - IT is meaningful to put all functionality in the services and userDetails the controller only as protocol layer
  - Is it meaningful to prevent deployment of controller and only allow access only thru the consuming module?
  	- In this case we might also consider developing two separated modules: 1 services module and a rest wrapper.
  	- Then the service module can directly be embedded into the consuming module without the need of activating those Rest services.
  	
3- It might be meaningful to distinguish between export interfaces, and journal entry end point:
  - In a clean design, the journal entry end point will have to be fully protected by the embedding product module to prevent the proliferation of unqualified postings.   

## Example

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


# Robustness

## Insert Only Database (For core records)

In the attempt to build a highly scalable accounting system, we are thinking about keeping change operations on core objects of the accounting module to insert-only.

Not having to change a created object can provide following advantages:

- Once created, an object can be quickly replicated across physical data centers, providing very scalable read caches.
- There is no need to maintain an invalidation cache  

## Idempotent Operation

We have design the accounting module so that any operation can be executed many times without having to compromise the consistency of the system. In orther to realize this, we make everything dependable on a moment. We distinguish between three core moments:

1- The operation time: This is the moment at which the business operation take place
2- The posting time: This is the moment at which the posting is supposed to happen
3- The recording time. This is the moment at which we record the posting in the accounting module.

The recording time is independent of both posting and operation time.

1- If an operation is recorded too late, dependent computations will have to run again, resulting posting will keep the original operation id and thus override them without any impact on the rest of the system.  
2- If an operation is recorded to early, dependent computations might run any time. The computation result as well as resulting actions will be temporal until posting date is reached. New computation might happen any time if originating operations are changed.

## Vertical Partitioning

Because of the read only character of critical records like journal entries, it might easy to off-load records to more read efficient databases without undergoing any consistency risk. 

These read database might even be partitioned over posting time for faster access to those record. Especially closed accounting periods can replicated at will.


##  Horizontal Partitioning

Even though we are dealing with a booking system, the system is designed such as to allow for parallel insert of account transactions. This parallelism bears the risk of not solving the double spending problem.

Fortunately only a fraction of accounts manage by the accounting system are exposed to that. Those are payment accounts.

Horizontal partitioning of payment accounts can help tackle the problem by assigning each payment account to a proper cluster. 
- Routing spending operation associated with the account to that cluster will help control spending access to the account (pessimistic locking the account).
- Synchronizing all incoming operation associated with that account with that cluster will help keep the balance actual but with some latency (eventual consistency)

With this approach event though we might prevent some expenses to happen while the account is furnished, we will never have a double spending problem.

The pattern will look like:
- Receive a journal entry
- For each double spending aware account is on the debit side
  - Get most actual balance from the assigned partition (there reserve the spending amount)
- Record the transaction
- Notify all clusters (starting with the partition of spending aware accounts)  

# Asynchronous Processing

## Need for Asynchronous Processing

The approach we chose for robustness requires a lot of asynchronous processing operations. Based on the nature of an account, we can decide to process the balance of a account as part of the journal recording operation or asynchronously. This feature shall be made configurable on a per account basis to allow for flexibility. A spending account might need a balance computation after each transaction. A P&L account might need it at the closing of the accounting period.

While storing a journal entry, the accounting module will check if there is any account that requires synchronous balance computation. If this is the case, the balance of the corresponding account will be computed an recorded as a separated posting but in the same transaction. If there is no required synchronous balance computation, the accounting module will check for the presence of the corresponding task. If there is no task, it will create one and store as part of the recording transaction.

## Design of the Asynchronous Processing

In order to guaranty for consistency, each asynchronous processing task must first be recorded in a task table as part of the originating transaction.

### Direct processing 

The direct processing of the task can be initiated in an asynchronous call after committing the main transaction. 

### Thread Pool

But due to the fact that, there is no guaranty processing is going to succeed, it is necessary to have a thread pool monitored those batch tables and restart failed jobs. 

### Ordered Processing for Consistency

In order to guaranty for consistency, ordered processing of task associated with an account might be enforced:
- Before another modification operation
- Before a critical read operation
- ...

This is, the process in charge of reading or processing changes will first check if there is any pending task and make sure those are executed and recorded first.

## Need for Lose Coupling between Entities

Event though a journal posting spawns many accounts, there is no need to have all account post processed in a single transaction. The common synchronization information between all accounts affected by the posting is the posting time. In the near future at the posting time in normal conditions all operation would have succeeded. An if one does not, the system will be in an inconsistent state at that posting time. And after the fix the processing can continue without having to compromise the consistency. (Eventual Consistency) 


## Insert Only Job Processing System

In order to guaranty for robustness, we will be trying to design an insert only job processing system.

- In the job table Job to be processed are inserted
- In the status table
  - A worker thread can obtain a lock to process jobs associated with an account. This is done by inserting a status line.
  - A worker thread will insert the last successful status line for that account in the table.
  - A recovery thread can take over processing by adding a new status line if older thread ran out of lease.
  	- Out of lease state is discovered by not finding a corresponding success or failure status entry in the table by the time out time.
  	- Thereby a worker thread is not allowed to write any success or failure status in the table after it runs out of lease.
  	- Worker thread might nevertheless extend the lease if control hasn't yet been taken by another thread.
 - Cleanup happen by moving task status of completed tasks to a read only system.  

