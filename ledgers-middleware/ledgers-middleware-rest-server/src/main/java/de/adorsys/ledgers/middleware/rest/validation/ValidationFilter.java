package de.adorsys.ledgers.middleware.rest.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.validator.routines.IBANValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


@Component
public class ValidationFilter extends GenericFilterBean {
    private final Logger logger = LoggerFactory.getLogger(ValidationFilter.class);
    private final ObjectMapper objectMapper;

    public ValidationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MultiReadHttpServletRequest servletRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);
        if (servletRequest.getContentLength() > 0) {
            List<JsonNode> values = objectMapper.readTree(servletRequest.getInputStream())
                                            .findValues("iban");
            for (JsonNode node : values) {
                boolean valid = IBANValidator.getInstance().isValid(node.asText());
                if (!valid) {
                    logger.error("Invalid IBAN: {}", node.asText());
                    ((HttpServletResponse) response).sendError(400, String.format("Invalid IBAN %s", node.asText()));
                    return;
                }
            }
        }
        chain.doFilter(servletRequest, response);
    }
}


