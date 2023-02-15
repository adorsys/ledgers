/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;

public interface AppManagementService {

    /**
     * Called one the application is started to preload the system with
     * some data.
     */
    void initApp();

    /**
     * Upload NISP compliant Test Data
     *
     * @param data uploaded data
     * @param info SCA information
     */
    void uploadData(UploadedDataTO data, ScaInfoTO info);

    /**
     * @param userId        id of user to perform block on
     * @param isSystemBlock boolean representation of choice of block (system or regular)
     * @return boolean representation of block status
     */
    boolean changeBlockedStatus(String userId, boolean isSystemBlock);

    String generateNextBban(BbanStructure structure);
}
