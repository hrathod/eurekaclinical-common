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
 * Base class for creating Jersey root resource classes. It provides 
 * implementations of REST APIs for getting all objects accessed through this
 * resource and for getting specific objects by its unique id. It additionally
 * allows configuring the resource to grant users with the <code>admin</code>
 * role access to all objects through these two APIs even if they are not the
 * owner of the object, and even if they are not a member of a group with
 * access to the object.
 * 
 * @author Andrew Post
 * 
 * @param <E> the entity class
 * @param <C> the comm class
 */
public abstract class AbstractResource<E extends Entity, C extends Object> {
    private final Dao<E, Long> dao;
    private final boolean restricted;
    
    /**
     * Creates an instance of a resource that will use the given data access
     * object for database queries.
     * 
     * @param inDao the data access object. Cannot be <code>null</code>.
     */
    protected AbstractResource(Dao<E, Long> inDao) {
        this(inDao, true);
    }
    
    /**
     * Creates an instance of a resource that will use the given data access
     * object for database queries. This constructor additionally permits
     * authorizing admin users for read-only access to objects that they
     * are not otherwise authorized to access through Eureka! Clinical's 
     * group and owner permissions.
     * 
     * @param inDao the data access object. Cannot be <code>null</code>.
     * @param inRestricted <code>false</code> to grant users with the 
     * <code>admin</code> role read-only access to objects that they are not
     * otherwise authorized to access through Eureka! Clinical's group and
     * owner permissions. Setting this parameter to <code>true</code> achieves
     * the same behavior as the one-argument constructor, which grants all
     * users access only to objects that they own or otherwise have access to
     * through being a member of a group.
     */
    protected AbstractResource(Dao<E, Long> inDao, boolean inRestricted) {
        this.dao = inDao;
        this.restricted = inRestricted;
    }

    /**
     * Whether or not admin users have read-only access to all objects, even
     * if they are not the object's owner, and even if they are not a member
     * of a group that has access to the object.
     * 
     * @return <code>false</code> if admin users do have these extra
     * privileges, <code>false</code> if they do not. The default value is
     * <code>true</code>.
     */
    public boolean isRestricted() {
        return restricted;
    }
    
    /**
     * Gets all objects managed by this resource. By default, only users with
     * the <code>admin</code> role are authorized to use this API. Setting
     * the <code>restricted</code> field to <code>false</code> in the two-
     * argument constructor will change this behavior so that non-admin users
     * may also make this API call.
     * 
     * @param req the HTTP servlet request.
     * @return the list of objects managed by this resource.
     * 
     * @throws HttpStatusException if an error occurred, for example, the user
     * is not authorized to make this call.
     */
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

    /**
     * Gets the object with the given unique identifier.
     * 
     * @param inId the unique identifier. Cannot be <code>null</code>.
     * @param req the HTTP servlet request.
     * @return  the object with the given unique identifier. Guaranteed not
     * <code>null</code>.
     * 
     * @throws HttpStatusException if there is no object with the given 
     * unique identifier, or if the user is not authorized to access the 
     * object.
     */
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

    /**
     * Converts a JPA entity to a Jersey POJO. Your implementation of this
     * method should create a Jersey POJO and copy the entity's fields into the
     * corresponding fields of the Jersey POJO.
     * 
     * @param entity the entity. Cannot be <code>null</code>.
     * @param req the HTTP servlet request.
     * @return the Jersey POJO. Guaranteed not <code>null</code>.
     */
    protected abstract C toComm(E entity, HttpServletRequest req);
    
    /**
     * Returns whether the requesting user is authorized to access an entity.
     * 
     * @param entity the entity.
     * @param req the HTTP servlet request.
     * @return <code>true</code> if the current user is authorized to access 
     * the entity, <code>false</code> otherwise.
     */
    protected abstract boolean isAuthorizedEntity(E entity, HttpServletRequest req);
    
}
