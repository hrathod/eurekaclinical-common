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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.eurekaclinical.standardapis.dao.DaoWithUniqueName;
import org.eurekaclinical.standardapis.entity.Entity;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractNamedReadOnlyResource<E extends Entity, C extends Object> extends AbstractReadOnlyResource<E, C> {

    private final GetByNameSupport<E, C> support;

    public AbstractNamedReadOnlyResource(DaoWithUniqueName<E, Long> inRoleDao) {
        super(inRoleDao);
        this.support = new GetByNameSupport<>(inRoleDao, this);
    }

    @GET
    @Path("/byname/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public C getByName(@PathParam("name") String inName, @Context HttpServletRequest req) {
        return this.support.get(inName, req);
    }

}
