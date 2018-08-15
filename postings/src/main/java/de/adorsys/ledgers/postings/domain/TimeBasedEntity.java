package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes an entity that exists only between a validFrom date and a valid to date.
 * 
 * 
 * @author fpo
 *
 */
@MappedSuperclass
@Getter
@ToString(callSuper=true)
@NoArgsConstructor
public abstract class TimeBasedEntity extends BaseEntity {

	/*Business identifier. Unique in the scope of it's validity. Name + from is unique.*/
	@Column(nullable=false)
	private String name;
	
	/* Valid From, by posting date */
	@Column(nullable=false)
	private LocalDateTime validFrom;

	/*Valid to by posting date. After this date, the ledger entity can not be used.*/
	@Column(nullable=false)
	@Setter
	private LocalDateTime validTo = LocalDateTime.of(2199, 01, 01, 0, 0, 0, 0);

	public TimeBasedEntity(String id, String name, LocalDateTime created, String user, String desc,
			LocalDateTime validFrom) {
		super(id, created, user, desc);
		this.name = name;
		this.validFrom = validFrom;
	}
}
