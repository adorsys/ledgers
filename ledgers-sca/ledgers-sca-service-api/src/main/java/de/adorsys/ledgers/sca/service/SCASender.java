/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service;

import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;

public interface SCASender<T extends ScaMessage> extends SCAMethodType {

    boolean send(T message);
}