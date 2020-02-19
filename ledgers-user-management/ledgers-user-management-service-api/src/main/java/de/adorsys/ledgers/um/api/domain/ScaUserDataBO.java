package de.adorsys.ledgers.um.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.EMAIL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScaUserDataBO {

    private String id;
    private ScaMethodTypeBO scaMethod;
    private String methodValue;
    private boolean usesStaticTan;
    private String staticTan;
    private boolean valid;

    public ScaUserDataBO(
            @NotNull ScaMethodTypeBO scaMethod,
            @NotNull String methodValue) {
        this.scaMethod = scaMethod;
        this.methodValue = methodValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScaUserDataBO that = (ScaUserDataBO) o;
        return scaMethod == that.scaMethod &&
                       Objects.equals(methodValue, that.methodValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaMethod, methodValue);
    }

    @Override
    public String toString() {
        return "ScaUserDataBO [id=" + id + ", scaMethod=" + scaMethod + ", methodValue=" + methodValue + "] [super: "
                       + super.toString() + "]";
    }

    public boolean isEmailValid() {
        return valid;
    }

    public static String checkId(String id) {
        return StringUtils.isBlank(id)
                       ? null
                       : id;
    }

    public static void checkAndUpdateValidity(ScaUserDataBO oldScaData, List<ScaUserDataBO> newScaData) {
        newScaData.stream()
                .filter(n -> n.getId().equals(oldScaData.getId()))
                .filter(n -> n.getScaMethod() == EMAIL)
                .findFirst()
                .ifPresent(n -> {
                    if (!n.getMethodValue().equals(oldScaData.getMethodValue())) {
                        n.setValid(false);
                    }
                });
    }
}