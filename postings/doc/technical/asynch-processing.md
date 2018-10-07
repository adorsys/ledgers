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