package de.adorsys.ledgers.postings.api.service;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;

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
     * @throws PostingNotFoundException
     */
    PostingBO newPosting(PostingBO posting) throws PostingNotFoundException, LedgerNotFoundException, LedgerAccountNotFoundException;

    /**
     * Listing all postings associated with this operation id.
     *
     * @param oprId
     * @return
     */
    List<PostingBO> findPostingsByOperationId(String oprId);

    /**
     * Compute the balance of a ledger account.
     *
     * @param ledgerAccount : the ledger account for which the balance shal be computed.
     * @param refTime       the time at which this balance has to be computed.
     * @return
     * @throws LedgerAccountNotFoundException
     */
    PostingBO balanceTx(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException;
}
