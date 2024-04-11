/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MultiPartContentUtils {
    private static final Pattern BOUNDARY_PATTERN = Pattern.compile("^(--\\w+)[\\r\\n]*((?s).*)");

    private MultiPartContentUtils() {
    }

    public static MultiPartContent parse(String content) {
        String boundary = getBoundary(content);
        if (StringUtils.isNoneBlank(boundary)) {
            return new MultiPartContent(getXmlSctPart(content, boundary),
                                        getJsonStandingOrderType(content, boundary));
        }
        log.error("Boundary is absent in multi-part content: {}", content);
        return new MultiPartContent();
    }

    private static String getJsonStandingOrderType(String content, String boundary) {
        return getPartByTypeAndStartText(content, boundary, MediaType.APPLICATION_JSON_VALUE, "{");
    }

    private static String getXmlSctPart(String content, String boundary) {
        return getPartByTypeAndStartText(content, boundary, MediaType.APPLICATION_XML_VALUE, "<Document");
    }

    private static String getPartByTypeAndStartText(String content, String boundary, String mediaType, String startText) {
        if (boundary != null) {
            String[] parts = content.split(boundary);
            if (parts.length >= 2) {
                for (String part : parts) {
                    if (part.contains(mediaType)) {
                        return part.substring(part.indexOf(startText)).trim();
                    }
                }
            }
        }
        return null;
    }

    private static String getBoundary(String content) {
        Matcher matcher = BOUNDARY_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiPartContent {
        private String xmlSct;
        private String jsonStandingOrderType;
    }
}
