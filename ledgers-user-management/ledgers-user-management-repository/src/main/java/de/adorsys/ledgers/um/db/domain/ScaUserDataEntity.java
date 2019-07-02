package de.adorsys.ledgers.um.db.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import de.adorsys.ledgers.util.Ids;
import lombok.Data;

@Entity
@Table(name = "sca_data")
@Data
public class ScaUserDataEntity {

    @Id
    @Column(name = "sca_id")
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScaMethodType scaMethod;

    @Column(nullable = false)
    private String methodValue;
    
    @PrePersist
    public void prePersist() {
    	if(id==null) {
    		id = Ids.id();
    	}
    }
}
