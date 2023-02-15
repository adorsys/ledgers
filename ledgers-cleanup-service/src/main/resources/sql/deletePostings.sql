/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

with postingIds as (
    SELECT posting_id
    from posting_line
    where account_id = ?1
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
     )
delete
from posting
where id in (
    select *
    from postingIds
)
