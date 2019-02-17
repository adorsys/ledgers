package de.adorsys.ledgers.middleware.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateDepositAccountServiceTest {

    private static final String USER_ID = "id";
    private static final String BRANCH = "Nuremberg";

    @InjectMocks
    private CreateDepositAccountService createDepositAccountService;
    @Mock
    private DepositAccountService accountService;
    @Mock
    private AccessService accessService;
    @Mock
    private AccountDetailsMapper detailsMapper;

    static ObjectMapper mapper = getObjectMapper();

    @Test
    public void createDepositAccount() throws DepositAccountNotFoundException, UserNotFoundMiddlewareException {
        AccountDetailsTO accountDetails = getAccountDetailsTO();
        DepositAccountBO depositAccount = getDepositAccountBO();

        when(detailsMapper.toDepositAccountBO(any(AccountDetailsTO.class))).thenReturn(depositAccount);
        when(accountService.createDepositAccountForBranch(any(DepositAccountBO.class), anyString(), anyString())).thenReturn(depositAccount);

        createDepositAccountService.createDepositAccount(USER_ID, accountDetails, anyList(), BRANCH);

        verify(accountService, times(1)).createDepositAccountForBranch(depositAccount, USER_ID, BRANCH);
    }

    private AccountDetailsTO getAccountDetailsTO() {
        return readYml(AccountDetailsTO.class, "AccountDetailsTO.yml");
    }

    private DepositAccountBO getDepositAccountBO() {
        return readYml(DepositAccountBO.class, "DepositAccountBO.yml");
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return mapper.readValue(PaymentConverter.class.getResourceAsStream(fileName), aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
