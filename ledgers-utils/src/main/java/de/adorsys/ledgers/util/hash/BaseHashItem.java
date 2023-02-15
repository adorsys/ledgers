/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.hash;

public class BaseHashItem<T> implements HashItem<T> {

    private T hashItem;

    public BaseHashItem(T hashItem) {
        this.hashItem = hashItem;
    }

    @Override
    public String getAlg() {
        return HashGenerator.DEFAULT_HASH_ALG;
    }

    @Override
    public T getItem() {
        return hashItem;
    }

    @Override
    public String toString() {
        return "BaseHashItem{" +
                       "hashItem=" + hashItem +
                       '}';
    }
}
