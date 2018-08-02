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