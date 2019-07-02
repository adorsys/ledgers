/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.postings.db.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.ledgers.util.hash.HashItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.MappedSuperclass;

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
