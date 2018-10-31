package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.domain.PostingStatus;
import de.adorsys.ledgers.postings.db.domain.PostingType;

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

	/**
	 * 
	 * 
	 * @param ledgerAccount
	 * @param txTypes
	 * @param pstStatus
	 * @param pstTime
	 * @param refTime
	 * @return
	 */
	List<PostingLine> findByAccountAndPstTypeInAndPstStatusAndPstTimeGreaterThanAndPstTimeLessThanEqual(
			LedgerAccount ledgerAccount, List<PostingType> txTypes, PostingStatus pstStatus, LocalDateTime pstTime,
            LocalDateTime refTime);


	List<PostingLine> findByBaseLineAndPstTimeLessThanEqualOrderByRecordTimeDesc(String baseLine, LocalDateTime refTime);

	List<PostingLine> findByAccountAndPstTimeLessThanEqualOrderByRecordTimeDesc(LedgerAccount account, LocalDateTime refTime);
}
