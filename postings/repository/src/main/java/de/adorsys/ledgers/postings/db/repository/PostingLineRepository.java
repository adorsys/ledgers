package de.adorsys.ledgers.postings.db.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.*;

public interface PostingLineRepository extends PagingAndSortingRepository<PostingLine, String> {
	
	/**
	 * Retrieve the last closing balance before reference time.
	 * 
	 * @param ledgerAccount
	 * @param ldgClsng
	 * @param pstStatus
	 * @param refTime
	 * @return
	 */
	PostingLine findFirstByAccountAndPstTypeAndPstStatusAndPstTimeLessThanEqualOrderByPstTimeDesc(LedgerAccount ledgerAccount,
			PostingType ldgClsng, PostingStatus pstStatus, LocalDateTime refTime);

	List<PostingLine> findByAccountAndPstTypeInAndPstStatusAndPstTimeGreaterThanAndPstTimeLessThanEqual(
            LedgerAccount ledgerAccount, List<PostingType> txTypes, PostingStatus pstStatus, LocalDateTime pstTime,
            LocalDateTime refTime);

	@Query("SELECT SUM(l.debitAmount), SUM(l.creditAmount) FROM PostingLine l WHERE l.account= :account AND l.pstType IN :txTypes AND l.pstStatus=:pstStatus AND l.pstTime>:lastClosing AND l.pstTime<=:refTime")
	List<BigDecimal> computeBalance(LedgerAccount account, List<PostingType> txTypes, PostingStatus pstStatus, LocalDateTime lastClosing,
                                    LocalDateTime refTime);
}
