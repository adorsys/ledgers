package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepositAccountRepository extends PagingAndSortingRepository<DepositAccount, String> {
    List<DepositAccount> findByBranch(String branch);

    Page<DepositAccount> findByBranchAndIbanContaining(String branch, String queryParam, Pageable pageable);

    Page<DepositAccount> findByBranchInAndIbanContainingAndBlockedInAndSystemBlockedFalse(Collection<String> branchIds, String iban, List<Boolean> blocked, Pageable pageable);

    Optional<DepositAccount> findByIbanAndCurrency(String iban, String currency);

    List<DepositAccount> findAllByIbanAndCurrencyContaining(String iban, String currency);

    @Query("update DepositAccount a set a.systemBlocked=?2 where a.branch=?1")
    void updateSystemBlockedStatus(String userId, boolean lockStatusToSet);

    @Modifying
    @Query("update DepositAccount a set a.blocked=?2 where a.branch=?1")
    void updateBlockedStatus(String userId, boolean lockStatusToSet);

    @Query("update DepositAccount da set da.systemBlocked=?2 where da.id in ?1")
    void updateSystemBlockedStatus(Set<String> accountIds, boolean lockStatusToSet);

    @Modifying
    @Query("update DepositAccount da set da.blocked=?2 where da.id in ?1")
    void updateBlockedStatus(Set<String> accountIds, boolean lockStatusToSet);
}
