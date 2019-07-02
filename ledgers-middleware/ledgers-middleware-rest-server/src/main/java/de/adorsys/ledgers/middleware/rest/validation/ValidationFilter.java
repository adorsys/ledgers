package de.adorsys.ledgers.middleware.rest.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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


@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationFilter extends GenericFilterBean {
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MultiReadHttpServletRequest servletRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);
        if (servletRequest.getContentLength() > 0) {
            List<JsonNode> values = objectMapper.readTree(servletRequest.getInputStream())
                                            .findValues("iban");
            for (JsonNode node : values) {
                boolean valid = IBANValidator.getInstance().isValid(node.asText());
                if (!valid) {
                    log.error("Invalid IBAN: {}", node.asText());
                    ((HttpServletResponse) response).sendError(400, String.format("Invalid IBAN %s", node.asText()));
                    return;
                }
            }
        }
        chain.doFilter(servletRequest, response);
    }
}


