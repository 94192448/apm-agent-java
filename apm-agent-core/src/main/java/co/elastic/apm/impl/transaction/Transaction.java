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
import co.elastic.apm.impl.context.Context;
import co.elastic.apm.impl.sampling.Sampler;
import co.elastic.apm.objectpool.Recyclable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Data captured by an agent representing an event occurring in a monitored service
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction implements Recyclable, co.elastic.apm.api.Transaction {

    /**
     * This counter helps to assign the spans with sequential IDs
     */
    private final AtomicInteger spanIdCounter = new AtomicInteger();

    /**
     * Context
     * <p>
     * Any arbitrary contextual information regarding the event, captured by the agent, optionally provided by the user
     */
    @JsonProperty("context")
    private final Context context = new Context();
    /**
     * Recorded time of the transaction, UTC based and formatted as YYYY-MM-DDTHH:mm:ss.sssZ
     * (Required)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    @JsonProperty("timestamp")
    private final Date timestamp = new Date(0);
    @JsonProperty("spans")
    private final List<Span> spans = new ArrayList<Span>();
    /**
     * A mark captures the timing of a significant event during the lifetime of a transaction. Marks are organized into groups and can be set by the user or the agent.
     */
    @JsonProperty("marks")
    private final Map<String, Object> marks = new HashMap<>();
    @JsonProperty("span_count")
    private final SpanCount spanCount = new SpanCount();
    /**
     * UUID for the transaction, referred by its spans
     * (Required)
     */
    @JsonProperty("id")
    private final TransactionId id = new TransactionId();
    @Nullable
    private transient ElasticApmTracer tracer;
    /**
     * How long the transaction took to complete, in ms with 3 decimal points
     * (Required)
     */
    @JsonProperty("duration")
    private double duration;
    /**
     * Generic designation of a transaction in the scope of a single service (eg: 'GET /users/:id')
     */
    @JsonProperty("name")
    private final StringBuilder name = new StringBuilder();
    /**
     * The result of the transaction. HTTP status code for HTTP-related transactions.
     */
    @Nullable
    @JsonProperty("result")
    private String result;
    /**
     * Keyword of specific relevance in the service's domain (eg: 'request', 'backgroundjob', etc)
     * (Required)
     */
    @Nullable
    @JsonProperty("type")
    private String type;
    /**
     * Transactions that are 'sampled' will include all available information. Transactions that are not sampled will not have 'spans' or 'context'. Defaults to true.
     */
    @JsonProperty("sampled")
    private boolean sampled;

    public Transaction start(ElasticApmTracer tracer, long startTimestampNanos, Sampler sampler) {
        this.tracer = tracer;
        this.duration = startTimestampNanos;
        this.timestamp.setTime(System.currentTimeMillis());
        this.id.setToRandomValue();
        this.sampled = sampler.isSampled(id);
        return this;
    }

    /**
     * Context
     * <p>
     * Any arbitrary contextual information regarding the event, captured by the agent, optionally provided by the user
     */
    @JsonProperty("context")
    public Context getContext() {
        return context;
    }

    /**
     * How long the transaction took to complete, in ms with 3 decimal points
     * (Required)
     */
    @JsonProperty("duration")
    public double getDuration() {
        return duration;
    }

    /**
     * UUID for the transaction, referred by its spans
     * (Required)
     */
    @JsonProperty("id")
    public TransactionId getId() {
        return id;
    }

    /**
     * Generic designation of a transaction in the scope of a single service (eg: 'GET /users/:id')
     */
    @JsonProperty("name")
    public StringBuilder getName() {
        return name;
    }

    /**
     * Generic designation of a transaction in the scope of a single service (eg: 'GET /users/:id')
     */
    @Override
    public void setName(@Nullable String name) {
        if (!sampled) {
            return;
        }
        this.name.setLength(0);
        this.name.append(name);
    }

    public Transaction withName(@Nullable String name) {
        if (!sampled) {
            return this;
        }
        setName(name);
        return this;
    }

    /**
     * The result of the transaction. HTTP status code for HTTP-related transactions.
     */
    @Nullable
    @JsonProperty("result")
    public String getResult() {
        return result;
    }

    /**
     * The result of the transaction. HTTP status code for HTTP-related transactions.
     */
    public Transaction withResult(@Nullable String result) {
        if (!sampled) {
            return this;
        }
        this.result = result;
        return this;
    }

    /**
     * Recorded time of the transaction, UTC based and formatted as YYYY-MM-DDTHH:mm:ss.sssZ
     * (Required)
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    public Transaction withTimestamp(long timestampEpoch) {
        if (!sampled) {
            return this;
        }
        this.timestamp.setTime(timestampEpoch);
        return this;
    }

    @JsonProperty("spans")
    public List<Span> getSpans() {
        return spans;
    }

    public Transaction addSpan(Span span) {
        if (!sampled) {
            return this;
        }
        synchronized (this) {
            spans.add(span);
        }
        return this;
    }

    /**
     * Keyword of specific relevance in the service's domain (eg: 'request', 'backgroundjob', etc)
     * (Required)
     */
    @Nullable
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Keyword of specific relevance in the service's domain (eg: 'request', 'backgroundjob', etc)
     * (Required)
     */
    @Override
    @JsonProperty("type")
    public void setType(@Nullable String type) {
        if (!sampled) {
            return;
        }
        this.type = type;
    }

    @Override
    public void addTag(String key, String value) {
        if (!sampled) {
            return;
        }
        getContext().getTags().put(key, value);
    }

    @Override
    public void setUser(String id, String email, String username) {
        if (!sampled) {
            return;
        }
        getContext().getUser().withId(id).withEmail(email).withUsername(username);
    }

    @Override
    public void end() {
        this.duration = (System.nanoTime() - duration) / ElasticApmTracer.MS_IN_NANOS;
        if (!sampled) {
            context.resetState();
        }
        if (this.tracer != null) {
            this.tracer.endTransaction(this);
        }
    }

    @Override
    public void close() {
        end();
    }

    public Transaction withType(@Nullable String type) {
        this.type = type;
        return this;
    }

    /**
     * A mark captures the timing of a significant event during the lifetime of a transaction. Marks are organized into groups and can be set by the user or the agent.
     */
    @JsonProperty("marks")
    public Map<String, Object> getMarks() {
        return marks;
    }

    /**
     * Transactions that are 'sampled' will include all available information.
     * Transactions that are not sampled will not have 'spans' or 'context'.
     * Defaults to true.
     */
    @JsonProperty("sampled")
    public boolean isSampled() {
        return sampled;
    }

    @JsonProperty("span_count")
    public SpanCount getSpanCount() {
        return spanCount;
    }


    @JsonIgnore
    int getNextSpanId() {
        return spanIdCounter.incrementAndGet();
    }

    @Override
    public void resetState() {
        context.resetState();
        duration = 0;
        id.resetState();
        name.setLength(0);
        result = null;
        timestamp.setTime(0);
        spans.clear();
        type = null;
        marks.clear();
        sampled = false;
        spanCount.resetState();
        tracer = null;
        spanIdCounter.set(0);
    }

    public void recycle() {
        if (tracer != null) {
            tracer.recycle(this);
        }
    }

}
