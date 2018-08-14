package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The existence or value of a ledger entity is always considered relative to
 * the posting date.
 * 
 * When a book is closed, modification on ledger entities must lead to the
 * creation of new entities.
 * 
 * @author fpo
 *
 */
@MappedSuperclass
@Getter
@ToString
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class LedgerEntity {
	
	public LedgerEntity(String id, String name, LocalDateTime validFrom, LocalDateTime created, String user) {
		this.id = id;
		this.name = name;
		this.validFrom = validFrom;
		this.created = created;
		this.user = user;
	}

	/* Identifier */
	@Id
	private String id;
	
	/*Business identifier. Unique in the scope of it's validity. Name + from is unique.*/
	@Column(nullable=false)
	private String name;

	/* Valid From, by posting date */
	@Column(nullable=false)
	private LocalDateTime validFrom;

	@CreatedDate
	@Column(nullable=false)
	private LocalDateTime created;

	@Column(nullable=false)
	private String user;
	
	/*Valid to by posting date. After this date, the ledger entity can not be used.*/
	@Column(nullable=false)
	@Setter
	private LocalDateTime validTo = LocalDateTime.of(2199, 01, 01, 0, 0, 0, 0);
	
	/*The description of this entity*/
	@Setter
	private String desc;
}
