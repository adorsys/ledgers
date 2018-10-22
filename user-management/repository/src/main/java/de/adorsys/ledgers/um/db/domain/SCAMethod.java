package de.adorsys.ledgers.um.db.domain;

import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "sca_methods", uniqueConstraints = @UniqueConstraint(columnNames={"method_type", "method_value"}))
public class SCAMethod {

    @Id
    @Column(name = "sca_id")
    private String id;

    @NotNull
    @Column(nullable = false)
    private String methodType;

    @NotNull
    @Column(nullable = false)
    private String methodValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getMethodValue() {
        return methodValue;
    }

    public void setMethodValue(String methodValue) {
        this.methodValue = methodValue;
    }
}
