package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.deposit.api.exception.DepositErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.postings.api.exception.PostingErrorCode;
import de.adorsys.ledgers.sca.exception.SCAErrorCode;
import de.adorsys.ledgers.um.api.exception.UserManagementErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class HttpStatusResolverTest {
    @Test
    public void testDepositStatusResolverCoverage() {
        List<DepositErrorCode> codes = Arrays.asList(DepositErrorCode.values());
        codes.forEach(c -> {
            boolean isNull = DepositHttpStatusResolver.resolveHttpStatusByCode(c) == null;
            if (isNull) {
                log.error("{} is missing in Resolver", c.name());
            }
            assertThat(isNull).isFalse();
        });
    }

    @Test
    public void testMiddlewareStatusResolverCoverage() {
        List<MiddlewareErrorCode> codes = Arrays.asList(MiddlewareErrorCode.values());
        codes.forEach(c -> {
            boolean isNull = MiddlewareHttpStatusResolver.resolveHttpStatusByCode(c) == null;
            if (isNull) {
                log.error("{} is missing in Resolver", c.name());
            }
            assertThat(isNull).isFalse();
        });
    }

    @Test
    public void testPostingStatusResolverCoverage() {
        List<PostingErrorCode> codes = Arrays.asList(PostingErrorCode.values());
        codes.forEach(c -> {
            boolean isNull = PostingHttpStatusResolver.resolveHttpStatusByCode(c) == null;
            if (isNull) {
                log.error("{} is missing in Resolver", c.name());
            }
            assertThat(isNull).isFalse();
        });
    }

    @Test
    public void testScaStatusResolverCoverage() {
        List<SCAErrorCode> codes = Arrays.asList(SCAErrorCode.values());
        codes.forEach(c -> {
            boolean isNull = ScaHttpStatusResolver.resolveHttpStatusByCode(c) == null;
            if (isNull) {
                log.error("{} is missing in Resolver", c.name());
            }
            assertThat(isNull).isFalse();
        });
    }

    @Test
    public void testUserManagementStatusResolverCoverage() {
        List<UserManagementErrorCode> codes = Arrays.asList(UserManagementErrorCode.values());
        codes.forEach(c -> {
            boolean isNull = UserManagementHttpStatusResolver.resolveHttpStatusByCode(c) == null;
            if (isNull) {
                log.error("{} is missing in Resolver", c.name());
            }
            assertThat(isNull).isFalse();
        });
    }
}
