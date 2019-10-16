package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

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
     */
    PostingBO newPosting(PostingBO posting);

    /**
     * Listing all postings associated with this operation id.
     *
     * @param oprId
     * @return
     */
    List<PostingBO> findPostingsByOperationId(String oprId);

    List<PostingLineBO> findPostingsByDates(LedgerAccountBO ledgerAccount, LocalDateTime dateFrom, LocalDateTime dateTo);

    Page<PostingLineBO> findPostingsByDatesPaged(LedgerAccountBO ledgerAccount, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    PostingLineBO findPostingLineById(LedgerAccountBO ledgerAccount, String transactionId);
}
