/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import de.adorsys.ledgers.util.Ids;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class OperationDetails {
    @Id
    private String id;
    @Lob
    @org.hibernate.annotations.Type( type = "text" )
    private String opDetails;

    public OperationDetails(String opDetails) {
        this.id = Ids.id();
        this.opDetails = opDetails;
    }
}
