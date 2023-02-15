/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.repository.ScaUserDataRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScaUserDataServiceImplTest {

    @InjectMocks
    private ScaUserDataServiceImpl scaUserDataService;
    @Mock
    private ScaUserDataRepository scaUserDataRepository;
    @Mock
    private UserConverter userConverter;

    private final ResourceReader reader = YamlReader.getInstance();

    private static final String EMAIL = "google@gmail.com";
    private static final String SCA_ID = "6DwJm-TpResvxLdX3fHpjc";
    private ScaUserDataEntity scaUserDataEntity;
    private ScaUserDataBO scaUserDataBO;

    @BeforeEach
    void setUp() {
        scaUserDataEntity = readScaUserDataEntity();
        scaUserDataBO = readScaUserDataBO();
    }

    @Test
    void findByEmail() {
        // Given
        when(scaUserDataRepository.findByMethodValue(any())).thenReturn(Collections.singletonList(scaUserDataEntity));
        when(userConverter.toScaUserDataBO(any())).thenReturn(scaUserDataBO);

        // When
        ScaUserDataBO scaUserDataBO = scaUserDataService.findByEmail(EMAIL);

        // Then
        assertThat(scaUserDataBO.getMethodValue(), is(EMAIL));
        verify(scaUserDataRepository, times(1)).findByMethodValue(EMAIL);
    }

    @Test
    void findByEmail_scaNotFound() {
        // Given
        when(scaUserDataRepository.findByMethodValue(any())).thenReturn(Collections.EMPTY_LIST);

        // Then
        assertThrows(ScaModuleException.class, () -> scaUserDataService.findByEmail(EMAIL));
    }

    @Test
    void findById() {
        // Given
        when(scaUserDataRepository.findById(any())).thenReturn(Optional.ofNullable(scaUserDataEntity));
        when(userConverter.toScaUserDataBO(any())).thenReturn(scaUserDataBO);

        // When
        ScaUserDataBO scaUserDataBO = scaUserDataService.findById(SCA_ID);

        // Then
        assertThat(scaUserDataBO.getId(), is(SCA_ID));
        verify(scaUserDataRepository, times(1)).findById(SCA_ID);
    }

    @Test
    void findById_scaNotFound() {
        // Given
        when(scaUserDataRepository.findById(any())).thenReturn(Optional.empty());

        // Then
        assertThrows(ScaModuleException.class, () -> scaUserDataService.findById(SCA_ID));
    }

    @Test
    void updateScaUserData() {
        // Given
        when(scaUserDataRepository.save(any())).thenReturn(scaUserDataEntity);
        when(userConverter.toScaUserDataEntity(any())).thenReturn(scaUserDataEntity);

        // When
        scaUserDataService.updateScaUserData(scaUserDataBO);

        // Then
        verify(scaUserDataRepository, times(1)).save(scaUserDataEntity);
    }

    @Test
    void ifScaChangedEmailNotValid() {
        ScaUserDataBO oldScaData = readScaUserDataBO();
        ScaUserDataBO newScaData = readScaUserDataBO();
        newScaData.setMethodValue("changed email");
        newScaData.setValid(true);

        scaUserDataService.ifScaChangedEmailNotValid(Collections.singletonList(oldScaData),
                                                     Collections.singletonList(newScaData));

        assertFalse(newScaData.isValid());
    }

    private ScaUserDataBO readScaUserDataBO() {
        try {
            return reader.getObjectFromResource(getClass(), "sca-user-data.yml", ScaUserDataBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ScaUserDataEntity readScaUserDataEntity() {
        try {
            return reader.getObjectFromResource(getClass(), "sca-user-data.yml", ScaUserDataEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
