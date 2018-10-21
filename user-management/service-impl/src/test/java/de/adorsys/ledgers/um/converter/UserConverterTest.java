package de.adorsys.ledgers.um.converter;

import de.adorsys.ledgers.um.domain.UserBO;
import de.adorsys.ledgers.um.domain.UserEntity;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import static org.hamcrest.CoreMatchers.is;
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
    }

    @Test
    public void toUserPO() {

        UserEntity po = converter.toUserPO(buildUserBO());

        assertThat(po.getId(), is(USER_ID));
        assertThat(po.getEmail(), is(USER_EMAIL));
        assertThat(po.getLogin(), is(USER_LOGIN));
        assertThat(po.getPin(), is(USER_PIN));
    }

    //todo: @spe replace by json source file
    private UserBO buildUserBO() {
        UserBO bo = new UserBO();
        bo.setId(USER_ID);
        bo.setEmail(USER_EMAIL);
        bo.setLogin(USER_LOGIN);
        bo.setPin(USER_PIN);
        return bo;
    }

    //todo: @spe replace by json source file
    private UserEntity buildUserPO() {
        UserEntity entity = new UserEntity();
        entity.setId(USER_ID);
        entity.setEmail(USER_EMAIL);
        entity.setLogin(USER_LOGIN);
        entity.setPin(USER_PIN);
        return entity;
    }
}