/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.labs.todo.coherence;

import com.oracle.coherence.common.base.Logger;
import com.tangosol.io.pof.schema.annotation.PortableType;
import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.Processors;
import java.util.Collection;

/**
 * A {@link AbstractInvocable} which is called to preload data from the configured DB.
 */
@PortableType(id = 1002)
public class PreloadDataInvocable extends AbstractInvocable {
    public PreloadDataInvocable() {
    }

    @Override
    public void run() {
       CachePreloader cachePreloader = new CachePreloader(System.getProperty("coherence.hibernate.config"));
       Collection<String> keys       = cachePreloader.getAllKeys();
       String             message    = "Preload data, keys found=" + keys.size();
       Logger.info(message);

       CacheFactory.getCache("tasks").invokeAll(keys, Processors.preload());

       setResult(message);
    }
}
