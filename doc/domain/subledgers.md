# Sub-Ledger

A sub ledger is used to display a detail view of a chart of account. The sub ledger is generally identified by the accounts types it manages. For example:
- The book of assets and liabilities (These are estate accounts) that can be used at a well defined detail level to print an balance statement.
- The book of operating assets can be used to print out receivables.

All ledgers and subledgers must be balanced.

## Detail Level

The detail level of an account defines the granularity at which the balance of this account is explicitly displayed in the balance statement of a ledger.

### Detail Level of an Account

Accounts are inherently classified by types. For example:

- Balance Sheet Accounts (Level 0)
  - Asset Accounts (Level 1)
  - Liability Accounts (Level 1)

- Profit & Lost Accounts (Level 0)
  - Revenue Accounts (Level 1)
  - Expense Accounts (Level 1)
   
The example above shows two detail levels: 0 and 1. Any account created in this ledger must inherit from those accounts. 

### Specifying a sub ledger

So defining a sub ledger boils down to :
- providing a list of accounts
- For each account provided, specifying the number of levels to display for the children of that sub ledger. 

Consider the following chart of account:

- Balance Sheet Accounts (Level 0)
  - Asset Accounts (Level 1)
    - Fixed Assets (Level 2)
    - Operating Assets (Level 2)
      - Accounts Receivable (Level 3)
      - Banks (Level 3)
      - Cash (Level 3)
  - Liability Accounts (Level 1)
  	- Owner's Equity (Level 2)
    - Bonds Payable (Level 2)
    - Accounts Payable (Level 2)

- Profit & Lost Accounts (Level 0)
  - Revenue Accounts (Level 1)
  - Expense Accounts (Level 1)
  
A sub ledger with focus on Account receivables can be formulated like:
First:
- Estate Accounts (Level 0) -> 0 (Mean no Details) -> Default.
- Profit & Lost Accounts (Level 0)  -> 0 (Mean no Details) -> Default.
Then:
- Accounts Receivable (Level 3) -> 2 (Mean display level 5. In this case the balance of each single customer)

A corresponding statement will display n+2 lines:
- Estate Accounts (one line with all other estate accounts. Summing debit balance and credit balances)
- All customer accounts (n lines for n customers)
- Profit & Lost Accounts (one line. all profit and lost accounts. Summing debit balance and credit balances)
