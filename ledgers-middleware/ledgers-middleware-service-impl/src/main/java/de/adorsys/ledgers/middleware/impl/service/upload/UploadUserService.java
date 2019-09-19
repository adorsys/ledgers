package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                       .map(u -> execute(() -> middlewareUserService.create(u)))
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
}
