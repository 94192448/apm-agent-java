/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 the original author or authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package co.elastic.apm.impl.error;

import co.elastic.apm.impl.payload.Payload;
import co.elastic.apm.impl.payload.ProcessInfo;
import co.elastic.apm.impl.payload.Service;
import co.elastic.apm.impl.payload.SystemInfo;
import co.elastic.apm.objectpool.Recyclable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


/**
 * Errors payload
 * <p>
 * List of errors wrapped in an object containing some other attributes normalized away from the errors themselves
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorPayload extends Payload {

    /**
     * (Required)
     */
    @JsonProperty("errors")
    private final List<ErrorCapture> errors = new ArrayList<ErrorCapture>();

    public ErrorPayload(ProcessInfo process, Service service, SystemInfo system) {
        super(process, service, system);
    }

    /**
     * (Required)
     */
    @JsonProperty("errors")
    public List<ErrorCapture> getErrors() {
        return errors;
    }

    @Override
    public List<? extends Recyclable> getPayloadObjects() {
        return errors;
    }

    @Override
    public void recycle() {
        for (ErrorCapture error : errors) {
            error.recycle();
        }
    }

    @Override
    public void resetState() {
        errors.clear();
    }

}
