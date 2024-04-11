/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.util.random.RandomUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!develop")
public class AuthCodeGeneratorImpl implements AuthCodeGenerator {

    @Override
    public String generate() {
        return RandomUtils.randomString(6, true, true);
    }
}
