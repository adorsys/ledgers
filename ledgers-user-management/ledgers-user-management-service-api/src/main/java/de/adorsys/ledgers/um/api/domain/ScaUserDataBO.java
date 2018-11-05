package de.adorsys.ledgers.um.api.domain;


import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ScaUserDataBO that = (ScaUserDataBO) o;
        return Objects.equals(id, that.id) &&
                       scaMethod == that.scaMethod &&
                       Objects.equals(methodValue, that.methodValue) &&
                       Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scaMethod, methodValue, user);
    }

    @Override
    public String toString() {
        return "ScaUserDataBO{" +
                       "id='" + id + '\'' +
                       ", scaMethod=" + scaMethod +
                       ", methodValue='" + methodValue + '\'' +
                       ", user=" + user +
                       '}';
    }
}
