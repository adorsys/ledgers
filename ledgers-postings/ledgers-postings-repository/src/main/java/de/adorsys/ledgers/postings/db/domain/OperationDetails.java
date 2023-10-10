/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import de.adorsys.ledgers.util.Ids;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class OperationDetails {
    @Id
    private String id;
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String opDetails;

    public OperationDetails(String opDetails) {
        this.id = Ids.id();
        this.opDetails = opDetails;
    }
}
