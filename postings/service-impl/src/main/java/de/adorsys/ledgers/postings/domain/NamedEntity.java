package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@ToString(callSuper=true)
public abstract class NamedEntity extends BaseEntity {
	
	/*Business identifier.  Always unique in a certain scope. Generally in the scope of it's container.*/
	@Column(nullable=false)
	@Getter
	private String name;

	public NamedEntity(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name) {
		super(id, created, user, shortDesc, longDesc);
		this.name = name;
	}
}
