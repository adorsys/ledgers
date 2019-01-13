package de.adorsys.ledgers.um.db.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import de.adorsys.ledgers.util.Ids;

@Entity
@Table(name = "sca_data")
public class ScaUserDataEntity {

    @Id
    @Column(name = "sca_id")
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScaMethodTypeEntity scaMethod;

    @Column(nullable = false)
    private String methodValue;
    
    @PrePersist
    public void prePersist() {
    	if(id==null) {
    		id = Ids.id();
    	}
    }

    public String getId() {
        return id;
    }

    public String getMethodValue() {
        return methodValue;
    }

    public void setMethodValue(String methodValue) {
        this.methodValue = methodValue;
    }

    public ScaMethodTypeEntity getScaMethod() {
        return scaMethod;
    }

    public void setScaMethod(ScaMethodTypeEntity scaMethod) {
        this.scaMethod = scaMethod;
    }

	public void setId(String id) {
		this.id = id;
	}
    
}
