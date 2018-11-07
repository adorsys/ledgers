package de.adorsys.ledgers.deposit.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;

@Service
public class DepositAccountServiceImpl extends AbstractServiceImpl implements DepositAccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepositAccountServiceImpl.class);

    private DepositAccountRepository depositAccountRepository;
    private LedgerService ledgerService;
    private DepositAccountConfigService depositAccountConfigService;
    private DepositAccountMapper depositAccountMapper;

    public DepositAccountServiceImpl(DepositAccountConfigService depositAccountConfigService, LedgerService ledgerService, DepositAccountRepository depositAccountRepository, LedgerService ledgerService1, DepositAccountConfigService depositAccountConfigService1, DepositAccountMapper depositAccountMapper) {
        super(depositAccountConfigService, ledgerService);
        this.depositAccountRepository = depositAccountRepository;
        this.ledgerService = ledgerService1;
        this.depositAccountConfigService = depositAccountConfigService1;
        this.depositAccountMapper = depositAccountMapper;
    }

    @Override
    public DepositAccountBO createDepositAccount(DepositAccountBO depositAccountBO) throws DepositAccountNotFoundException {
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);

        LedgerBO ledgerBO = loadLedger();

        String depositParentAccountNbr = depositAccountConfigService.getDepositParentAccount();
        LedgerAccountBO depositParentAccount = new LedgerAccountBO();
        depositParentAccount.setLedger(ledgerBO);
        depositParentAccount.setName(depositParentAccountNbr);

        // Business logic
        LedgerAccountBO ledgerAccount = new LedgerAccountBO();
        ledgerAccount.setParent(depositParentAccount);
        ledgerAccount.setName(depositAccount.getIban());


        try {
            ledgerService.newLedgerAccount(ledgerAccount);
        } catch (LedgerAccountNotFoundException | LedgerNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new DepositAccountNotFoundException(e.getMessage(), e);
        }

        DepositAccount da = createDepositAccountObj(depositAccount);

        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    private DepositAccount createDepositAccountObj(DepositAccount depositAccount) {
        DepositAccount da = new DepositAccount();
        da.setId(Ids.id());
        da.setAccountStatus(depositAccount.getAccountStatus());
        da.setAccountType(depositAccount.getAccountType());
        da.setCurrency(depositAccount.getCurrency());
        da.setDetails(depositAccount.getDetails());
        da.setIban(depositAccount.getIban());
        da.setLinkedAccounts(depositAccount.getLinkedAccounts());
        da.setMsisdn(depositAccount.getMsisdn());
        da.setName(depositAccount.getName());
        da.setProduct(depositAccount.getProduct());
        da.setUsageType(depositAccount.getUsageType());
        return da;
    }

    @Override
    public DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException {
        return depositAccountRepository.findById(accountId)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(accountId));
    }

    @Override
    public DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException {
        return depositAccountRepository.findByIban(iban)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(iban));
    }
}
