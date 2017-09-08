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

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eurekaclinical.standardapis.dao.Dao;
import org.eurekaclinical.standardapis.entity.Entity;
import org.eurekaclinical.standardapis.exception.HttpStatusException;

/**
 *
 * @author Andrew Post
 * @param <E> the entity class
 * @param <C> the comm class
 */
public abstract class AbstractResource<E extends Entity, C extends Object> {
    private final Dao<E, Long> dao;
    private final boolean restricted;
    
    protected AbstractResource(Dao<E, Long> inDao) {
        this(inDao, true);
    }
    
    protected AbstractResource(Dao<E, Long> inDao, boolean inRestricted) {
        this.dao = inDao;
        this.restricted = inRestricted;
    }

    public boolean isRestricted() {
        return restricted;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<C> getAll(@Context HttpServletRequest req) {
        if (isRestricted() && !req.isUserInRole("admin")) {
            throw new HttpStatusException(Response.Status.FORBIDDEN);
        }
        List<C> results = new ArrayList<>();
        for (E userEntity : this.dao.getAll()) {
            results.add(toComm(userEntity, req));
        }
        return results;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public C getAny(@PathParam("id") Long inId, @Context HttpServletRequest req) {
        E entity = this.dao.retrieve(inId);
        if (entity == null) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else if (isAuthorizedEntity(entity, req) && (!isRestricted() || req.isUserInRole("admin"))) {
            return toComm(entity, req);
        } else {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        }
    }

    protected abstract C toComm(E entity, HttpServletRequest req);
    
    protected abstract boolean isAuthorizedEntity(E entity, HttpServletRequest req);
    
}
