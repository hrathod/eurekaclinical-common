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
import org.eurekaclinical.standardapis.dao.RoleDao;
import org.eurekaclinical.standardapis.exception.HttpStatusException;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.standardapis.entity.RoleEntity;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractRoleResource<E extends RoleEntity, R extends Role> {

    private final RoleDao<E> roleDao;

    public AbstractRoleResource(RoleDao<E> inRoleDao) {
        this.roleDao = inRoleDao;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<R> getAll() {
        List<R> results = new ArrayList<>();
        for (E roleEntity : this.roleDao.getAll()) {
            results.add(toRole(roleEntity));
        }
        return results;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public R getAny(@PathParam("id") Long inId, @Context HttpServletRequest req) {
        E roleEntity = this.roleDao.retrieve(inId);
        if (roleEntity == null) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else {
            return toRole(roleEntity);
        }
    }
    
    @GET
    @Path("/byname/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public R getAny(@PathParam("name") String inName, @Context HttpServletRequest req) {
        E roleEntity = this.roleDao.getRoleByName(inName);
        if (roleEntity == null) {
            throw new HttpStatusException(Response.Status.NOT_FOUND);
        } else {
            return toRole(roleEntity);
        }
    }

    protected abstract R toRole(E roleEntity);

}
