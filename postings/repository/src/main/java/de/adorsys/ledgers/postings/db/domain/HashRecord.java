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

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class HashRecord {

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

	public HashRecord() {
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHashAlg() {
		return hashAlg;
	}

	public void setHashAlg(String hashAlg) {
		this.hashAlg = hashAlg;
	}

	public String getAntecedentId() {
		return antecedentId;
	}

	public String getAntecedentHash() {
		return antecedentHash;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((antecedentHash == null) ? 0 : antecedentHash.hashCode());
		result = prime * result + ((antecedentId == null) ? 0 : antecedentId.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((hashAlg == null) ? 0 : hashAlg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashRecord other = (HashRecord) obj;
		if (antecedentHash == null) {
			if (other.antecedentHash != null)
				return false;
		} else if (!antecedentHash.equals(other.antecedentHash))
			return false;
		if (antecedentId == null) {
			if (other.antecedentId != null)
				return false;
		} else if (!antecedentId.equals(other.antecedentId))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (hashAlg == null) {
			if (other.hashAlg != null)
				return false;
		} else if (!hashAlg.equals(other.hashAlg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HashRecord [antecedentId=" + antecedentId + ", antecedentHash=" + antecedentHash + ", hash=" + hash
				+ ", hashAlg=" + hashAlg + "]";
	}
    
    
}
