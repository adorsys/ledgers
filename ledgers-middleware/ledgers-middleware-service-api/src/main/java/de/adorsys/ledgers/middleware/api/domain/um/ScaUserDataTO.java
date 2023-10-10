/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@JsonIgnoreProperties(value = {"user"}, allowSetters = true)
public class ScaUserDataTO {
    private String id;
    @NotNull
    private ScaMethodTypeTO scaMethod;
    @NotNull
    private String methodValue;
    @NotNull
    private UserTO user;

    private boolean usesStaticTan;
    private String staticTan;
    private boolean decoupled;
    private boolean valid;

    public boolean isDecoupled() {
        return scaMethod.isDecoupled();
    }
}
