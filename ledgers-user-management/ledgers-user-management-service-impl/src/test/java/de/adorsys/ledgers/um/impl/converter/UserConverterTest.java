/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserConverterTest {
    public static final String USER_ID = "someID";
    private static final String USER_EMAIL = "spe@adorsys.com.ua";
    private static final String USER_LOGIN = "speex";
    private static final String USER_PIN = "1234567890";
    private static final String SCA_ID = "0UaAHbFRQxUpptZmyjq9XQ";
    private static final List<ScaUserDataEntity> scaUserDataEntityList = readListYml(ScaUserDataEntity.class, "sca-user-data.yml");
    private static final List<ScaUserDataBO> scaUserDataBOList = readListYml(ScaUserDataBO.class, "sca-user-data.yml");
    private static final List<AccountAccess> accountAccessList = readListYml(AccountAccess.class, "account-access.yml");
    private static final List<AccountAccessBO> accountAccessBOList = readListYml(AccountAccessBO.class, "account-access.yml");

    private UserConverter converter = Mappers.getMapper(UserConverter.class);

    @Test
    void toUserBOList() {
        //empty list case
        List<UserEntity> users = Collections.emptyList();
        List<UserBO> result = converter.toUserBOList(users);
        assertEquals(Collections.emptyList(), result);

        //user = null
        users = Collections.singletonList(null);
        result = converter.toUserBOList(users);
        assertEquals(Collections.singletonList(new UserBO()), result);
    }

    @Test
    void toUserEntityList() {
        //empty list case
        List<UserBO> users = Collections.emptyList();
        List<UserEntity> result = converter.toUserEntityList(users);
        assertEquals(Collections.emptyList(), result);

        //user = null
        users = Collections.singletonList(null);
        result = converter.toUserEntityList(users);
        assertEquals(Collections.singletonList(new UserEntity()), result);
    }

    @Test
    void toScaUserDataBO() {
        // When
        ScaUserDataBO result = converter.toScaUserDataBO(scaUserDataEntityList.get(0));

        // Then
        assertEquals(scaUserDataBOList.get(0), result);
    }

    @Test
    void toScaUserDataEntity_idExist() {
        // When
        ScaUserDataEntity result = converter.toScaUserDataEntity(getScaUserDataBO(SCA_ID));

        // Then
        assertEquals(getScaUserDataEntity(SCA_ID), result);
    }

    @Test
    void toScaUserDataEntity_idBlank() {
        // When
        ScaUserDataEntity result = converter.toScaUserDataEntity(getScaUserDataBO(""));

        // Then
        assertEquals(getScaUserDataEntity(null), result);
    }

    @Test
    void toScaUserDataListBO() {
        // When
        List<ScaUserDataBO> result = converter.toScaUserDataListBO(scaUserDataEntityList);

        // Then
        assertEquals(new HashSet<>(scaUserDataBOList), new HashSet<>(result));
    }

    @Test
    void toScaUserDataListEntity() {
        // When
        List<ScaUserDataEntity> result = converter.toScaUserDataListEntity(scaUserDataBOList);

        // Then
        assertEquals(new HashSet<>(scaUserDataEntityList), new HashSet<>(result));
    }

    @Test
    void toAccountAccessBO() {
        // When
        AccountAccessBO result = converter.toAccountAccessBO(accountAccessList.get(0));

        // Then
        assertEquals(accountAccessBOList.get(0), result);
    }

    @Test
    void toAccountAccessEntity() {
        // When
        AccountAccess result = converter.toAccountAccessEntity(accountAccessBOList.get(0));
        result.setCreated(null);

        AccountAccess expected = accountAccessList.get(0);
        expected.setCreated(null);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void toAccountAccessListBO() {
        // When
        List<AccountAccessBO> result = converter.toAccountAccessListBO(accountAccessList);

        // Then
        assertEquals(new HashSet<>(accountAccessBOList), new HashSet<>(result));
    }

    @Test
    void toAccountAccessListEntity() {
        // When
        List<AccountAccess> result = converter.toAccountAccessListEntity(accountAccessBOList);

        result.forEach(accountAccess -> accountAccess.setCreated(null));
        accountAccessList.forEach(accountAccess -> accountAccess.setCreated(null));

        // Then
        assertEquals(new HashSet<>(accountAccessList), new HashSet<>(result));
    }

    @Test
    void toUserRole() {
        // When
        List<UserRole> result = converter.toUserRole(Collections.singletonList(UserRoleBO.CUSTOMER));

        // Then
        assertEquals(Collections.singletonList(UserRole.CUSTOMER), result);
    }

    @Test
    void toUserBO() {
        // When
        UserBO bo = converter.toUserBO(buildUserPO());

        // Then
        assertEquals(USER_ID, bo.getId());
        assertEquals(USER_EMAIL, bo.getEmail());
        assertEquals(USER_LOGIN, bo.getLogin());
    }

    @Test
    void toUserPO() {
        // When
        UserEntity po = converter.toUserPO(buildUserBO());

        // Then
        assertEquals(USER_ID, po.getId());
        assertEquals(USER_EMAIL, po.getEmail());
        assertEquals(USER_LOGIN, po.getLogin());
    }

    @Test
    void toExtendedUserBO() {
        UserExtendedBO result = converter.toUserExtendedBO(buildUserPO(), "BRANCH_LOGIN");
        assertEquals(getExpectedExtendedUser(), result);
    }

    private UserExtendedBO getExpectedExtendedUser() {
        UserExtendedBO bo = new UserExtendedBO();
        bo.setId(USER_ID);
        bo.setLogin(USER_LOGIN);
        bo.setEmail(USER_EMAIL);
        bo.setBranchLogin("BRANCH_LOGIN");
        return bo;
    }

    private ScaUserDataBO getScaUserDataBO(String id) {
        ScaUserDataBO scaUserData = scaUserDataBOList.get(0);
        scaUserData.setId(id);
        return scaUserData;
    }

    private ScaUserDataEntity getScaUserDataEntity(String id) {
        ScaUserDataEntity scaUserData = scaUserDataEntityList.get(0);
        scaUserData.setId(id);
        return scaUserData;
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
