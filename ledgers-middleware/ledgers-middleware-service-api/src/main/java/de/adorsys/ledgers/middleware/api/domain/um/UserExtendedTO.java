package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.Data;

@Data
public class UserExtendedTO extends UserTO {
    private String branchLogin;
}