# MockBank

Using ledgers modules for a mockbank requires some background on book keeping. 

See [Accounting Basics](../doc/architecture/postings.md) for some initial knowledge on double entry accounting.

First of all, you need a Chart of Account (COA). A COA defines accounts are allowed in your banking system. See a sample chart of account at: [Sample Chart Of Accounts](src/main/resources/sample_coa_banking.yml).

## Sample Configuration

In order to have the mock bank up and ready, we need following artifacts. Look at the file at: [Sample Mock Bank Config](src/main/resources/mockbank-config.yml)

Given each ledger a name allow to operate many ledgers in a single database.

```
name: mockbank
ledger: mockbank
coaFile: sample_coa_banking.yml
```

At initialization the coaFile is read and all account are created.

### Special Accounts

The coaExtensions field collect account that will be created at initialization. Thos are generally clearing account for different payment product supported.

```
coaExtensions:
### Clearing account as subaccount of  Deposits with Centralbank - Non-Interest bearing
  - shortDesc: SEPA-Clearing Account (sepa-credit-transfers)
    name: 11031
    parent: 1103
  - shortDesc: INSTANT_SEPA-Clearing Account (instant-sepa-credit-transfers)
    name: 11032
    parent: 1103
  - shortDesc: TARGET2-Clearing Account (target-2-payments)
    name: 11033
    parent: 1103
  - shortDesc: CROSS_BORDER-Clearing Account (cross-border-credit-transfers)
    name: 11034
    parent: 1103
```

The clearingAccounts field defines associates each created clearing account to a defined payment product.

```
### Payment products supported.
clearingAccounts:
  - paymentProduct: SEPA
    accountNbr: 11031
  - paymentProduct: INSTANT_SEPA
    accountNbr: 11032
  - paymentProduct: TARGET2
    accountNbr: 11033
  - paymentProduct: CROSS_BORDER
    accountNbr: 11034

```

The depositParentAccount field defines the account number of the parent account of all customer deposit accounts. Customer deposit accounts are created dynamically upon registering a customer as a child account of the depositParentAccount.

```
depositParentAccount: 2332
```

## Test Data

The file [Sample Mock Bank Test Data](src/main/resources/sample-test-data.yml) is finally used to produced some test accounts and users so we can browse through the system without having to fill it up manually.

### Sample Accounts

here we create 3 accounts. Using IBAN as account numbers, even in the chart of account.

```
###############################################
#
# Accounts :
# 
# to be created by the test system
#
###############################################
accounts: 
  - iban: DE69760700240340283600
    currency: EUR
    name: Marion Mueller
    product: Cash24
    accountType: CASH
    accountStatus: ENABLED
    usageType: PRIV
  - iban: DE80760700240271232400
    currency: EUR
    name: Anton Brueckner
    product: Cash24
    accountType: CASH
    accountStatus: ENABLED
    usageType: PRIV
  - iban: DE38760700240320465700
    currency: EUR
    name: Max Musterman
    product: Cash24
    accountType: CASH
    accountStatus: ENABLED
    usageType: PRIV
    
```

### Sample Users

Here we create online banking users and give them access to bank accounts. We also define SCA (strocn customer authentication) data.

```
###############################################
#
# Users :
# 
# Online banking users to be created by the test system
#
###############################################
users:
  - login: marion.mueller
    email: marion.mueller@mail.de
    pin: 12345
    scaUserData:
      - scaMethod: EMAIL
        methodValue: marion.mueller@mail.de
    accountAccesses:
      - iban: DE69760700240340283600
        accessType: OWNER
  - login: anton.brueckner
    email: anton.brueckner@mail.de
    pin: 12345
    scaUserData:
      - scaMethod: EMAIL
        methodValue: anton.brueckner@mail.de
    accountAccesses:
      - iban: DE80760700240271232400
        accessType: OWNER
  - login: max.musterman
    email: max.musterman@mail.de
    pin: 12345
    scaUserData:
      - scaMethod: EMAIL
        methodValue: max.musterman@mail.de
    accountAccesses:
      - iban: DE38760700240320465700
        accessType: OWNER

```


### Sample Payments

We also orchestrate some payment so we can read transactions and balances after starting the system.

```
###############################################
#
# Single Payments :
# 
# Single Payments to bank internal and bank external accounts
#
###############################################
singlePayments:
# Payment from Marion Mueller to Max Musterman on 2018-06-01 at 12:25:00
  - singlePayment:
      requestedExecutionDate: 2018-06-01
      requestedExecutionTime: 12:25:00
      debtorAccount:
        iban: DE69760700240340283600
        currency: EUR
      instructedAmount:
        amount: 3000
      creditorAccount:
        iban: DE38760700240320465700
        currency: EUR
      creditorName: Max Musterman
      remittanceInformationUnstructured: Payment
      paymentProduct: SEPA
    balancesList:
      - refTime: 2018-06-01T00:00:01
        balances:
          - accNbr: DE69760700240340283600
            balance: 0.00
          - accNbr: DE38760700240320465700
            balance: 0.00
      - refTime: 2018-06-01T12:30:01
        balances:
          - accNbr: DE69760700240340283600
            balance: -3000.00
          - accNbr: DE38760700240320465700
            balance: 3000.00
    
```

