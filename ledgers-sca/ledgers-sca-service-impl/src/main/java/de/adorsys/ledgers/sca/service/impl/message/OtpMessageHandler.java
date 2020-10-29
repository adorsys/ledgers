package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.service.SCAMethodType;

public interface OtpMessageHandler extends SCAMethodType {

    String getMessage(AuthCodeDataBO data, String tan);
}
