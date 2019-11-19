package de.adorsys.ledgers.middleware.rest.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_XML;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationFilter extends OncePerRequestFilter {
    private static final String IBAN = "iban";
    private static final String CURRENCY = "currency";

    private final CurrencyService currencyService;
    private final ObjectMapper mapper;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        MultiReadHttpServletRequest servletRequest = new MultiReadHttpServletRequest(request);
        boolean isXmlContent = StringUtils.equals(APPLICATION_XML.toString(), servletRequest.getHeader(CONTENT_TYPE));
        if (isXmlContent) {
            // TODO implement iban validation for xml payment
            chain.doFilter(servletRequest, response);
            return;
        }
        try {
            Supplier<Optional<String>> supplierIban = () -> validate(readValuesByField(servletRequest, IBAN), v -> IBANValidator.getInstance().isValid(v));
            Supplier<Optional<String>> supplierCurrency = () -> validate(readValuesByField(servletRequest, CURRENCY), this::isSupportedCurrency);
            Optional<String> combined = findFirstPresent(supplierIban, supplierCurrency);
            if (combined.isPresent()) {
                buildError(response, combined.get());
                return;
            }
        } catch (IOException e) {
            response.sendError(400, String.format("Could not parse request body, msg: %s", e.getMessage()));
            return;
        }
        chain.doFilter(servletRequest, response);
    }

    private Optional<String> validate(Collection<String> values, Predicate<String> predicate) {
        return values.stream().filter(predicate.negate())
                       .findFirst();
    }

    @SneakyThrows
    private Collection<String> readValuesByField(MultiReadHttpServletRequest servletRequest, String fieldName) {
        JsonNode jsonNode = mapper.readTree(servletRequest.getInputStream());
        return jsonNode != null
                       ? jsonNode.findValuesAsText(fieldName)
                       : CollectionUtils.emptyCollection();
    }

    private boolean isSupportedCurrency(String currency) {
        return currencyService.getSupportedCurrencies().stream()
                       .anyMatch(c -> StringUtils.equals(c.getCurrencyCode(), currency));
    }

    @SafeVarargs
    private static Optional<String> findFirstPresent(Supplier<Optional<String>>... suppliers) {
        return Stream.of(suppliers)
                       .map(Supplier::get)
                       .filter(Optional::isPresent)
                       .findFirst()
                       .orElseGet(Optional::empty);
    }

    private void buildError(HttpServletResponse response, String value) throws IOException {
        log.error("Invalid value: {}", value);
        response.sendError(400, String.format("Invalid value: %s", value));
    }
}
