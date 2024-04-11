/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.ledgers.util.hash.HashItem;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@MappedSuperclass
public abstract class HashRecord  implements HashItem<HashRecord> {

    /* The antecedent identifier. Use for hash chaining */
    protected String antecedentId;

    protected String antecedentHash;

    /*
     * The hash value of this posting. If is used by the system for integrity
     * check. A posting is never modified.
     *
     * Aggregation of the UTF-8 String value of all fields by field name in
     * alphabetical order.
     */
    protected String hash;

    private String hashAlg;

    @Override
    @JsonIgnore
    public String getAlg() {
        return hashAlg;
    }

    @Override
    @JsonIgnore
    public HashRecord getItem() {
        return this;
    }
}
