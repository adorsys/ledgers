package de.adorsys.ledgers.um.api.domain;


import org.jetbrains.annotations.NotNull;

public class ScaUserDataBO {

    private String id;

    @NotNull
    private ScaMethodTypeBO scaMethod;

    @NotNull
    private String methodValue;

    @NotNull
    private UserBO user;

    public ScaUserDataBO() {
    }

    public ScaUserDataBO(@NotNull UserBO user,
                         @NotNull ScaMethodTypeBO scaMethod,
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

    public ScaMethodTypeBO getScaMethod() {
        return scaMethod;
    }

    public void setScaMethod(ScaMethodTypeBO scaMethod) {
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
