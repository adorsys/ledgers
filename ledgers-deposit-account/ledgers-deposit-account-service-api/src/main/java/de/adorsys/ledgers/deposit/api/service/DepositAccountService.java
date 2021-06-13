package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.FundsConfirmationRequestBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public interface DepositAccountService {

    /**
     * Retrieve accounts by IBAN and Currency(partial/empty)
     *
     * @param iban     mandatory IBAN
     * @param currency optional or partial Currency
     * @return List of accounts as if currency not specified can be many
     */
    List<DepositAccountBO> getAccountsByIbanAndParamCurrency(String iban, String currency);

    /**
     * Retrieve account by IBAN and Currency
     *
     * @param iban     IBAN
     * @param currency Currency
     * @return DepositAccount
     */
    DepositAccountBO getAccountByIbanAndCurrency(String iban, Currency currency);

    /**
     * Retrieve account by accountId
     *
     * @param accountId the account id
     * @return DepositAccount
     */
    DepositAccountBO getAccountById(String accountId);

    Optional<DepositAccountBO> getOptionalAccountByIbanAndCurrency(String iban, Currency currency);

    Optional<DepositAccountBO> getOptionalAccountById(String accountId);

    DepositAccountBO createNewAccount(DepositAccountBO depositAccountBO, String userName, String branch);

    DepositAccountDetailsBO getAccountDetailsByIbanAndCurrency(String iban, Currency currency, LocalDateTime refTime, boolean withBalances);

    DepositAccountDetailsBO getAccountDetailsById(String accountId, LocalDateTime refTime, boolean withBalances);

    TransactionDetailsBO getTransactionById(String accountId, String transactionId);

    List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo);

    Page<TransactionDetailsBO> getTransactionsByDatesPaged(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO);

    String readIbanById(String id);

    List<DepositAccountDetailsBO> findDetailsByBranch(String branch);

    Page<DepositAccountDetailsBO> findDetailsByBranchPaged(String branch, String queryParam, boolean withBalance, Pageable pageable);

    void changeAccountsBlockedStatus(String userId, boolean isSystemBlock, boolean lockStatusToSet);

    Page<DepositAccountBO> findByBranchIdsAndMultipleParams(Collection<String> branchIds, String iban, Boolean blocked, Pageable pageable);

    void changeAccountsBlockedStatus(Set<String> accountIds, boolean systemBlock, boolean lockStatusToSet);

    void changeCreditLimit(String accountId, BigDecimal creditLimit);
}
