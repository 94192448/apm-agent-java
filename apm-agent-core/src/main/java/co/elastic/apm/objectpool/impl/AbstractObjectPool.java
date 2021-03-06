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
package co.elastic.apm.objectpool.impl;

import co.elastic.apm.objectpool.ObjectPool;
import co.elastic.apm.objectpool.Recyclable;
import co.elastic.apm.objectpool.RecyclableObjectFactory;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractObjectPool<T extends Recyclable> implements ObjectPool<T> {

    private final RecyclableObjectFactory<T> recyclableObjectFactory;
    private final AtomicInteger garbageCreated = new AtomicInteger();

    protected AbstractObjectPool(RecyclableObjectFactory<T> recyclableObjectFactory) {
        this.recyclableObjectFactory = recyclableObjectFactory;
    }

    @Override
    public T createInstance() {
        T recyclable = tryCreateInstance();
        if (recyclable == null) {
            // queue is empty, falling back to creating a new instance
            garbageCreated.incrementAndGet();
            return recyclableObjectFactory.createInstance();
        } else {
            return recyclable;
        }
    }

    @Override
    public void fillFromOtherPool(ObjectPool<T> otherPool, int maxElements) {
        for (int i = 0; i < maxElements; i++) {
            T obj = tryCreateInstance();
            if (obj == null) {
                return;
            }
            otherPool.recycle(obj);
        }
    }

    @Override
    public long getGarbageCreated() {
        return garbageCreated.longValue();
    }


}
