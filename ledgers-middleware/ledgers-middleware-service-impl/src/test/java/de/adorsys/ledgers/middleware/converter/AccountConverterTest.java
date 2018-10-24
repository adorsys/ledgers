package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountStatusTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountTypeTO;
import de.adorsys.ledgers.middleware.service.domain.account.UsageTypeTO;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.util.Collections;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountConverterTest {
    private static final AccountDetailsTO ACCOUNT_DETAILS = getAccount(AccountDetailsTO.class);
    private static final DepositAccountBO DEPOSIT_ACCOUNT = getAccount(DepositAccountBO.class);
    private AccountDetailsMapper mapper = Mappers.getMapper(AccountDetailsMapper.class);
    private AccountConverter accountConverter = new AccountConverter(mapper);

    @Test
    public void toAccountDetailsTO() {
        AccountDetailsTO details = accountConverter.toAccountDetailsTO(DEPOSIT_ACCOUNT, Collections.emptyList());

        //assertThat(details).isEqualToComparingFieldByField(ACCOUNT_DETAILS); //TODO Should be uncommented when GetBalances and its Mapping is ready

        assertThat(details.getId()).isEqualTo("id");
        assertThat(details.getIban()).isEqualTo("iban");
        assertThat(details.getBban()).isEqualTo("bban");
        assertThat(details.getPan()).isEqualTo("pan");
        assertThat(details.getMaskedPan()).isEqualTo("maskedPan");
        assertThat(details.getMsisdn()).isEqualTo("msisdn");
        assertThat(details.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(details.getName()).isEqualTo("name");
        assertThat(details.getProduct()).isEqualTo("product");
        assertThat(details.getAccountType()).isEqualTo(AccountTypeTO.CASH);
        assertThat(details.getAccountStatus()).isEqualTo(AccountStatusTO.ENABLED);
        assertThat(details.getBic()).isEqualTo("bic");
        assertThat(details.getLinkedAccounts()).isEqualTo("account");
        assertThat(details.getUsageType()).isEqualTo(UsageTypeTO.PRIV);
        assertThat(details.getDetails()).isEqualTo("details");
        assertThat(details.getBalances()).isEqualTo(Collections.emptyList());//TODO fix when balances will be added
    }

    private static <T> T getAccount(Class<T> aClass) {
        return YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/AccountDetails.yml", aClass);
    }
}