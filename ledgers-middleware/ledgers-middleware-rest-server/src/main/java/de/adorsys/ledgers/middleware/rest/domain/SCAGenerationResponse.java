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

package de.adorsys.ledgers.middleware.rest.domain;

import java.util.Objects;

public class SCAGenerationResponse {
    private String opId;

    public SCAGenerationResponse() {
    }

    public SCAGenerationResponse(String opId) {
        this.opId = opId;
    }

    public String getOpId() {
        return opId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SCAGenerationResponse that = (SCAGenerationResponse) o;
        return Objects.equals(opId, that.opId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opId);
    }
}
