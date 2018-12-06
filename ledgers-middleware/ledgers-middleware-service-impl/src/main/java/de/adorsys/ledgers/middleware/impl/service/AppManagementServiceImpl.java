package de.adorsys.ledgers.middleware.impl.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;

@Service
public class AppManagementServiceImpl implements AppManagementService {

    private final DepositAccountInitService depositAccountInitService;

    public AppManagementServiceImpl(DepositAccountInitService depositAccountInitService) {
		this.depositAccountInitService = depositAccountInitService;
	}

	@Override
	public void initApp() throws IOException {
		// Init deposit account config  data.
		depositAccountInitService.initConfigData();
	}

}
