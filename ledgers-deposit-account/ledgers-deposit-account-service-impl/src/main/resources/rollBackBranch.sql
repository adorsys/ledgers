with depAccIds as (
    select id, linked_accounts
    from deposit_account
    where branch = ?1 --All accounts belong to BRANCH
),
     paymentIds as ( --Select Payment IDs to delete
         select distinct payment_id from payment where account_id in (select id from depAccIds) and updated > ?2
     ),
     deletePayments as (
         delete from payment where payment_id in (select * from paymentIds)
     ),
     consentIds as (
         Select distinct id
         FROM sca_ais_consent
                  join sca_ais_consent_accounts
                       on (id = sca_ais_consent_accounts.ais_consent_entity_id) or accounts in (select iban
                                                                                                from deposit_account
                                                                                                where deposit_account.id in (select id from depAccIds))
     ),
     deleteConsents as (
         delete FROM sca_ais_consent where id in (select * from consentIds)
     ),
     deleteScaOperations as (
         delete from sca_operation where op_id in (select * from paymentIds union select * from consentIds)
     ),
     postingIds as ( -- Postings to delete
         SELECT posting_id
         from posting_line
         where account_id in (select linked_accounts from depAccIds)
           and record_time > ?2
     ),
     oprDetIDS as ( -- matching posting Ids
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
     deleteLedgerAccounts
         as (delete from ledger_account where id in (select linked_accounts from depAccIds) and created > ?2),
     deleteDepAccounts as (delete from deposit_account where branch = ?1 and created > ?2),
     deleteUsers as (delete from users where branch = ?1 and created > ?2),
     deletePoints as(delete from recovery_point where branch_id = ?1 and roll_back_time > ?2)
delete
from account_accesses
where account_id in (select id from depAccIds)
  and created > ?2
