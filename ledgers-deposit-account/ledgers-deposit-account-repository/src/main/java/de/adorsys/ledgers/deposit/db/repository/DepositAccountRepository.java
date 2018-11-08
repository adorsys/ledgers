package de.adorsys.ledgers.deposit.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.deposit.db.domain.DepositAccount;

public interface DepositAccountRepository extends PagingAndSortingRepository<DepositAccount, String> {
	Optional<DepositAccount> findByIban(String iban);

	List<DepositAccount> findByIbanIn(List<String> ibans);
}
