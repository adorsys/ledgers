package de.adorsys.ledgers.um.db.domain;

import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "sca_methods")
public class ScaUserData {

    @Id
    @Column(name = "sca_id")
    private String id;

    @NotNull
    @Column(nullable = false)
    private ScaMethodType scaMethod;

    @NotNull
    @Column(nullable = false)
    private String methodValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "user_id", nullable = false, updatable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
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
