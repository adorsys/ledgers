package de.adorsys.ledgers.sca.service;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

public interface ScaMessageResolver<T extends ScaMessage> {

    T resolveMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan);
}
