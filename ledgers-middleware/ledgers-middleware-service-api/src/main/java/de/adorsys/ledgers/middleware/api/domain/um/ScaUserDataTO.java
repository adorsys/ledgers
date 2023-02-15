/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
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
