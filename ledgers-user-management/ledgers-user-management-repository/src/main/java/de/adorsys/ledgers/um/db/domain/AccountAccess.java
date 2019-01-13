package de.adorsys.ledgers.um.db.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.jetbrains.annotations.NotNull;

import de.adorsys.ledgers.util.Ids;

/*
*
*
* */

//Todo clarify unique constrains iban & access_type
@Entity
@Table(name = "account_accesses")
public class AccountAccess {

    @Id
    @Column(name = "account_access_id")
    private String id;

    @NotNull
    @Column(nullable = false)
    private String iban;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType= AccessType.OWNER;
    
    @PrePersist
    public void prePersist() {
    	if(id==null) {
    		id = Ids.id();
    	}
    }

    public String getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

	public void setId(String id) {
		this.id = id;
	}
    
}
