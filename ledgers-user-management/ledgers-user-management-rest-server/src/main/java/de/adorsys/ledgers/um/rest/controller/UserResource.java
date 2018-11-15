/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.rest.controller;


import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.rest.exception.NotFoundRestException;

@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {

    static final String USERS = "/users/";
    private static final String SCA_DATA = "sca-data";
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    ResponseEntity<Void> createUser(@RequestBody UserBO user) throws UserAlreadyExistsException {
        UserBO userBO;
        userBO = userService.create(user);
        URI uri = UriComponentsBuilder.fromUriString(USERS + userBO.getLogin()).build().toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("{id}")
    ResponseEntity<UserBO> getUserById(@PathVariable("id") String id) {
        try {
        	return ResponseEntity.ok(userService.findById(id));
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @GetMapping
    ResponseEntity<UserBO> getUserByLogin(@RequestParam("login") String login) {
        try {
        	return ResponseEntity.ok(userService.findByLogin(login));
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @PutMapping("{id}/" + SCA_DATA)
    ResponseEntity<Void> updateUserScaData(@PathVariable String id, @RequestBody List<ScaUserDataBO> data) {
        try {
            UserBO userBO = userService.findById(id);
            UserBO user = userService.updateScaData(data, userBO.getLogin());

            URI uri = UriComponentsBuilder.fromUriString(USERS + user.getId())
                    .build().toUri();

            return ResponseEntity.created(uri).build();
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }
}
