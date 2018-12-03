package de.adorsys.ledgers.um.db.domain;

import javax.persistence.*;

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
    @Enumerated(EnumType.STRING)
    private ScaMethodTypeEntity scaMethod;

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
