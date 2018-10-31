package de.adorsys.ledgers.um.api.domain;


import de.adorsys.ledgers.um.db.domain.ScaMethodType;
import org.jetbrains.annotations.NotNull;

public class ScaUserDataBO {

    private String id;

    @NotNull
    private ScaMethodType scaMethod;

    @NotNull
    private String methodValue;

    @NotNull
    private UserBO user;

    public ScaUserDataBO() {
    }

    public ScaUserDataBO(@NotNull UserBO user,
                         @NotNull ScaMethodType scaMethod,
                         @NotNull String methodValue) {
        this.user = user;
        this.scaMethod = scaMethod;
        this.methodValue = methodValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScaMethodType getScaMethod() {
        return scaMethod;
    }

    public void setScaMethod(ScaMethodType scaMethod) {
        this.scaMethod = scaMethod;
    }

    public String getMethodValue() {
        return methodValue;
    }

    public void setMethodValue(String methodValue) {
        this.methodValue = methodValue;
    }

    public UserBO getUser() {
        return user;
    }

    public void setUser(UserBO user) {
        this.user = user;
    }

}
