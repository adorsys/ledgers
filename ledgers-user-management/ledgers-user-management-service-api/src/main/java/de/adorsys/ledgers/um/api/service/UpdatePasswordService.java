package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.security.UpdatePassword;

public interface UpdatePasswordService {
    UpdatePassword updatePassword(String userId, String newPassword);
}
