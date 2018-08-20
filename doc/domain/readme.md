# Ledger

The following image describes the component architecture of a ledger.

![Component diagram](ledgers-LedgerDiagramm.jpg)

## Chart of Account 

A chart of account models a ledger by defining the types of accounts that can be contained in that ledger as well as hierarchy constraints among those accounts. This is we can say that a "Chart of Account" defines "Account Types".

## Account Type

When addressing elements of a chart of account, we talk. about "Account Types"

## Ledger

A ledger is a tree of accounts, whereby each account references a type from the associated chart of accounts.  Each ledger is governed by exactly one chart of accounts.

When addressing elements of a ledger, we talk. about "Accounts"

## Ledger Account

An account is used to group related posting lines. Each account has exactly one account type in the ledger's chart of account. Accounts are arranged in a tree structure. This tree structure is constrained by the tree structure of the account types in the chart of account.

An account created as a descendant of the "Balance Sheet Account" can not reference a type that is descendant of a "P&L Account Type" 
