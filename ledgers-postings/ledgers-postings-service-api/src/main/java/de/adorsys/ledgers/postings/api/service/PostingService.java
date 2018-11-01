package de.adorsys.ledgers.postings.api.service;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
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
     * @throws BaseLineException 
     * @throws DoubleEntryAccountingException 
     */
    PostingBO newPosting(PostingBO posting) throws PostingNotFoundException, LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException, DoubleEntryAccountingException;

    /**
     * Listing all postings associated with this operation id.
     *
     * @param oprId
     * @return
     */
    List<PostingBO> findPostingsByOperationId(String oprId);

    /**
     * Compute and store the balance of a ledger account.
     *
     * @param ledgerAccount : the ledger account for which the balance shal be computed.
     * @param refTime       the time at which this balance has to be computed.
     * @return
     * @throws LedgerAccountNotFoundException
     * @throws LedgerNotFoundException
     * @throws BaseLineException 
     * @throws DoubleEntryAccountingException 
     */
    PostingBO balanceTx(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException;

    /**
     * Compute and return the balance of a ledger account whithout storing it.
     * 
     * @param ledgerAccount : the ledger account for which the balance shal be computed.
     * @param refTime       the time at which this balance has to be computed.
     * @return
     * @throws LedgerAccountNotFoundException
     * @throws LedgerNotFoundException
     */
    PostingLineBO computeBalance(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException;
}
