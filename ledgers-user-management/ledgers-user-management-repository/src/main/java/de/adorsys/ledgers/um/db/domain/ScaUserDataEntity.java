package de.adorsys.ledgers.um.db.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "sca_data")
public class ScaUserDataEntity {

    @Id
    @Column(name = "sca_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false)
    private ScaMethodType scaMethod;

    @Column(nullable = false)
    private String methodValue;

    public String getId() {
        return id;
    }

    public String getMethodValue() {
        return methodValue;
    }

    public void setMethodValue(String methodValue) {
        this.methodValue = methodValue;
    }

    public ScaMethodType getScaMethod() {
        return scaMethod;
    }

    public void setScaMethod(ScaMethodType scaMethod) {
        this.scaMethod = scaMethod;
    }
}
