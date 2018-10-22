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

import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.rest.converter.UserTOConverter;
import de.adorsys.ledgers.um.rest.domain.UserTO;
import de.adorsys.ledgers.um.rest.exception.NotFoundRestException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {

    public static final String USERS = "/users/";
    private final UserService userService;
    private final UserTOConverter converter;

    public UserResource(UserService userService, UserTOConverter converter) {
        this.userService = userService;
        this.converter = converter;
    }

    @PostMapping
    ResponseEntity<Void> createUser(@RequestBody UserTO user) {
        try {
            UserBO bo = converter.toUserBO(user);
            UserBO userBO;
            userBO = userService.create(bo);
            URI uri = UriComponentsBuilder.fromUriString(USERS + userBO.getId()).build().toUri();
            return ResponseEntity.created(uri).build();
        } catch (UserAlreadyExistsException e) {
            throw new de.adorsys.ledgers.um.rest.exception.UserAlreadyExistsException(e.getMessage());
        }
    }

    @GetMapping("{id}")
    ResponseEntity<UserTO> getUser(@PathVariable String id) {
        try {
            UserBO userBO;
            userBO = userService.findById(id);
            return ResponseEntity.ok(converter.toUserTO(userBO));
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }
}
