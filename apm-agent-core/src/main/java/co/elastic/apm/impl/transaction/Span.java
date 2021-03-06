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
package co.elastic.apm.impl.transaction;

import co.elastic.apm.impl.ElasticApmTracer;
import co.elastic.apm.impl.stacktrace.Stacktrace;
import co.elastic.apm.objectpool.Recyclable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static co.elastic.apm.impl.ElasticApmTracer.MS_IN_NANOS;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Span implements Recyclable, co.elastic.apm.api.Span {

    /**
     * Any other arbitrary data captured by the agent, optionally provided by the user
     */
    @JsonProperty("context")
    private final SpanContext context = new SpanContext();
    /**
     * List of stack frames with variable attributes (eg: lineno, filename, etc)
     */
    @JsonProperty("stacktrace")
    private final List<Stacktrace> stacktrace = new ArrayList<Stacktrace>();
    /**
     * The locally unique ID of the span.
     */
    @JsonProperty("id")
    private final SpanId id = new SpanId();
    @Nullable
    private transient ElasticApmTracer tracer;
    private transient boolean sampled;
    /**
     * Duration of the span in milliseconds
     * (Required)
     */
    @JsonProperty("duration")
    private double duration;
    /**
     * Generic designation of a span in the scope of a transaction
     * (Required)
     */
    @Nullable
    @JsonProperty("name")
    private String name;
    /**
     * The locally unique ID of the parent of the span.
     */
    @JsonProperty("parent")
    private SpanId parent = new SpanId();
    /**
     * Offset relative to the transaction's timestamp identifying the start of the span, in milliseconds
     * (Required)
     */
    @JsonProperty("start")
    private double start;
    /**
     * Keyword of specific relevance in the service's domain (eg: 'db.postgresql.query', 'template.erb', etc)
     * (Required)
     */
    @Nullable
    @JsonProperty("type")
    private String type;

    public Span start(ElasticApmTracer tracer, Transaction transaction, @Nullable Span span, long nanoTime, boolean dropped) {
        this.tracer = tracer;
        this.id.setLong(transaction.getNextSpanId());
        if (span != null) {
            this.parent.copyFrom(span.getId());
        }
        this.sampled = transaction.isSampled() && !dropped;
        if (sampled) {
            start = (nanoTime - transaction.getDuration()) / MS_IN_NANOS;
            duration = nanoTime;
            transaction.addSpan(this);
        }
        return this;
    }

    /**
     * The locally unique ID of the span.
     */
    @JsonProperty("id")
    public SpanId getId() {
        return id;
    }

    /**
     * Any other arbitrary data captured by the agent, optionally provided by the user
     */
    @JsonProperty("context")
    public SpanContext getContext() {
        return context;
    }

    /**
     * Duration of the span in milliseconds
     * (Required)
     */
    @JsonProperty("duration")
    public double getDuration() {
        return duration;
    }

    /**
     * Generic designation of a span in the scope of a transaction
     * (Required)
     */
    @Nullable
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Generic designation of a span in the scope of a transaction
     * (Required)
     */
    @Override
    public void setName(@Nullable String name) {
        withName(name);
    }

    public Span withName(@Nullable String name) {
        if (!sampled) {
            return this;
        }
        this.name = name;
        return this;
    }

    /**
     * The locally unique ID of the parent of the span.
     */
    @JsonProperty("parent")
    public SpanId getParent() {
        return parent;
    }

    /**
     * List of stack frames with variable attributes (eg: lineno, filename, etc)
     */
    @JsonProperty("stacktrace")
    public List<Stacktrace> getStacktrace() {
        return stacktrace;
    }

    /**
     * Offset relative to the transaction's timestamp identifying the start of the span, in milliseconds
     * (Required)
     */
    @JsonProperty("start")
    public double getStart() {
        return start;
    }

    /**
     * Keyword of specific relevance in the service's domain (eg: 'db.postgresql.query', 'template.erb', etc)
     * (Required)
     */
    @Nullable
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Keyword of specific relevance in the service's domain (eg: 'db.postgresql.query', 'template.erb', etc)
     * (Required)
     */
    @Override
    public void setType(@Nullable String type) {
        withType(type);
    }

    @Override
    public void end() {
        end(System.nanoTime());
    }

    public void end(long nanoTime) {
        if (isSampled()) {
            this.duration = (nanoTime - duration) / MS_IN_NANOS;
        }
        if (this.tracer != null) {
            this.tracer.endSpan(this);
        }
    }

    @Override
    public void close() {
        end();
    }

    public Span withType(@Nullable String type) {
        if (!sampled) {
            return this;
        }
        this.type = type;
        return this;
    }

    @JsonIgnore
    public boolean isSampled() {
        return sampled;
    }

    @Override
    public void resetState() {
        id.resetState();
        context.resetState();
        duration = 0;
        name = null;
        parent.resetState();
        stacktrace.clear();
        start = 0;
        type = null;
        tracer = null;
        sampled = false;
    }

}
