# Accounting

## Accounting Basic

This is an implementation of a simple accounting system based on the lectures found in:
* https://www.business-case-analysis.com/account.html
* https://www.business-case-analysis.com/accrual-accounting.html
* https://www.business-case-analysis.com/journal.html
* https://www.business-case-analysis.com/ledger.html
* https://www.business-case-analysis.com/trial-balance.html

## Data Model for the Posting Module

![Data Model](ledgers-LedgerDiagramm.jpg)


## Balance Sheets

### Accounting Period

An accounting period has an opening time and a closing time. 

Producing a balance is just a report or relevant technical postings in the accounting period.

### Trial Balance

A trial balance has the purpose of producing the balance sheet of a root ledger at a given moment. Producing a trial balance involves following operations:

- Producing the balance of all accounts of the ledger that do not have an actual balance posting to the given trial balance date.
- Producing the profit and lost statement by recording P&L postings that balance profit and lost accounts to zero to the benefit of the P&L-Estate account.

As this is only a trial balance and there shall be no influence on the balance of profit and lost accounts, revert P&L-Posting associated with the corresponding trial balance at the moment after the trial balance moment (posting time wise).  

### Closing Balance Sheet

A balance sheet is producing, by closing the business period. In order to turn a trial balance into a final balance sheet, reversal operations have to be neutralized as part of the upcoming opening balance. This means, all trial balance neutralizing P&L Statements have to be neutralized again as well at the same moment.

### Opening Balance Sheet

After closing an accounting period, we assume no modification can be done on the posting times before the closing date. This assumption gives us the opportunity of shortening the processing depth of the accounting module repeating all account balances with a posting date equal to the posting date of the moment after the closing balance sheet.

- After recording an opening balance sheet, we can instruct the journal interface not to accept any posting with posting time laying before the opening date.  
- Doing this, we can off load all postings associated with the former accounting period to a read only database and increase the write performance of the accounting module. 



