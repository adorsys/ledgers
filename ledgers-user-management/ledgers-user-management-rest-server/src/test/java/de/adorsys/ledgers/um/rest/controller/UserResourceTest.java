/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.rest.controller;

import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.ResponseEntity;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class UserResourceTest {

    private static final String USER_LOGIN = "vne";
    private static final String USER_ID = "SomeUniqueID";

    @InjectMocks
    private UserResource userResource; // user controller

    @Mock
    public UserService userService;

    private ResourceReader reader = YamlReader.getInstance();

    @Test
    void createUser() throws Exception {
        // Given
        String jsonUser = JsonReader.getInstance().getStringFromFile("de/adorsys/ledgers/um/rest/controller/user.json");
        UserBO userBO = JsonReader.getInstance().getObjectFromString(jsonUser, UserBO.class);

        when(userService.create(userBO)).thenReturn(userBO);

        // When
        ResponseEntity<Void> actual = userResource.createUser(userBO);

        // Then
        assertEquals("/users/vne_ua", actual.getHeaders().get("Location").get(0));
        verify(userService, times(1)).create(any());
    }

    @Test
    void getUserByLogin() {
        // Given
        UserBO userBO = getUserBO();
        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);

        // When
        ResponseEntity<UserBO> actual = userResource.getUserByLogin(USER_LOGIN);

        UserBO user = actual.getBody();

        // Then
        assertNotNull(user);
        assertEquals(user, userBO);
        verify(userService, times(1)).findByLogin(USER_LOGIN);
    }

    @Test
    void getUserById() {
        // Given
        UserBO userBO = getUserBO();
        when(userService.findById(USER_ID)).thenReturn(userBO);

        // When
        ResponseEntity<UserBO> actual = userResource.getUserById(USER_ID);

        UserBO user = actual.getBody();

        // Then
        assertNotNull(user);
        assertEquals(user, userBO);
        verify(userService, times(1)).findById(USER_ID);
    }

    @Test
    void updateUserScaData() throws Exception {
        // Given
        UserBO userBO = getUserBO();
        when(userService.findById(anyString())).thenReturn(userBO);
        when(userService.updateScaData(any(), anyString())).thenReturn(userBO);

        String jsonScaUserData = JsonReader.getInstance().getStringFromFile("de/adorsys/ledgers/um/rest/controller/scaUserData.json");
        List scaUserData = JsonReader.getInstance().getObjectFromString(jsonScaUserData, ArrayList.class);

        // When
        ResponseEntity actual = userResource.updateUserScaData(USER_ID, scaUserData);

        // Then
        assertEquals("/users/" + USER_ID, actual.getHeaders().get("Location").get(0));
        verify(userService, times(1)).findById(USER_ID);
        verify(userService, times(1)).updateScaData(any(), anyString());
    }

    @Test
    void getAllUsers() {
        // Given
        List<UserBO> userList = Collections.singletonList(getUserBO());
        when(userService.listUsers(anyInt(), anyInt())).thenReturn(userList);

        // When
        ResponseEntity<List<UserBO>> actual = userResource.getAllUsers();

        List<UserBO> users = actual.getBody();

        // Then
        assertNotNull(users);
        assertEquals(users, userList);

        verify(userService, times(1)).listUsers(anyInt(), anyInt());
    }

    private UserBO getUserBO() {
        try {
            return this.reader.getObjectFromFile("de/adorsys/ledgers/um/rest/controller/user-BO.yml", UserBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
