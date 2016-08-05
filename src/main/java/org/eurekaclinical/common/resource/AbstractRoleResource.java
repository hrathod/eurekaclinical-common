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
import org.eurekaclinical.standardapis.dao.RoleDao;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.standardapis.entity.RoleEntity;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractRoleResource<E extends RoleEntity, R extends Role> extends AbstractNamedReadOnlyResource<E, R> {

    private final RoleDao<E> roleDao;

    public AbstractRoleResource(RoleDao<E> inRoleDao) {
        super(inRoleDao, false);
        this.roleDao = inRoleDao;
    }

    @Override
    protected boolean isAuthorizedEntity(RoleEntity entity, HttpServletRequest req) {
        return true;
    }

}
