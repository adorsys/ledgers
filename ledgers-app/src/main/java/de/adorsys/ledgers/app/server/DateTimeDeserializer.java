/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.app.server;

import static de.adorsys.ledgers.app.server.ApiDateConstants.DATE_TIME_PATTERN;
import static de.adorsys.ledgers.app.server.ApiDateConstants.DATE_TIME_PATTERN_LOCAL;
import static de.adorsys.ledgers.app.server.ApiDateConstants.DATE_TIME_PATTERN_OFFSET;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class DateTimeDeserializer extends StdDeserializer<LocalDateTime> {
	Logger logger = LoggerFactory.getLogger(DateTimeDeserializer.class);
    private final DateTimeFormatter formatter;

    {
        formatter = new DateTimeFormatterBuilder()
                        .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_OFFSET))
                        .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_LOCAL))
                        .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                        .toFormatter();
    }

    public DateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            String date = jsonParser.getText();
            return LocalDateTime.parse(date, formatter);
        } catch (IOException | DateTimeParseException e) {
        	logger.error("Unsupported dateTime format!");
        }
        return null;
    }
}
