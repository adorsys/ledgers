package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static de.adorsys.ledgers.middleware.impl.service.upload.ExpressionExecutionWrapper.execute;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadDepositAccountService {
    private final MiddlewareAccountManagementService middlewareAccountService;

    public void uploadDepositAccounts(List<UserTO> users, Map<String, AccountDetailsTO> details, ScaInfoTO info) {
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        users.forEach(u -> createDepositAccount(u, details, info));
    }

    private void createDepositAccount(UserTO user, Map<String, AccountDetailsTO> details, ScaInfoTO info) {
        user.getAccountAccesses().stream()
                .filter(a -> details.containsKey(a.getIban()))
                .map(a -> details.get(a.getIban()))
                .forEach(a -> execute(() -> {
                    try {
                        middlewareAccountService.createDepositAccount(user.getId(), info, a);
                    } catch (Exception e) {
                        log.info("Seems account is already present, skipping creation. {}", e.getMessage());
                    }
                }));
    }
}
