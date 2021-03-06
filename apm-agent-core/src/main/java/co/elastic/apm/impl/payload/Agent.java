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

package co.elastic.apm.impl.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Name and version of the Elastic APM agent
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agent {

    /**
     * Name of the Elastic APM agent, e.g. "Python"
     * (Required)
     */
    @JsonProperty("name")
    private final String name;
    /**
     * Version of the Elastic APM agent, e.g."1.0.0"
     * (Required)
     */
    @JsonProperty("version")
    private final String version;

    public Agent(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Name of the Elastic APM agent, e.g. "Python"
     * (Required)
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Version of the Elastic APM agent, e.g."1.0.0"
     * (Required)
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

}
