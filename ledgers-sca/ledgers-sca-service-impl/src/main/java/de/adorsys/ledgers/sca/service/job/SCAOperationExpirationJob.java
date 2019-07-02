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

package de.adorsys.ledgers.sca.service.job;

import de.adorsys.ledgers.sca.service.SCAOperationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SCAOperationExpirationJob {
    private final SCAOperationService scaOperationService;

    public SCAOperationExpirationJob(SCAOperationService scaOperationService) {
        this.scaOperationService = scaOperationService;
    }


    @Scheduled(cron = "${sca.authCode.expiration.cron}")
    public void checkOperationExpiration(){
        log.info("Start job of processing expired operations");
        scaOperationService.processExpiredOperations();
        log.info("End job of processing expired operations");
    }
}
