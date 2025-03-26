/*
 * Copyright (c) 2018-2025 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.it_stages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import static de.adorsys.ledgers.app.BaseContainersTest.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@JGivenStage
public class ManagementStage extends BaseStage<ManagementStage> {

    private static final String UPLOAD_DATA = "/staff-access/data/upload";
    private static final String DELETE_USER_RESOURCE = "/staff-access/data/user/{userId}";
    private static final String USERS_RESOURCE_ADMIN = "/admin/user";
    private static final String USERS_RESOURCE_STAFF = "staff-access/users";
    private static final String GET_ALL_USERS = "/admin/users/all";
    private static final String ACCOUNTS_RESOURCE = "/staff-access/accounts";
    private static final String ACCOUNT_BY_IBAN = "/staff-access/accounts/acc/acc";
    private static final String ACCOUNT_DETAIL = "/staff-access/accounts/{accountId}";
    private static final String DEPOSIT_CASH_RESOURCE = "/staff-access/accounts/{accountId}/cash";
    private static final String UPDATE_PASSWORD_BRANCH = "/admin/password";
    private static final String KEYCLOAK_TOKEN_PATH = "/realms/ledgers/protocol/openid-connect/token";
    private static final String ALL_USERS_RESOURCE = "/admin/users/all";
    private static final String CHANGE_STATUS_RESOURCE = "admin/status";
    private static final String MODIFY_USER_RESOURCE_AS_STAFF = "/staff-access/users/modify";
    private static final String MODIFY_SELF_RESOURCE = "/users/me";
    private static final String CUSTOMERS_RESOURCE_LOGIN = "/staff-access/users/logins";

    @Autowired
    private NamedParameterJdbcOperations jdbcOperations;
    @Autowired
    private KeycloakClientConfig clientConfig;

    @ScenarioState
    private String bearerToken;
    @ScenarioState
    private String userId;
    @ScenarioState
    private String accountId;
    @Getter
    @ScenarioState
    private Map<String, Object> userEntity;

    public ManagementStage obtainTokenFromKeycloak(String psuLogin, String psuPassword) {
        var resp = RestAssured.given()
                           .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                           .formParams(Map.of(
                                   "username", psuLogin,
                                   "password", psuPassword,
                                   "client_id", clientConfig.getExternalClientId(),
                                   "client_secret", clientConfig.getClientSecret(),
                                   "grant_type", "password")
                           )
                           .when()
                           .post(clientConfig.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();

        this.response = resp;
        this.bearerToken = getAccessToken(resp);
        return self();
    }

    public ManagementStage modifyUser(String resourceName, String newLogin, String email, String branch) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .header(AUTHORIZATION, bearerToken)
                           .contentType(ContentType.JSON)
                           .queryParams("branch", branch)
                           .body(resource(resourceName, Map.of("USER_ID", userId, "LOGIN", newLogin, "EMAIL", email)))
                           .when()
                           .post(MODIFY_USER_RESOURCE_AS_STAFF)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .extract();
        this.response = resp;
        return self();
    }

    public ManagementStage listCustomerLogins() {
        var resp = RestAssured.given()
                .header(AUTHORIZATION, this.bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(CUSTOMERS_RESOURCE_LOGIN)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract();
        this.response = resp;
        return self();
    }

    public ManagementStage getAllUsers() {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, bearerToken)
                           .when()
                           .get(ALL_USERS_RESOURCE)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();

        this.response = resp;
        return self();
    }

    public ManagementStage deleteUser() {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, bearerToken)
                           .when()
                           .delete(DELETE_USER_RESOURCE, userId)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();

        this.response = resp;
        return self();
    }

    @SneakyThrows
    public ManagementStage uploadData(String resourceName) {
        var toUpload = new ObjectMapper(new YAMLFactory()).readValue(resource(resourceName, Collections.emptyMap()), UploadedDataTO.class);

        var resp = RestAssured.given()
                .header(AUTHORIZATION, this.bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ObjectMapper().writeValueAsString(toUpload))
                .when()
                .post(UPLOAD_DATA)
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract();
        this.response = resp;

        return self();
    }

    public ManagementStage createNewUserAsAdmin(String login, String email, String branch) {
        return createNewUser(USERS_RESOURCE_ADMIN, login, email, branch);
    }

    public ManagementStage createNewUserAsStaff(String login, String email, String branch) {
        return createNewUser(USERS_RESOURCE_STAFF, login, email, branch);
    }


    public ManagementStage createNewTppAsAdmin(String login, String email, String branch) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .body(resource("new_staff.json", Map.of("LOGIN", login, "EMAIL", email, "ID", branch)))
                           .when()
                           .post(USERS_RESOURCE_ADMIN)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();

        this.response = resp;
        this.userId = resp.path("id");
        return self();
    }

    public ManagementStage getUserIdByLogin(String login) {
        var oldToken = bearerToken;
        obtainTokenFromKeycloak("admin", "admin123");
        var resp = RestAssured.given()
                          .header(AUTHORIZATION, this.bearerToken)
                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                          .when()
                          .get(GET_ALL_USERS)
                          .then()
                          .statusCode(HttpStatus.OK.value())
                          .and()
                          .extract();

        this.response = resp;
        this.userId = this.response.path("findAll { o -> o.login.equals(\""+ login +"\") }[0].id");
        this.bearerToken = oldToken;
        return self();
    }

    public ManagementStage changeStatusUser() {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .queryParams("userId", userId)
                           .when()
                           .post(CHANGE_STATUS_RESOURCE)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();

        this.response = resp;
        return self();
    }

    public ManagementStage createNewAccountForUser(String accountBodyResource, String iban) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .body(resource(accountBodyResource, Map.of("IBAN", iban)))
                           .queryParams("userId", userId)
                           .when()
                           .post(ACCOUNTS_RESOURCE)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();
        this.response = resp;
        return self();
    }

    public ManagementStage accountByIban(String iban) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .queryParams(Map.of("ibanParam", iban))
                           .when()
                           .get(ACCOUNT_BY_IBAN)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();
        this.response = resp;
        this.accountId = resp.path("id[0]");
        return self();
    }

    public ManagementStage getAccountDetails() {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .when()
                           .get(ACCOUNT_DETAIL, accountId)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();
        this.response = resp;
        return self();
    }

    public ManagementStage depositCash(String amountResource, String amount) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .body(resource(amountResource).replaceAll("%AMOUNT%", amount))
                           .when()
                           .post(DEPOSIT_CASH_RESOURCE, accountId)
                           .then()
                           .statusCode(HttpStatus.ACCEPTED.value())
                           .and()
                           .extract();

        this.response = resp;
        return self();
    }

    public ManagementStage changePasswordBranch(String branch, String newPassword) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .queryParams("branchId", branch, "password", newPassword)
                           .when()
                           .put(UPDATE_PASSWORD_BRANCH)
                           .then()
                           .statusCode(HttpStatus.ACCEPTED.value())
                           .and()
                           .extract();

        this.response = resp;
        return self();
    }

    public ManagementStage updateSelfTpp(String branchId, String newLogin, String newEmail) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .body(resource("update_tpp_user.json", Map.of("USER_ID", branchId, "LOGIN", newLogin,"EMAIL", newEmail)))
                           .when()
                           .put(MODIFY_SELF_RESOURCE)
                           .then()
                           .statusCode(HttpStatus.ACCEPTED.value())
                           .and()
                           .extract();

        this.response = resp;
        return self();
    }

    public ManagementStage readUserFromDb(String userLogin) {
        var query = "SELECT * FROM public.users WHERE login = :userLogin";
        this.userEntity = jdbcOperations.queryForObject(
                query,
                Map.of("userLogin", userLogin),
                new ColumnMapRowMapper()
        );
        assertThat(userEntity).isNotNull();
        return self();
    }

    public ManagementStage verifyUserEntity(Consumer<Map<String, Object>> entityConsumer) {
        entityConsumer.accept(this.userEntity);
        return self();
    }

    private ManagementStage createNewUser(String endpoint, String login, String email, String branch) {
        var resp = RestAssured.given()
                           .header(AUTHORIZATION, this.bearerToken)
                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                           .body(resource("new_user.json", Map.of("LOGIN", login, "EMAIL", email, "BRANCH", branch, "SCA_ID", UUID.randomUUID().toString())))
                           .when()
                           .post(endpoint)
                           .then()
                           .statusCode(HttpStatus.OK.value())
                           .and()
                           .extract();

        this.response = resp;
        this.userId = resp.path("id");
        return self();
    }
}
