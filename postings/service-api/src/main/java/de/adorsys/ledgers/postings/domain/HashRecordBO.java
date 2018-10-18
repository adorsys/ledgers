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

package de.adorsys.ledgers.postings.domain;

public abstract class HashRecordBO {

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

	public String getAntecedentId() {
		return antecedentId;
	}

	public void setAntecedentId(String antecedentId) {
		this.antecedentId = antecedentId;
	}

	public String getAntecedentHash() {
		return antecedentHash;
	}

	public void setAntecedentHash(String antecedentHash) {
		this.antecedentHash = antecedentHash;
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
    
}
