package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

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
@Getter
@Setter
@EqualsAndHashCode
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
	
	/* Identifier */
	@Id
	private String id;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime created;

//	todo: seems this property should be moved from base class
	@Column(name = "user_details",nullable=false)
	private String userDetails;

	//	todo: seems this property should be moved from base class
	/*The short description of this entity*/
	private String shortDesc;

	//	todo: seems this property should be moved from base class
	/*The long description of this entity*/
	private String longDesc;

	public BaseEntity() {
		super();
	}

	public BaseEntity(String id, LocalDateTime created, String userDetails, String shortDesc, String longDesc) {
		super();
		this.id = id;
		this.created = created;
		this.userDetails = userDetails;
		this.shortDesc = shortDesc;
		this.longDesc = longDesc;
	}

}
