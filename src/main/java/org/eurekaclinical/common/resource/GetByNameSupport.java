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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.eurekaclinical.standardapis.dao.DaoWithUniqueName;
import org.eurekaclinical.standardapis.entity.Entity;
import org.eurekaclinical.standardapis.exception.HttpStatusException;

/**
 *
 * @author Andrew Post
 */
class GetByNameSupport<E extends Entity, C extends Object> {
    private final DaoWithUniqueName<E, Long> dao;
    private final AbstractResource<E, C> resource;
    
    GetByNameSupport(DaoWithUniqueName<E, Long> inDao, AbstractResource<E, C> inResource) {
        this.dao = inDao;
        this.resource = inResource;
    }
    C get(String inName, HttpServletRequest req) {
        E groupEntity = this.dao.getByName(inName);
        if (groupEntity == null || (!this.resource.isAuthorizedEntity(groupEntity, req) && !req.isUserInRole("admin"))) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else {
            return this.resource.toComm(groupEntity, req);
        }
    }
}
