package de.adorsys.ledgers.um.api.domain;

import de.adorsys.ledgers.util.Ids;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationBO {

    private Long id;
    private String token;
    private EmailVerificationStatusBO status;
    private LocalDateTime expiredDateTime;
    private LocalDateTime issuedDateTime;
    private LocalDateTime confirmedDateTime;
    private ScaUserDataBO scaUserData;

    public void createToken() {
        LocalDateTime now = LocalDateTime.now();
        setToken(Ids.id());
        setExpiredDateTime(now.plusWeeks(1));
        setStatus(EmailVerificationStatusBO.PENDING);
        setIssuedDateTime(now);
    }

    public void updateToken() {
        LocalDateTime now = LocalDateTime.now();
        setExpiredDateTime(now.plusWeeks(1));
        setIssuedDateTime(now);
    }

    public boolean isExpired() {
        return getExpiredDateTime().isBefore(LocalDateTime.now());
    }

    public String formatMessage(String message, String basePath, String endpoint, String token, LocalDateTime date) {
        return String.format(message, basePath + endpoint + "?verificationToken=" + token, date.getMonth().toString() + " " + date.getDayOfMonth() + ", " + date.getYear() + " " + date.getHour() + ":" + date.getMinute());
    }
}