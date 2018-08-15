package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
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
@NoArgsConstructor
public abstract class NamedEntity extends BaseEntity {
	
	/*Business identifier. Unique in the scope of it's validity. Name + from is unique.*/
	@Column(nullable=false, unique=true)
	@Getter
	private String name;

	public NamedEntity(String id, String name, LocalDateTime created, String user, String desc) {
		super(id, created, user, desc);
		this.name = name;
	}
}
