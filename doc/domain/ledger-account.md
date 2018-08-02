# Ledger - Account

## Account

An account is used to group related journal entry lines.

## Account Properties

### Chart of Account (coA)

Information identifying the chart of account containing this account.

### Parent Account (parentInCoA)

Identifier of the parent of this account in the containing chart of account.

This is essential for the classification of accounts. 

The parent account must always be indicated. There are only two accounts without parent accounts:
- The account "Estate Accounts" (aka. balance Sheet Account)
- The account "P&L Account" (aka. Profit & Lost Accounts)

Those accounts are always automatically created by the accounting module while creating the enclosing chart of account.

### Id in Chart Of Account (idInCoA)

Identifier of this account in the containing chart of account. This is generally the concatenation of the parent id and an numeric number that is unique is the scope of the parent.

The framework suggest two approaches for the generation of child ids.
- Auto: the framework will increment a number that is stored in the parent record.
- Suggested: The framework will check all existing children to make sure there is no conflicting child. 

In both cases, the framework will proceed like concatenating the parentInCoA with the new id to produce a new idInCoA and mark the column unique.

### Account Identifier

The technical id of this account. Used to reference the account in other records. 

### Detail Level

The detail level of an account is the level at which the account is created. For the two root accounts "Estate Accounts" and "P&L Account" the detail level is zero (Level 0). The detail level of each subsequent account is the detail level of the parent account + one. Mean Level[self] = Level[parent]+1.


### Valid from

The moment from which this account number can be used.

### Valid to

The moment from which the account must stop being used.

### Account number

The external business identifier of this account. Generally made out of a IBAN and a currency. 

Following three properties will generally be made unique in their combination.
- Account Number
- Valid From
- Valid To

### Account Type

The type of this account. Generally, following account types are distinguished:

- Estate Accounts
  - Asset Accounts
  - Liability Accounts

- Profit & Lost Accounts
  - Revenue Accounts
  - Expense Accounts

## Account vs. Contract

A account is not a contract. The data structure is not designed to carry more that information defined here. A contract module might assign an account number to each contract. But each account in the accounting layer does not refer to a contract. 

## Account Balance

Despite other accounting module, this framework does not store the balance in the account data structure. The balance is a special type of posting, containing a technical operation and carrying the same account number on both side with the balance amount.

### Account Balance Type

This is an "Operation Type" defined by a product module and used to identify account balances operations in the journal.

There is also tone of type of balances. Each of them can be computed any time by a product module and stored as a new posting in the journal. Beware that a balance might be a well define operation with a well defined id. This is, the daily balance of an account might appear many times in the journal depending on how often it was computed during the day. The oldest balance (posting wise) will then be the relevant operation.

### Account Balance Computation Tasks

A task computing and recording an account balance is supposed to be done synchronously in the perspective of the account id.


## Account Statement

An account statement as well is a neutral posting recorded in the journal for the purpose of documenting that a statement has been printed for that account. This might have consequences on some product processes. For example the operations are reversed might differ depending on whether the customer hat the operation printed on a statement or not:

- If a statement is not yet printed:
  - a reversal operation might not be visible to the customer
  - the posting is set such as to neutralize the effect of the erroneous operation on balances and interests. 
- If a statement is printed:
  - a reversal operation must be displayed to the customer
  - this reversal is more complicated. The customer might be credited back some formerly debited interests aso...

