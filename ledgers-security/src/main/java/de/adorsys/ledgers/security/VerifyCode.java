package de.adorsys.ledgers.security;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCode {
    private String userId;
    private boolean verified;

    public VerifyCode(String userId) {
        this(userId, true);
    }
}
