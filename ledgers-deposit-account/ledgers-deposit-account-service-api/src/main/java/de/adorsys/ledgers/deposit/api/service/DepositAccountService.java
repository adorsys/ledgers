package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

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

    Optional<DepositAccountBO> getOptionalAccountByIbanAndCurrency(String iban, Currency currency);

    DepositAccountBO createNewAccount(DepositAccountBO depositAccountBO, String userName, String branch);

    DepositAccountDetailsBO getAccountDetailsByIbanAndCurrency(String iban, Currency currency, LocalDateTime refTime, boolean withBalances);

    DepositAccountDetailsBO getAccountDetailsById(String accountId, LocalDateTime refTime, boolean withBalances);

    TransactionDetailsBO getTransactionById(String accountId, String transactionId);

    List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo);

    Page<TransactionDetailsBO> getTransactionsByDatesPaged(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO);

    String readIbanById(String id);

    List<DepositAccountBO> findByAccountNumberPrefix(String accountNumberPrefix);

    List<DepositAccountDetailsBO> findDetailsByBranch(String branch);

    Page<DepositAccountDetailsBO> findDetailsByBranchPaged(String branch, String queryParam, Pageable pageable);

    void deleteTransactions(String iban);

    void deleteBranch(String branchId);

    DepositAccountDetailsBO getDetailsByIban(String iban, LocalDateTime refTime, boolean withBalances);
}
