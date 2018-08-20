package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
	
	/* Identifier */
	@Id
	private String id;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime created;

	@Column(nullable=false)
	private String user;

	/*The short description of this entity*/
	private String shortDesc;

	/*The long description of this entity*/
	private String longDesc;
	
	public BaseEntity(String id, LocalDateTime created, String user, String shortDesc, String longDesc) {
		super();
		this.id = id;
		this.created = created;
		this.user = user;
		this.shortDesc = shortDesc;
		this.longDesc = longDesc;
	}
}
