package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.repository.ScaUserDataRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.exception.SCAErrorCode;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.EMAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScaUserDataServiceImpl implements ScaUserDataService {
    private final ScaUserDataRepository scaUserDataRepository;
    private final UserConverter userConverter;

    @Override
    public ScaUserDataBO findByEmail(String email) {
        ScaUserDataEntity userUserDataEntity = scaUserDataRepository.findByMethodValue(email).stream()
                                                       .findFirst()
                                                       .orElseThrow(() -> ScaModuleException.builder()
                                                                                  .errorCode(SCAErrorCode.USER_SCA_DATA_NOT_FOUND)
                                                                                  .devMsg(String.format("Sca data with email: %s not found", email))
                                                                                  .build());

        return userConverter.toScaUserDataBO(userUserDataEntity);
    }

    @Override
    public ScaUserDataBO findById(String scaId) {
        ScaUserDataEntity scaUserDataEntity = scaUserDataRepository.findById(scaId).orElseThrow(() -> ScaModuleException.builder()
                                                                                                              .errorCode(SCAErrorCode.USER_SCA_DATA_NOT_FOUND)
                                                                                                              .devMsg(String.format("Sca data with id: %s not found", scaId))
                                                                                                              .build());
        return userConverter.toScaUserDataBO(scaUserDataEntity);
    }

    @Override
    public void updateScaUserData(ScaUserDataBO scaUserDataBO) {
        scaUserDataRepository.save(userConverter.toScaUserDataEntity(scaUserDataBO));
    }

    @Override
    public void ifScaChangedEmailNotValid(List<ScaUserDataBO> oldScaData, List<ScaUserDataBO> newScaData) {
        oldScaData.forEach(scaData -> newScaData.stream()
                                              .filter(n -> n.getScaMethod() == EMAIL)
                                              .filter(n -> n.getId().equals(scaData.getId()))
                                              .findFirst()
                                              .ifPresent(n -> {
                                                  if (!n.getMethodValue().equals(scaData.getMethodValue())) {
                                                      n.setValid(false);
                                                  }
                                              }));
    }
}