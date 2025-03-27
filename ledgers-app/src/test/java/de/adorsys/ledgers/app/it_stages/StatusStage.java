/*
 * Copyright (c) 2018-2025 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.it_stages;

import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import io.restassured.RestAssured;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@JGivenStage
public class StatusStage extends BaseStage<StatusStage> {

    public static final String PAYMENTS = "/payments/{paymentId}";
    @Autowired
    private NamedParameterJdbcOperations jdbcOperations;

    @ScenarioState
    private String bearerToken;

    @Getter
    @ScenarioState
    private Map<String, Object> paymentEntity;
    @Getter
    @ScenarioState
    private List<String> paymentTargets;
    @ScenarioState
    private String operationObjectId;

    public StatusStage paymentStatus() {
        var resp = RestAssured.given()
                       .header(HttpHeaders.AUTHORIZATION, bearerToken)
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .when()
                       .get(PAYMENTS, operationObjectId)
                       .then()
                       .statusCode(HttpStatus.OK.value())
                       .and()
                       .extract();
        this.response = resp;
        return self();
    }
    public StatusStage readPaymentFromDB() {
        var query = "SELECT * FROM public.payment WHERE payment_id = :paymentId";
        this.paymentEntity = jdbcOperations.queryForObject(
                query,
                Map.of("paymentId", this.operationObjectId),
                new ColumnMapRowMapper()
        );
        assertThat(paymentEntity).isNotNull();
        return self();
    }
    public StatusStage readPaymentTargetsFromDB() {
        var query = "SELECT cred_iban FROM public.payment_target WHERE payment_payment_id = :paymentId";
        this.paymentTargets = jdbcOperations.query(
                query,
                Map.of("paymentId", this.operationObjectId),
                (rs, row) -> rs.getString(1)
        );
        assertThat(paymentTargets).isNotNull();
        return self();
    }
    public StatusStage verifyPaymentEntity(Consumer<Map<String, Object>> entityConsumer) {
        entityConsumer.accept(this.paymentEntity);
        return self();
    }
    public StatusStage verifyPaymentTargetsIban(Consumer<List<String>> entityConsumer) {
        entityConsumer.accept(this.paymentTargets);
        return self();
    }
}
