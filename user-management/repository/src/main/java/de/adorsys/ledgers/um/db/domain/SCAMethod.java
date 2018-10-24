package de.adorsys.ledgers.um.db.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "sca_methods")
public class SCAMethod {

    @Id
    @Column(name = "sca_id")
    private String id;

    @NotNull
    @Column(name = "method_type", nullable = false)
    private String methodType;

    @NotNull
    @Column(name = "method_value", nullable = false)
    private String methodValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    // TODO: clarify if we need user setter in SCAMethod
    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

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
