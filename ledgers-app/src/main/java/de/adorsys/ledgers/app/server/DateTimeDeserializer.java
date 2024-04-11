/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateTimeDeserializer extends StdDeserializer<LocalDateTime> implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;

    public DateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            String date = jsonParser.getText();
            return LocalDateTime.parse(date);
        } catch (IOException | DateTimeParseException e) {
            log.error("Unsupported dateTime format!");
        }
        return null;
    }
}
