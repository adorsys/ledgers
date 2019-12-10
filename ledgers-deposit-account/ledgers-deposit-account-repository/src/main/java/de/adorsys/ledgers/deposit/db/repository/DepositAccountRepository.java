package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface DepositAccountRepository extends PagingAndSortingRepository<DepositAccount, String> {
    List<DepositAccount> findByIbanStartingWith(String iban);  //TODO fix this!

    List<DepositAccount> findByBranch(String branch);

    Page<DepositAccount> findByBranchAndIbanContaining(String branch, String queryParam, Pageable pageable);

    Optional<DepositAccount> findByIbanAndCurrency(String iban, String currency);

    List<DepositAccount> findAllByIbanAndCurrencyContaining(String iban, String currency);
}
