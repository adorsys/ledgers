package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaUserDataTO {
    private String id;
    @NotNull
    private ScaMethodTypeTO scaMethod;
    @NotNull
    private String methodValue;
    @NotNull
    @JsonIgnore
    private UserTO user;
}
