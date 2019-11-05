package de.adorsys.ledgers.middleware.rest.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationFilter extends GenericFilterBean {
    private static final String IBAN = "iban";
    private static final String CURRENCY = "currency";
    private final CurrencyService currencyService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MultiReadHttpServletRequest servletRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);
        if (servletRequest.getContentLength() > 0) {
            try {
                Optional<JsonNode> invalidIban = validate(readValuesByField(servletRequest, IBAN), v -> IBANValidator.getInstance().isValid(v.asText()));
                if (invalidIban.isPresent()) {
                    log.error("Invalid IBAN: {}", invalidIban.get().asText());
                    ((HttpServletResponse) response).sendError(400, String.format("Invalid IBAN %s", invalidIban.get().asText()));
                    return;
                }
                Optional<JsonNode> invalidCurrency = validate(readValuesByField(servletRequest, CURRENCY),
                                                              n -> currencyService.getSupportedCurrencies().stream().anyMatch(c -> c.getCurrencyCode().hashCode() == n.hashCode()));
                if (invalidCurrency.isPresent()) {
                    log.error("Invalid currency: {}", invalidCurrency.get().asText());
                    ((HttpServletResponse) response).sendError(400, String.format("Invalid currency %s", invalidCurrency.get().asText()));
                    return;
                }
            } catch (IOException e) {
                String msg = String.format("Could not parse request body, msg: %s", e.getMessage());
                log.error(msg);
                ((HttpServletResponse) response).sendError(400, msg);
                return;
            }
        }
        chain.doFilter(servletRequest, response);
    }

    private List<JsonNode> readValuesByField(MultiReadHttpServletRequest servletRequest, String fieldName) throws IOException {
        return objectMapper.readTree(servletRequest.getInputStream())
                       .findValues(fieldName);
    }

    private Optional<JsonNode> validate(List<JsonNode> values, Predicate<JsonNode> predicate) {
        return values.stream()
                       .filter(predicate.negate())
                       .findFirst();
    }
}
