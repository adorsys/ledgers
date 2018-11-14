package de.adorsys.ledgers.middleware.service.domain.um;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class ScaUserDataTO {
    private String id;

    @NotNull
    private ScaMethodTypeTO scaMethod;

    @NotNull
    private String methodValue;

    @NotNull
    private UserTO user;

    public ScaUserDataTO() {
    }

    public ScaUserDataTO(@NotNull UserTO user,
                         @NotNull ScaMethodTypeTO scaMethod,
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

    public ScaMethodTypeTO getScaMethod() {
        return scaMethod;
    }

    public void setScaMethod(ScaMethodTypeTO scaMethod) {
        this.scaMethod = scaMethod;
    }

    public String getMethodValue() {
        return methodValue;
    }

    public void setMethodValue(String methodValue) {
        this.methodValue = methodValue;
    }

    public UserTO getUser() {
        return user;
    }

    public void setUser(UserTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ScaUserDataTO that = (ScaUserDataTO) o;
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
        return "ScaUserDataTO{" +
                       "id='" + id + '\'' +
                       ", scaMethod=" + scaMethod +
                       ", methodValue='" + methodValue + '\'' +
                       ", user=" + user +
                       '}';
    }

}
