# Journal

A journal is an ordered set of records of financial operations. A single record in a journal is called a posting. The word posting is associated with the moment at which the recorded operation is effective in the ledger. Therefore, we distinguish between following moments:

- The operation time: the moment at which this operation took place..
- The posting time: the moment at which this operation is effectively posted (e.g. having influence of an account balance).
- The recording time: the moment at which this operation is recorded in the journal.

## Posting

Again, a posting is an entry in the journal. You might also call it a journal entry.

### Recording Information

A posting is used to record exactly one operation. Following technical information are recorded with a posting.

#### Posting Identifier

The unique identifier of this posting. This is a UUID that shall not be repeated.

#### Recording User

The user (technically) recording this posting.

#### Recording Time

The time of recording of this posting. 

#### Predecessor Identifier

The Identifier of a preceding posting. Generally the identifier of any eventually most recent posting (by recording time).

#### Posting Hash

The hash value of this posting. If is used by the system for integrity check. A posting is never modified.

The hash value is computed using:
- The hash value of a predecessor
- All additional posting information (See computation algorithm).

### Operation Information

Operation information contains the business semantic of the posting.

#### Operation Identifier

The unique identifier of this business operation. The operation identifier differs from the posting identifier in that it is not unique. The same operation, can be repetitively posted if some conditions change. The operation identifier will always be the sam for all the postings of an operation. Only one of them will be effective in the account statement at any given time. 

#### Operation Time

The time of occurrence of this operation. Set by the consuming module. 

- The operation time can be before, same of after the posting time. 
- The operation time can be before or after the recording time.

The accounting module does not process the operation time. The is store for information purpose and can be processed by consuming product modules.

#### Posting Time

This is the time from which the posting is effective in the account statement. This also differs from the recording time in that the posting time can be before or after the recording time.

If the posting time if before the recording time, it might have an effect on former postings like past balances. This might lead to the generation of new postings.

The posting time of an adjustment operation at day closing is always the last second of that day. So event if that operation is posted while still inside the day, the day closing will be the same. This is, the last second of that day. In the case of an adjustment operation, the posting time and the operation time are identical. 

#### Value Time

The Date use to compute interests. This can be different from the posting date and can lead to the production of other type of balances.

#### Operation Type

The type of operation recorded here. The semantic of this information is determined by the consuming module.

#### Posting Type

Some posting are mechanical and do not have an influence on the balance of an account. Depending on the business logic of the product module, different types of posting might be defined so that the journal can be used to document all events associated with an account.

For a mechanical posting, the same account and amounts must appear in the debit and the credit side of the posting. Some account statement will not display mechanical postings while producing the user statement. 

#### Operation Details

Details associated with this operation.

#### Operation Currency

The currency of this operation. All entries of an operation are associated with the same currency.

#### Posting Line

Line in the journal entry with properties like
- account id : the identifier of the account.
- sub account : used to refine specification of a posting
- amount : the amount in the posting currency
- source amount : the amount in the source currency, if different from the operation currency
- source currency : the source currency, if different from the operation currency
- source account id : generally needed while replacing an invalid account with a default account. See below (Conto Pro Diverse)
- conversion details

#### Debit Entries -> Posting Line

List  of debit transaction entries. Amounts are always positive amounts.

#### Credit Entries -> Posting Line

List of credit transaction entries. Amounts are always positive amounts.

#### Originating Operations

These are operations whose effect lead to the creation of this posting. Might any of these operation receives new posting, then the computation of this operation will be reconsidered.

### Ledger (coA)

Redundant information identifying the ledger of this journal.

## Operation Notes

Operation notes are used to associate information to an operation. They are stored in a proper table with following fields:

### Posting Identifier

This is the identifier of the  posting from which the note is written.

### Operation Identifier

This is the identifier of the operation. Used to group all notes of a related operation.

### Note Identifier

This is the identifier of this note entry.

### Note Type

This is the type of the note. A note can be a simple comment, a task, a reminder...
 
### Note Content

This is the content of the note. Format might be dependent of the note type.

### Recording Time

Time of recording of this note.

### Execution Time

Prospective time of execution of this note.

### Premature execution allowed

States if execution might occur before execution time.

### Repeated execution allowed

States if repeated execution is allowed.

### Execution Status

Document the status of execution of this note.

## Properties of a Journal

In order to customize the behavior of a journal, we might assign some properties to a journal object.

### Validate Double Entry Bookkeeping

Requires the journal to check that the sum of debit activities is equal to the sum of credit activities in the operation currency.

### Validate Hashes

Recompute the hash before storing the posting.

### If Invalid Account

Check is account is valid before storing the posting. An account can be invalid because:
- It does not exist
- It is closed
- ...

#### Fail

Do not store posting. 

#### DefaultAccount (Conto Pro Diverse)

Replaces the inexistent account with a default account before storing the posting. From the default account operations, manual processing will be necessary to fix those postings, thus generating new postings with the same operation id.

### Validate Currency

Validate that all operations associated with an account are recorded in the currency of that account. 

    