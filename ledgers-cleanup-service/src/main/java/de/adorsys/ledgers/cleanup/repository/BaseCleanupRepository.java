/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.cleanup.repository;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ResourceLoader;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BaseCleanupRepository {

    @PersistenceContext
    private final EntityManager entityManager;
    private final ResourceLoader resourceLoader;

    public BaseCleanupRepository(EntityManager entityManager, ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.entityManager = entityManager;
    }

    protected void executeUpdate(String queryFilePath, Map<Integer, Object> params) throws IOException {
        String query = loadQuery(queryFilePath);
        Query nativeQuery = entityManager.createNativeQuery(query);
        params.keySet().forEach(param -> nativeQuery.setParameter(param, params.get(param)));
        nativeQuery.executeUpdate();
    }

    private String loadQuery(String queryFilePath) throws IOException {
        InputStream stream = resourceLoader.getResource("classpath:sql/" + queryFilePath).getInputStream();
        return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }
}
