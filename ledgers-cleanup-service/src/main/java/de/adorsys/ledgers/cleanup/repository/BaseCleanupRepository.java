package de.adorsys.ledgers.cleanup.repository;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ResourceLoader;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
