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

package de.adorsys.ledgers.middleware.rest.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.RestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Management" , description= "Application management")
public interface AppMgmtRestAPI {

	public static final String BASE_PATH = "/management/app";
	public static final String ADMIN_PATH = "/admin";
	public static final String INIT_PATH = "/init";

    @GetMapping("/ping")
    @ApiOperation("Echo the server")
    public ResponseEntity<String> ping();
	
    @PostMapping(INIT_PATH)
    @ApiOperation("Initializes the deposit account module.")
    public ResponseEntity<Void> initApp() throws RestException;
    
    @PostMapping(ADMIN_PATH)
    @ApiOperation(value="Creates the admin account. This is only done if the application has no account yet. Returns a bearer token admin can use to proceed with further operations.")
    public ResponseEntity<BearerTokenTO> admin(@RequestBody(required=true) UserTO adminUser)
    		throws NotFoundRestException, ForbiddenRestException, ConflictRestException;

}
