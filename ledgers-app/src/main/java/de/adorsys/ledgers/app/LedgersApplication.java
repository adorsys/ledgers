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

package de.adorsys.ledgers.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.adorsys.ledgers.deposit.api.service.EnableDepositAccountService;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.impl.EnableLedgersMiddlewareService;
import de.adorsys.ledgers.middleware.rest.EnableLedgersMiddlewareRest;
import de.adorsys.ledgers.mockbank.simple.service.EnableMockBankSimple;
import de.adorsys.ledgers.mockbank.simple.service.MockBankSimpleInitService;
import de.adorsys.ledgers.postings.impl.EnablePostingService;
import de.adorsys.ledgers.sca.service.EnableSCAService;
import de.adorsys.ledgers.um.impl.EnableUserManagementService;

@EnableScheduling
@SpringBootApplication
@EnableUserManagementService
@EnableSCAService
@EnablePostingService
@EnableDepositAccountService
@EnableLedgersMiddlewareService
@EnableLedgersMiddlewareRest
@EnableMockBankSimple
@EnableFeignClients(basePackageClasses=AccountRestClient.class)
public class LedgersApplication implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private ApplicationContext context;
	
	@Value("${ledgers.mockbank.data.load}")
	private boolean loadMockData;
	
    public static void main(String[] args) {
        new SpringApplicationBuilder(LedgersApplication.class).run(args);
    }

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		if(loadMockData) {
			context.getBean(MockBankSimpleInitService.class).runInit();
		}
	}

}
