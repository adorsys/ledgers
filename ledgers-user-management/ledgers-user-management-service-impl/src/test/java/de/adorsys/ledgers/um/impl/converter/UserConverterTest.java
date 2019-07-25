package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserConverterTest {
    public static final String USER_ID = "someID";
    public static final String USER_EMAIL = "spe@adorsys.com.ua";
    public static final String USER_LOGIN = "speex";
    public static final String USER_PIN = "1234567890";
    private static final List<ScaUserDataEntity> scaUserDataEntityList = readListYml(ScaUserDataEntity.class, "sca-user-data.yml");
    private static final List<ScaUserDataBO> scaUserDataBOList = readListYml(ScaUserDataBO.class, "sca-user-data.yml");
    private static final List<AccountAccess> accountAccessList = readListYml(AccountAccess.class, "account-access.yml");
    private static final List<AccountAccessBO> accountAccessBOList = readListYml(AccountAccessBO.class, "account-access.yml");

    UserConverter converter = Mappers.getMapper(UserConverter.class);

    @Test
    public void toUserBOList() {
        //empty list case
        List<UserEntity> users = Collections.emptyList();
        List<UserBO> result = converter.toUserBOList(users);
        assertThat(result, is(Collections.emptyList()));

        //user = null
        users = Collections.singletonList(null);
        result = converter.toUserBOList(users);
        assertThat(result, is(Collections.singletonList(new UserBO())));
    }

    @Test
    public void toUserEntityList() {
        //empty list case
        List<UserBO> users = Collections.emptyList();
        List<UserEntity> result = converter.toUserEntityList(users);
        assertThat(result, is(Collections.emptyList()));

        //user = null
        users = Collections.singletonList(null);
        result = converter.toUserEntityList(users);
        assertThat(result, is(Collections.singletonList(new UserEntity())));
    }

    @Test
    public void toScaUserDataBO() {
        ScaUserDataBO result = converter.toScaUserDataBO(scaUserDataEntityList.get(0));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(scaUserDataBOList.get(0));
    }

    @Test
    public void toScaUserDataEntity() {
        ScaUserDataEntity result = converter.toScaUserDataEntity(scaUserDataBOList.get(0));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(scaUserDataEntityList.get(0));
    }

    @Test
    public void toScaUserDataListBO() {
        List<ScaUserDataBO> result = converter.toScaUserDataListBO(scaUserDataEntityList);
        assertThat(result).containsExactlyInAnyOrderElementsOf(scaUserDataBOList);
    }

    @Test
    public void toScaUserDataListEntity() {
        List<ScaUserDataEntity> result = converter.toScaUserDataListEntity(scaUserDataBOList);
        assertThat(result).containsExactlyInAnyOrderElementsOf(scaUserDataEntityList);
    }

    @Test
    public void toAccountAccessBO() {
        AccountAccessBO result = converter.toAccountAccessBO(accountAccessList.get(0));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(accountAccessBOList.get(0));
    }

    @Test
    public void toAccountAccessEntity() {
        AccountAccess result = converter.toAccountAccessEntity(accountAccessBOList.get(0));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(accountAccessList.get(0));
    }

    @Test
    public void toAccountAccessListBO() {
        List<AccountAccessBO> result = converter.toAccountAccessListBO(accountAccessList);
        assertThat(result).containsExactlyInAnyOrderElementsOf(accountAccessBOList);
    }

    @Test
    public void toAccountAccessListEntity() {
        List<AccountAccess> result = converter.toAccountAccessListEntity(accountAccessBOList);
        assertThat(result).containsExactlyInAnyOrderElementsOf(accountAccessList);
    }

    @Test
    public void toUserRole() {
        List<UserRole> result = converter.toUserRole(Collections.singletonList(UserRoleBO.CUSTOMER));
        assertThat(result).isEqualTo(Collections.singletonList(UserRole.CUSTOMER));
    }

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

    private static <T> List<T> readListYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getListFromResource(UserConverterTest.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
