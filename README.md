This is the implementation of a simple ledger with following features:

- Classical functionality of an accounting module
    - Journaling
    - Closing
    - Account Balance Inquiry
    - Account Statement Reporting
- Some innovative accounting funtionalities
    - Read only journal entries
    - No batch activity for predefined closing operations (day, month, year)
    - Asynchronous balance computation for parallel processing of journal entries
    - Detaching processing time and entry effective time, allowing the storage of future and/or passt entries
    - Distinction between prospective entries and effective entries
- Some technical innovations
    - Securing integrity of entries using blockchain technology
    - Allowing in place extension of all data models
    - Allowing the embedding of the accounting module in another application (for transactional integrity)
    - Allowing the horizontal partitioning of the module using eventual consistency techniques
    - Allowing the vertical partitioning (time based) of entries to increase the throughput of parallel write operations
    