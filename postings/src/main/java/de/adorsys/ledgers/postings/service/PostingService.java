package de.adorsys.ledgers.postings.service;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.Posting;

public interface PostingService {

    /**
     * Creates a new Posting.
     * <p>
     * - If there is another posting with the same operation id
     * - The new posting can only be stored is the oldest is not part of a closed accounting period.
     * - A posting time can not be older than a closed accounting period.
     *
     * @param posting
     * @return
     * @throws NotFoundException
     */
    Posting newPosting(Posting posting) throws NotFoundException;

    /**
     * Listing all postings associated with this operation id.
     *
     * @param oprId
     * @return
     */
    List<Posting> findPostingsByOperationId(String oprId);

    /**
     * Compute the balance of a ledger account.
     *
     * @param ledgerAccount : the ledger account for which the balance shal be computed.
     * @param refTime       the time at which this balance has to be computed.
     * @return
     * @throws NotFoundException
     */
    Posting balanceTx(LedgerAccount ledgerAccount, LocalDateTime refTime) throws NotFoundException;
}
