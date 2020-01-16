package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.repository.ScaUserDataRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScaUserDataServiceImplTest {

    @InjectMocks
    private ScaUserDataServiceImpl scaUserDataService;
    @Mock
    private ScaUserDataRepository scaUserDataRepository;
    @Mock
    private UserConverter userConverter;

    private ResourceReader reader = YamlReader.getInstance();

    private static final String EMAIL = "google@gmail.com";
    private static final String SCA_ID = "6DwJm-TpResvxLdX3fHpjc";
    private ScaUserDataEntity scaUserDataEntity;
    private ScaUserDataBO scaUserDataBO;

    @Before
    public void setUp() {
        scaUserDataEntity = readScaUserDataEntity();
        scaUserDataBO = readScaUserDataBO();
    }

    @Test
    public void findByEmail(){
        when(scaUserDataRepository.findByMethodValue(any())).thenReturn(Collections.singletonList(scaUserDataEntity));
        when(userConverter.toScaUserDataBO(any())).thenReturn(scaUserDataBO);

        ScaUserDataBO scaUserDataBO = scaUserDataService.findByEmail(EMAIL);

        assertThat(scaUserDataBO.getMethodValue(), is(EMAIL));

        verify(scaUserDataRepository, times(1)).findByMethodValue(EMAIL);
    }

    @Test(expected = ScaModuleException.class)
    public void findByEmail_scaNotFound(){
        when(scaUserDataRepository.findByMethodValue(any())).thenReturn(Collections.EMPTY_LIST);
        when(userConverter.toScaUserDataBO(any())).thenReturn(scaUserDataBO);

        scaUserDataService.findByEmail(EMAIL);
    }

    @Test
    public void findById(){
        when(scaUserDataRepository.findById(any())).thenReturn(Optional.ofNullable(scaUserDataEntity));
        when(userConverter.toScaUserDataBO(any())).thenReturn(scaUserDataBO);

        ScaUserDataBO scaUserDataBO = scaUserDataService.findById(SCA_ID);

        assertThat(scaUserDataBO.getId(), is(SCA_ID));

        verify(scaUserDataRepository, times(1)).findById(SCA_ID);
    }

    @Test(expected = ScaModuleException.class)
    public void findById_scaNotFound(){
        when(scaUserDataRepository.findById(any())).thenReturn(Optional.empty());
        when(userConverter.toScaUserDataBO(any())).thenReturn(scaUserDataBO);

        scaUserDataService.findById(SCA_ID);
    }

    @Test
    public void updateScaUserData(){
        when(scaUserDataRepository.save(any())).thenReturn(scaUserDataEntity);
        when(userConverter.toScaUserDataEntity(any())).thenReturn(scaUserDataEntity);

        scaUserDataService.updateScaUserData(scaUserDataBO);

        verify(scaUserDataRepository, times(1)).save(scaUserDataEntity);
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
