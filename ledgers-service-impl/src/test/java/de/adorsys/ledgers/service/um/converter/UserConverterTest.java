package de.adorsys.ledgers.service.um.converter;

import de.adorsys.ledgers.db.um.domain.UserPO;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.service.um.domain.UserBO;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class UserConverterTest {
    public static final String USER_ID = "someID";
    public static final String USER_EMAIL = "spe@adorsys.com.ua";
    public static final String USER_LOGIN = "speex";
    public static final String USER_PIN = "1234567890";

    UserConverter converter = Mappers.getMapper(UserConverter.class);


    @Test
    public void toUserBO() {
        UserBO bo = converter.toUserBO(buildUserPO());

        assertThat(bo.getId(), is(USER_ID));
        assertThat(bo.getEmail(), is(USER_EMAIL));
        assertThat(bo.getLogin(), is(USER_LOGIN));
        assertThat(bo.getPin(), is(USER_PIN));
        assertThat(bo.getAccounts(), hasSize(1));
    }

    @Test
    public void toUserPO() {

        UserPO po = converter.toUserPO(buildUserBO());

        assertThat(po.getId(), is(USER_ID));
        assertThat(po.getEmail(), is(USER_EMAIL));
        assertThat(po.getLogin(), is(USER_LOGIN));
        assertThat(po.getPin(), is(USER_PIN));
        assertThat(po.getAccounts(), hasSize(1));
    }

    //todo: @spe replace by json source file
    private UserBO buildUserBO(){
        UserBO bo = UserBO.builder().id(USER_ID)
                               .email(USER_EMAIL)
                               .login(USER_LOGIN)
                               .pin(USER_PIN)
                               .accounts(Collections.singletonList(LedgerAccount.builder().build()))
                               .build();
        return bo;
    }

    //todo: @spe replace by json source file
    private UserPO buildUserPO(){
        UserPO po = UserPO.builder().id(USER_ID)
                               .email(USER_EMAIL)
                               .login(USER_LOGIN)
                               .pin(USER_PIN)
                               .accounts(Collections.singletonList(LedgerAccount.builder().build()))
                               .build();
        return po;
    }
}