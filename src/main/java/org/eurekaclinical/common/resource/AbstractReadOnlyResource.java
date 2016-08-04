package org.eurekaclinical.common.resource;

/*-
 * #%L
 * Eureka! Clinical Common
 * %%
 * Copyright (C) 2016 Emory University
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

import org.eurekaclinical.standardapis.dao.Dao;
import org.eurekaclinical.standardapis.entity.Entity;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractReadOnlyResource<E extends Entity, C extends Object> extends AbstractResource<E, C> {
    
    protected AbstractReadOnlyResource(Dao<E, Long> inDao) {
        super(inDao);
    }

    protected AbstractReadOnlyResource(Dao<E, Long> inDao, boolean inRestricted) {
        super(inDao, inRestricted);
    }
    
}
