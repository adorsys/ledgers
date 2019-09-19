package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LedgerAccountRepository extends NamedEntityRepository<LedgerAccount> {

    Optional<LedgerAccount> findOptionalByLedgerAndName(Ledger ledger, String name);

    @Query(value = "select distinct a from  LedgerAccount a where a.ledger=?2 and a.name in ?1")
    List<LedgerAccount> getAccountsByIbans(Set<String> ibans, Ledger ledger);

    @Query(value = "select distinct a from LedgerAccount  a where a.id in ?1")
    List<LedgerAccount> getAccountsByIds(List<String> ids);
}
