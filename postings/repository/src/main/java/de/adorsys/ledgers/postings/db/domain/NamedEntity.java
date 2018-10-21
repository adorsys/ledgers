package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

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
public abstract class NamedEntity extends BaseEntity {
	
	/*Business identifier.  Always unique in a certain scope. Generally in the scope of it's container.*/
	@Column(nullable=false)
	private String name;

	public NamedEntity(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name) {
		super(id, created, user, shortDesc, longDesc);
		this.name = name;
	}

	public NamedEntity() {
		super();
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedEntity other = (NamedEntity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamedEntity [name=" + name + "] [super: " + super.toString() + "]";
	}
	
}
