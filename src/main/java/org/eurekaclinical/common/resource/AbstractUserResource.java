package org.eurekaclinical.common.resource;

/*-
 * #%L
 * Eureka! Clinical User Agreement Service
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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eurekaclinical.standardapis.dao.UserDao;
import org.eurekaclinical.standardapis.exception.HttpStatusException;
import org.eurekaclinical.common.comm.User;
import org.eurekaclinical.standardapis.entity.RoleEntity;
import org.eurekaclinical.standardapis.entity.UserEntity;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractUserResource<U extends User, E extends UserEntity<R>, R extends RoleEntity> {

    private final UserDao<E> userDao;

    public AbstractUserResource(UserDao<E> inUserDao) {
        this.userDao = inUserDao;
    }

    @RolesAllowed("admin")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<U> getAll() {
        List<U> results = new ArrayList<>();
        for (E userEntity : this.userDao.getAll()) {
            results.add(toUser(userEntity));
        }
        return results;
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public U getCurrent(@Context HttpServletRequest req) {
        E me = this.userDao.getByUsername(req.getRemoteUser());
        if (me == null) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else {
            return toUser(me);
        }
    }
    
    @GET
    @Path("/byname/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public U getCurrent(@PathParam("username") String username, @Context HttpServletRequest req) {
        E userEntity = this.userDao.getByUsername(username);
        if (userEntity == null) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else if (req.getRemoteUser().equals(userEntity.getUsername())) {
            return toUser(userEntity);
        } else {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public U getAny(@PathParam("id") Long inId, @Context HttpServletRequest req) {
        E userEntity = this.userDao.retrieve(inId);
        if (userEntity == null) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else if (req.getRemoteUser().equals(userEntity.getUsername())) {
            return toUser(userEntity);
        } else {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(U user, @Context HttpServletRequest req) {
        if (req.getRemoteUser().equals(user.getUsername()) || req.isUserInRole("admin")) {
            this.userDao.update(toUserEntity(user));
        } else {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        }
    }

    @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(U user) {
        UserEntity userEntity = toUserEntity(user);
        return Response.created(URI.create("/" + userEntity.getId())).build();
    }
    
    protected abstract U toUser(E userEntity);
    
    protected abstract E toUserEntity(U user);

}
