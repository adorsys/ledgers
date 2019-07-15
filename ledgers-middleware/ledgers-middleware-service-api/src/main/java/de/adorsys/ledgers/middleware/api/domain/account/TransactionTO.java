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

package de.adorsys.ledgers.middleware.api.domain.account;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTO {
    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private String mandateId;
    private String checkId;
    private String creditorId;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate bookingDate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate valueDate;
    private AmountTO amount;
    private List<ExchangeRateTO> exchangeRate;
    private String creditorName;
    private AccountReferenceTO creditorAccount;
    private String ultimateCreditor;
    private String debtorName;
    private AccountReferenceTO debtorAccount;
    private String ultimateDebtor;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String purposeCode;
    private String bankTransactionCode;
    private String proprietaryBankTransactionCode;
}
