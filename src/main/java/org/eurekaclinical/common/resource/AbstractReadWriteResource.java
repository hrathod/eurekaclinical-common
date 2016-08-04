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

import java.net.URI;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eurekaclinical.standardapis.dao.Dao;
import org.eurekaclinical.standardapis.entity.Entity;
import org.eurekaclinical.standardapis.exception.HttpStatusException;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractReadWriteResource<E extends Entity, C extends Object> extends AbstractResource<E, C> {
    private final Dao<E, Long> dao;
    
    protected AbstractReadWriteResource(Dao<E, Long> inDao) {
        this(inDao, true);
    }
    
    protected AbstractReadWriteResource(Dao<E, Long> inDao, boolean inRestricted) {
        super(inDao, inRestricted);
        this.dao = inDao;
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(C commObj, @Context HttpServletRequest req) {
        if (isAuthorizedComm(commObj, req) && (!isRestricted() || req.isUserInRole("admin"))) {
            this.dao.update(toEntity(commObj));
        } else {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(C user, @Context HttpServletRequest req) {
        if (isRestricted() && !req.isUserInRole("admin")) {
            throw new HttpStatusException(Response.Status.FORBIDDEN);
        }
        E entity = toEntity(user);
        return Response.created(URI.create("/" + entity.getId())).build();
    }

    protected abstract E toEntity(C commObj);
    
    protected abstract boolean isAuthorizedComm(C commObj, HttpServletRequest req);
}
