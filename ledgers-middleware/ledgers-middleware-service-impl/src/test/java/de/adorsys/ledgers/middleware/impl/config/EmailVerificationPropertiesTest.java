/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmailVerificationPropertiesTest {

    @Test
    void createConfig() {
        EmailVerificationProperties properties = new EmailVerificationProperties();
        properties.setEndPoint("endp");
        EmailVerificationProperties.Page page = new EmailVerificationProperties.Page();
        page.setFail("fail");
        page.setSuccess("success");
        properties.setPage(page);

        EmailVerificationProperties.EmailTemplate template = new EmailVerificationProperties.EmailTemplate();
        template.setFrom("from");
        template.setSubject("subj");
        template.setMessage("msg");
        properties.setTemplate(template);
        properties.setExtBasePath("path");

        assertNotNull(properties);
        assertNotNull(properties.getPage().getFail());
        assertNotNull(properties.getPage().getSuccess());
        assertNotNull(properties.getEndPoint());
        assertNotNull(properties.getTemplate().getFrom());
        assertNotNull(properties.getTemplate().getSubject());
        assertNotNull(properties.getTemplate().getMessage());
        assertNotNull(properties.getExtBasePath());
    }

}