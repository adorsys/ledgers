/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
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


    @Scheduled(cron = "${ledgers.sca.authCode.expiration.cron}")
    public void checkOperationExpiration(){
        log.info("Start job of processing expired operations");
        scaOperationService.processExpiredOperations();
        log.info("End job of processing expired operations");
    }
}
