package de.adorsys.ledgers.cleanup.repository;

import de.adorsys.ledgers.cleanup.exception.CleanupModuleException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositAccountCleanupRepositoryImplTest {

    private static final String BRANCH = "branch";
    private static final String USER_ID = "user-id";
    private static final String ACCOUNT_ID = "account-id";
    private static final String POSTING_ID = "posting-id";
    private static final String QUERY = "query";

    @InjectMocks
    private DepositAccountCleanupRepositoryImpl repository;

    @Mock
    private EntityManager entityManager;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private Resource resource;
    @Mock
    private Query nativeQuery;

    private InputStream targetStream;

    @BeforeEach
    void setUp() {
        targetStream = IOUtils.toInputStream(QUERY, StandardCharsets.UTF_8);
    }

    @Test
    void deleteBranch() throws CleanupModuleException, IOException {
        when(resourceLoader.getResource("classpath:sql/deleteBranch.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(targetStream);
        when(entityManager.createNativeQuery(QUERY)).thenReturn(nativeQuery);

        repository.deleteBranch(BRANCH);

        verify(nativeQuery, times(1)).setParameter(1, BRANCH);
        verify(nativeQuery, times(1)).executeUpdate();
    }

    @Test
    void deleteBranch_error() throws IOException {
        when(resourceLoader.getResource("classpath:sql/deleteBranch.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(IOException.class);

        assertThrows(CleanupModuleException.class, () -> repository.deleteBranch(BRANCH));
        verifyNoInteractions(entityManager, nativeQuery);
    }

    @Test
    void deleteUser() throws CleanupModuleException, IOException {
        when(resourceLoader.getResource("classpath:sql/deleteUser.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(targetStream);
        when(entityManager.createNativeQuery(QUERY)).thenReturn(nativeQuery);

        repository.deleteUser(USER_ID);

        verify(nativeQuery, times(1)).setParameter(1, USER_ID);
        verify(nativeQuery, times(1)).executeUpdate();
    }

    @Test
    void deleteUser_error() throws IOException {
        when(resourceLoader.getResource("classpath:sql/deleteUser.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(IOException.class);

        assertThrows(CleanupModuleException.class, () -> repository.deleteUser(USER_ID));
        verifyNoInteractions(entityManager, nativeQuery);
    }

    @Test
    void deleteAccount() throws CleanupModuleException, IOException {
        when(resourceLoader.getResource("classpath:sql/deleteAccount.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(targetStream);
        when(entityManager.createNativeQuery(QUERY)).thenReturn(nativeQuery);

        repository.deleteAccount(ACCOUNT_ID);

        verify(nativeQuery, times(1)).setParameter(1, ACCOUNT_ID);
        verify(nativeQuery, times(1)).executeUpdate();
    }

    @Test
    void deleteAccount_error() throws IOException {
        when(resourceLoader.getResource("classpath:sql/deleteAccount.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(IOException.class);

        assertThrows(CleanupModuleException.class, () -> repository.deleteAccount(ACCOUNT_ID));
        verifyNoInteractions(entityManager, nativeQuery);
    }

    @Test
    void deletePostings() throws CleanupModuleException, IOException {
        when(resourceLoader.getResource("classpath:sql/deletePostings.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(targetStream);
        when(entityManager.createNativeQuery(QUERY)).thenReturn(nativeQuery);

        repository.deletePostings(POSTING_ID);

        verify(nativeQuery, times(1)).setParameter(1, POSTING_ID);
        verify(nativeQuery, times(1)).executeUpdate();
    }

    @Test
    void deletePostings_error() throws IOException {
        when(resourceLoader.getResource("classpath:sql/deletePostings.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(IOException.class);

        assertThrows(CleanupModuleException.class, () -> repository.deletePostings(POSTING_ID));
        verifyNoInteractions(entityManager, nativeQuery);
    }

    @Test
    void rollBackBranch() throws CleanupModuleException, IOException {
        LocalDateTime now = LocalDateTime.now();
        ArgumentCaptor<Integer> paramKeyCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<LocalDateTime> paramValueCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        when(resourceLoader.getResource("classpath:sql/rollBackBranch.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(targetStream);
        when(entityManager.createNativeQuery(QUERY)).thenReturn(nativeQuery);

        repository.rollBackBranch(BRANCH, now);

        verify(nativeQuery, times(2)).setParameter(paramKeyCaptor.capture(), paramValueCaptor.capture());
        verify(nativeQuery, times(1)).executeUpdate();

        assertTrue(paramKeyCaptor.getAllValues().contains(1));
        assertTrue(paramKeyCaptor.getAllValues().contains(2));

        assertTrue(paramValueCaptor.getAllValues().contains(BRANCH));
        assertTrue(paramValueCaptor.getAllValues().contains(now));
    }

    @Test
    void rollBackBranch_error() throws IOException {
        when(resourceLoader.getResource("classpath:sql/rollBackBranch.sql")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(IOException.class);

        assertThrows(CleanupModuleException.class, () -> repository.rollBackBranch(BRANCH, LocalDateTime.now()));
        verifyNoInteractions(entityManager, nativeQuery);
    }
}