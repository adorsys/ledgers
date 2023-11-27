/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * The existence or value of a ledger entity is always considered relative to
 * the posting date.
 * <p>
 * When a book is closed, modification on ledger entities must lead to the
 * creation of new entities.
 *
 * @author fpo
 */
@Data
@AllArgsConstructor
public abstract class NamedBO {

    /*Business identifier.  Always unique in a certain scope. Generally in the scope of it's container.*/
    private String name;

    /* Identifier */
    private String id;

    private LocalDateTime created;

    private String userDetails;

    /*The short description of this entity*/
    private String shortDesc;

    /*The long description of this entity*/
    private String longDesc;

    protected NamedBO() {
    }

    protected NamedBO(String name) {
        this.name = name;
    }

}
