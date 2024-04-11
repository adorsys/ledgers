/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.hash;

public interface HashItem<T> {

    String getAlg();
    T getItem();
}
