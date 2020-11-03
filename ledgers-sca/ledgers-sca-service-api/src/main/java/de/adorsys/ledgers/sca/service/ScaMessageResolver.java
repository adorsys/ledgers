package de.adorsys.ledgers.sca.service;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;

public interface ScaMessageResolver {

    String resolveMessage(AuthCodeDataBO data, String tan, ScaMethodTypeBO methodType);
}
