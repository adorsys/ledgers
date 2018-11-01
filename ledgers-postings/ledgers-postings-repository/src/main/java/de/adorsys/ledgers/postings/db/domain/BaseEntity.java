package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public String getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(String userDetails) {
		this.userDetails = userDetails;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getLongDesc() {
		return longDesc;
	}

	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}

	@Override
	public String toString() {
		return "BaseEntity [id=" + id + ", created=" + created + ", userDetails=" + userDetails + ", shortDesc=" + shortDesc
				+ ", longDesc=" + longDesc + "]";
	}
}
