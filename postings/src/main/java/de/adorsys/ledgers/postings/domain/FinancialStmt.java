package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A financial statement will help draw time lines on ledgers. No changes are allowed 
 * in a ledger as long as the ledger haben closed with a financial statement.
 * 
 * @author fpo
 *
 */
@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FinancialStmt {
	@Id
	private String id;

	/* The user (technically) recording this posting. */
	@Column(nullable = false, updatable = false)
	private String recordUser;

	/* The time of recording of this posting. */
	@CreatedDate
	@Column(nullable = false, updatable = false)
	@Setter
	private LocalDateTime recordTime;
	
	@Column(nullable = false, updatable = false)
	private String ledger;

	/*Documents the time of the posting.*/
	@Column(nullable = false, updatable = false)
	private LocalDateTime pstTime;

	/*Documents the posting holding additional information.*/
	@Column(nullable = false, updatable = false)
	private String pstId;
}
