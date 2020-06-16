package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.impl.service.upload.ExpressionExecutionWrapper.execute;

@Service
@RequiredArgsConstructor
public class UploadUserService {
    private final MiddlewareUserManagementService middlewareUserService;

    public List<UserTO> uploadUsers(List<UserTO> users, String branch) {
        return users.stream()
                       .map(u -> u.updateUserBranch(branch))
                       .map(u -> execute(() -> {
                           List<AccountAccessTO> temp = u.getAccountAccesses();
                           u.setAccountAccesses(Collections.emptyList());
                           UserTO to = middlewareUserService.create(u);
                           to.setAccountAccesses(temp);
                           return to;
                       }))
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
}
