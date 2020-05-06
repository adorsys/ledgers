package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;

public interface AppManagementService {

    /**
     * Called one the application is started to preload the system with
     * some data.
     */
    void initApp();

    /**
     * @param userId   id of user initiating operation
     * @param role     role of user initiating operation
     * @param branchId branch to remove
     */
    void removeBranch(String userId, UserRoleTO role, String branchId);

    /**
     * Upload NISP compliant Test Data
     * @param data uploaded data
     * @param info SCA information
     */
    void uploadData(UploadedDataTO data, ScaInfoTO info);

    /**
     *
     * @param userId id of user to perform block on
     * @param isSystemBlock boolean representation of choice of block (system or regular)
     * @return boolean representation of block status
     */
    boolean changeBlockedStatus(String userId, boolean isSystemBlock);
}
