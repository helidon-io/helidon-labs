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

import java.io.File;
import java.util.Collection;
import com.oracle.coherence.hibernate.cachestore.HibernateCacheLoader;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class CachePreloader extends HibernateCacheLoader {

    public CachePreloader(String coherenceHibernateConfig) {
        super("io.helidon.labs.todo.coherence.Task", new File(coherenceHibernateConfig));
    }

    public Collection<String> getAllKeys() {
        ensureInitialized();
        Session session = openSession();
        
        try {
            Query<String> query = session.createQuery("SELECT e.id FROM " + getEntityName() + " e", String.class);
            return query.list();
        } finally {
            closeSession(session);
        }
    }
}
