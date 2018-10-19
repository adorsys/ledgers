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

package de.adorsys.ledgers.deposit.domain;

import java.time.LocalDate;
import java.util.List;

public class BulkPayment extends BasePayment {
    /*
     * If this element equals "true", the PSU prefers only one booking entry. If this element equals "false", the PSU prefers individual booking of all contained individual transactions. The ASPSP will follow this preference according to contracts agreed on with the PSU.
     */
    private Boolean batchBookingPreferred;

    private LocalDate requestedExecutionDate;

    List<SinglePayment> payments;

	public Boolean getBatchBookingPreferred() {
		return batchBookingPreferred;
	}

	public void setBatchBookingPreferred(Boolean batchBookingPreferred) {
		this.batchBookingPreferred = batchBookingPreferred;
	}

	public LocalDate getRequestedExecutionDate() {
		return requestedExecutionDate;
	}

	public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
		this.requestedExecutionDate = requestedExecutionDate;
	}

	public List<SinglePayment> getPayments() {
		return payments;
	}

	public void setPayments(List<SinglePayment> payments) {
		this.payments = payments;
	}

}
