package de.adorsys.ledgers.middleware.client.mappers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import de.adorsys.ledgers.middleware.client.util.MultiPartContentUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.BULK;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMapperTO {
    private Map<String, List<String>> debtorPart;
    private Map<String, List<String>> creditorPart;
    private Map<String, List<String>> address;
    private Map<String, List<String>> reference;
    private Map<String, List<String>> amount;

    @JsonIgnore
    private ObjectMapper mapper;
    @JsonIgnore
    private XmlMapper xmlMapper = new XmlMapper();

    @JsonIgnore
    public PaymentTO toAbstractPayment(String payment, String paymentType, String paymentProduct) {
        if (isMultiPartPeriodicPayment(paymentType, paymentProduct)) {
            MultiPartContentUtils.MultiPartContent multiPartContent = MultiPartContentUtils.parse(payment);
            JsonNode node = readTree(multiPartContent.getXmlSct(), paymentProduct);
            PaymentTO paymentTO = fillPaymentTO(paymentType, paymentProduct, node);
            return fillPeriodicData(paymentTO, multiPartContent.getJsonStandingOrderType());
        }

        JsonNode node = readTree(payment, paymentProduct);
        return fillPaymentTO(paymentType, paymentProduct, node);
    }

    private boolean isMultiPartPeriodicPayment(String paymentType, String paymentProduct) {
        return PaymentTypeTO.PERIODIC.name().equals(paymentType) &&
                       (paymentProduct.contains("pain") || paymentProduct.contains("xml"));
    }

    @NotNull
    private PaymentTO fillPaymentTO(String paymentType, String paymentProduct, JsonNode node) {
        PaymentTO paymentTO = parseDebtorPart(paymentType, paymentProduct, node);
        List<PaymentTargetTO> parts = parseCreditorParts(node, paymentTO.getPaymentType());
        paymentTO.setTargets(parts);
        return paymentTO;
    }

    private PaymentTO fillPeriodicData(PaymentTO paymentTO, String content) {
        try {
            JsonNode node = mapper.readTree(content);
            return fillPeriodicData(paymentTO, node);
        } catch (IOException e) {
            log.error("Read tree exception {}, {}", e.getCause(), e.getMessage());
            throw new IllegalArgumentException("Could not parse payment!");
        }
    }

    private PaymentTO fillPeriodicData(PaymentTO paymentTO, JsonNode node) {
        mapProperties(debtorPart, "startDate", node, paymentTO::setStartDate, LocalDate.class);
        mapProperties(debtorPart, "endDate", node, paymentTO::setEndDate, LocalDate.class);
        mapProperties(debtorPart, "executionRule", node, paymentTO::setExecutionRule, String.class);
        mapProperties(debtorPart, "dayOfExecution", node, paymentTO::setDayOfExecution, Integer.class);
        debtorPart.get("frequency").stream()
                .map(node::findValue)
                .filter(Objects::nonNull)
                .findFirst()
                .map(JsonNode::asText)
                .map(String::toUpperCase)
                .map(FrequencyCodeTO::valueOf)
                .ifPresent(paymentTO::setFrequency);

        return paymentTO;
    }

    private JsonNode readTree(String payment, String product) {
        try {
            if (product.contains("pain") || product.contains("xml")) {
                JSONObject json = XML.toJSONObject(payment);
                return mapper.readTree(json.toString());
            }
            return mapper.readTree(payment);
        } catch (IOException e) {
            log.error("Read tree exception {}, {}", e.getCause(), e.getMessage());
            throw new IllegalArgumentException("Could not parse payment!");
        }
    }

    private List<PaymentTargetTO> parseCreditorParts(JsonNode node, PaymentTypeTO type) {
        List<PaymentTargetTO> targets = new ArrayList<>();
        List<JsonNode> nodes = debtorPart.get("targets").stream()
                                       .map(node::findValue)
                                       .filter(Objects::nonNull).collect(Collectors.toList());
        if (type == BULK) {
            nodes.forEach(n -> n.elements()
                                       .forEachRemaining(t -> targets.add(mapTarget(t))));
        } else {
            if (!nodes.isEmpty()) {
                nodes.forEach(t -> targets.add(mapTarget(t)));
            } else {
                targets.add(mapTarget(node));
            }
        }
        return targets;
    }

    private PaymentTO parseDebtorPart(String paymentType, String paymentProduct, JsonNode node) {
        PaymentTO paymentTO = new PaymentTO();
        paymentTO.setPaymentProduct(paymentProduct);
        paymentTO.setPaymentType(PaymentTypeTO.valueOf(paymentType));

        mapProperties(debtorPart, "paymentId", node, paymentTO::setPaymentId, String.class);
        mapProperties(debtorPart, "batchBookingPreferred", node, paymentTO::setBatchBookingPreferred, boolean.class);
        mapProperties(debtorPart, "requestedExecutionDate", node, paymentTO::setRequestedExecutionDate, LocalDate.class);
        mapProperties(debtorPart, "requestedExecutionTime", node, paymentTO::setRequestedExecutionTime, LocalTime.class);
        mapProperties(debtorPart, "debtorAgent", node, paymentTO::setDebtorAgent, String.class);
        mapProperties(debtorPart, "debtorName", node, paymentTO::setDebtorName, String.class);
        mapProperties(debtorPart, "transactionStatus", node, paymentTO::setTransactionStatus, TransactionStatusTO.class);
        fillPeriodicData(paymentTO, node);

        fillEmbeddedProperty(debtorPart, "debtorAccount", node, this::mapReference, paymentTO::setDebtorAccount);
        return paymentTO;
    }

    private PaymentTargetTO mapTarget(JsonNode node) {
        PaymentTargetTO target = new PaymentTargetTO();
        mapProperties(creditorPart, "endToEndIdentification", node, target::setEndToEndIdentification, String.class);
        mapProperties(creditorPart, "currencyOfTransfer", node, target::setCurrencyOfTransfer, Currency.class);
        mapProperties(creditorPart, "creditorAgent", node, target::setCreditorAgent, String.class);
        mapProperties(creditorPart, "creditorName", node, target::setCreditorName, String.class);
        mapProperties(creditorPart, "purposeCode", node, target::setPurposeCode, PurposeCodeTO.class);
        mapProperties(creditorPart, "remittanceInformationUnstructured", node, target::setRemittanceInformationUnstructured, String.class);
        mapProperties(creditorPart, "remittanceInformationStructured", node, target::setRemittanceInformationStructured, RemittanceInformationStructuredTO.class);
        mapProperties(creditorPart, "chargeBearer", node, target::setChargeBearerTO, ChargeBearerTO.class);

        fillEmbeddedProperty(creditorPart, "creditorAccount", node, this::mapReference, target::setCreditorAccount);
        fillEmbeddedProperty(creditorPart, "instructedAmount", node, this::mapAmount, target::setInstructedAmount);
        fillEmbeddedProperty(creditorPart, "creditorAddress", node, this::mapAddress, target::setCreditorAddress);
        return target;
    }

    private AccountReferenceTO mapReference(JsonNode node) {
        AccountReferenceTO account = new AccountReferenceTO();
        mapProperties(reference, "iban", node, account::setIban, String.class);
        mapProperties(reference, "currency", node, account::setCurrency, Currency.class);
        return account;
    }

    private AddressTO mapAddress(JsonNode node) {
        AddressTO addressTO = new AddressTO();
        mapProperties(address, "street", node, addressTO::setStreet, String.class);
        mapProperties(address, "buildingNumber", node, addressTO::setBuildingNumber, String.class);
        mapProperties(address, "city", node, addressTO::setCity, String.class);
        mapProperties(address, "postalCode", node, addressTO::setPostalCode, String.class);
        mapProperties(address, "country", node, addressTO::setCountry, String.class);
        mapProperties(address, "line1", node, addressTO::setLine1, String.class);
        mapProperties(address, "line2", node, addressTO::setLine2, String.class);
        return addressTO;
    }

    private AmountTO mapAmount(JsonNode node) {
        AmountTO instructedAmount = new AmountTO();
        mapProperties(amount, "amount", node, instructedAmount::setAmount, BigDecimal.class);
        mapProperties(amount, "currency", node, instructedAmount::setCurrency, Currency.class);
        return instructedAmount;
    }

    private <T> void fillEmbeddedProperty(Map<String, List<String>> map, String property, JsonNode node, Function<JsonNode, T> mappingMethod, Consumer<T> consumer) {
        Optional.ofNullable(map.get(property))
                .ifPresent(props -> mapEmbeddedProperty(node, mappingMethod, consumer, props));
    }

    private <T> void mapEmbeddedProperty(JsonNode node, Function<JsonNode, T> mappingMethod, Consumer<T> consumer, List<String> properties) {
        properties.stream()
                .map(node::findValue)
                .filter(Objects::nonNull)
                .map(mappingMethod)
                .forEach(consumer);
    }

    private <T> void mapProperties(Map<String, List<String>> map, String property, JsonNode node, Consumer<T> consumer, Class<T> clazz) {
        Optional.ofNullable(map.get(property))
                .ifPresent(pr -> pr.forEach(p -> mapProperty(node, consumer, clazz, p)));
    }

    private <T> void mapProperty(JsonNode node, Consumer<T> consumer, Class<T> clazz, String property) {
        Optional.ofNullable(node.findValue(property))
                .map(n -> mapObject(n, clazz))
                .ifPresent(consumer);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private <T> T mapObject(JsonNode node, Class<T> clazz) {
        try {
            while (node.fields().hasNext()) {
                node = node.fields().next().getValue();
            }
            return mapper.readValue(node.toString(), clazz);
        } catch (IOException e) {
            log.error("Parse value exception {}", e.getMessage());
        }
        return null;
    }
}
