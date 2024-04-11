/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

with ibans as (
    select iban
    from deposit_account
    where id = ?1
),
     paymentIds as (
         select distinct payment_id
         from payment
         where account_id =?1
     ),
     deletePayments as (
         delete from payment where payment_id in (select * from paymentIds)
     ),
     consentIds as (
         Select distinct id
         FROM sca_ais_consent
                  join sca_ais_consent_accounts
                       on (id = sca_ais_consent_accounts.ais_consent_entity_id) or accounts in (select * from ibans)
     ),
     deleteConsents as (
         delete FROM sca_ais_consent where id in (select * from consentIds)
     ),
     deleteScaOperations as (
         delete from sca_operation where op_id in (select * from paymentIds union select * from consentIds)
     ),
     ledgerAccountIDs as (
         select a.linked_accounts as ID
         from deposit_account a
         where a.id =?1
     ),
     postingIds as (
         SELECT posting_id
         from posting_line
         where account_id in (select * from ledgerAccountIDs)
     ),
     oprDetIDS as (
         SELECT opr_details_id AS detId
         FROM posting
         where id in (select * from postingIds)
         UNION
         SELECT details_id AS detId
         FROM posting_line
         where posting_id in (select * from postingIds)),
     deleteOperationDetails as (
         delete from operation_details where id in (select * from oprDetIDS)
     ),
     deletePostings as (
         delete from posting where id in (select * from postingIds)
     ),
     deleteAccounts as (delete from ledger_account where id in (select * from ledgerAccountIDs)),
     deleteAccountAccess as( delete from account_accesses where account_id =?1)
delete
from deposit_account
where id =?1