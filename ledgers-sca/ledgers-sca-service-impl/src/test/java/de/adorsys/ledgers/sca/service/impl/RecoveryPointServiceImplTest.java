package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.db.domain.RecoveryPointEntity;
import de.adorsys.ledgers.sca.db.repository.RecoveryPointRepository;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import de.adorsys.ledgers.sca.service.impl.mapper.RecoveryPointMapper;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryPointServiceImplTest {
    private static final String DESCRIPTION = "description";
    private static final String BRANCH_ID = "BRANCH";
    private static final long POINT_ID = 1L;
    public static final RecoveryPointMapper MAPPER = Mappers.getMapper(RecoveryPointMapper.class);

    @InjectMocks
    private RecoveryPointServiceImpl service;

    @Mock
    private RecoveryPointRepository repository;
    @Mock
    private RecoveryPointMapper mapper;

    @Test
    void getById() {
        when(repository.findByIdAndBranchId(anyLong(), anyString())).thenReturn(Optional.of(new RecoveryPointEntity()));
        when(mapper.toBO(any())).thenReturn(getPointBO());
        RecoveryPointBO result = service.getById(POINT_ID, BRANCH_ID);
        assertEquals(POINT_ID, result.getId());
    }

    @Test
    void getById_non_existing_id() {
        when(repository.findByIdAndBranchId(anyLong(), anyString())).thenReturn(Optional.empty());
        assertThrows(ScaModuleException.class, () -> service.getById(POINT_ID, BRANCH_ID));
    }

    @Test
    void getAllByBranch() {
        when(repository.findAllByBranchId(anyString())).thenReturn(singletonList(new RecoveryPointEntity()));
        when(mapper.toBOs(anyList())).thenReturn(singletonList(getPointBO()));
        List<RecoveryPointBO> result = service.getAllByBranch(BRANCH_ID);
        assertEquals(singletonList(getPointBO()), result);
    }

    @Test
    void deleteRecoveryPoint() {
        when(repository.existsByIdAndBranchId(anyLong(), anyString())).thenReturn(true);
        service.deleteRecoveryPoint(POINT_ID, BRANCH_ID);
        verify(repository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteRecoveryPoint_non_existing() {
        when(repository.existsByIdAndBranchId(anyLong(), anyString())).thenReturn(false);
        assertThrows(ScaModuleException.class, () -> service.deleteRecoveryPoint(POINT_ID, BRANCH_ID));
        verify(repository, times(0)).deleteById(any());
    }

    @Test
    void createRecoveryPoint() {
        when(mapper.toEntity(any())).thenAnswer(a -> MAPPER.toEntity(a.getArgument(0)));
        RecoveryPointBO input = getPointBO(DESCRIPTION);
        service.createRecoveryPoint(input);
        ArgumentCaptor<RecoveryPointEntity> captor = ArgumentCaptor.forClass(RecoveryPointEntity.class);
        verify(repository, times(1)).save(captor.capture());
        RecoveryPointEntity persisted = captor.getValue();
        assertEquals(BRANCH_ID, persisted.getBranchId());
        assertEquals(DESCRIPTION, persisted.getDescription());
    }

    @Test
    void createRecoveryPoint_no_descr() {
        when(mapper.toEntity(any())).thenAnswer(a -> MAPPER.toEntity(a.getArgument(0)));
        RecoveryPointBO input = getPointBO(" ");
        service.createRecoveryPoint(input);
        ArgumentCaptor<RecoveryPointEntity> captor = ArgumentCaptor.forClass(RecoveryPointEntity.class);
        verify(repository, times(1)).save(captor.capture());
        RecoveryPointEntity persisted = captor.getValue();
        assertEquals(BRANCH_ID, persisted.getBranchId());
        assertTrue(StringUtils.isNotBlank(persisted.getDescription()));
    }

    private RecoveryPointBO getPointBO() {
        RecoveryPointBO bo = new RecoveryPointBO();
        bo.setId(POINT_ID);
        return bo;
    }

    private RecoveryPointBO getPointBO(String description) {
        RecoveryPointBO bo = new RecoveryPointBO();
        bo.setDescription(description);
        bo.setBranchId(BRANCH_ID);
        return bo;
    }
}