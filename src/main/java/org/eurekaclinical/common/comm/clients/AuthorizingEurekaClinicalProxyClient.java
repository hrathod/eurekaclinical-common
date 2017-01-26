package org.eurekaclinical.common.comm.clients;

/*-
 * #%L
 * Eureka! Clinical Common
 * %%
 * Copyright (C) 2016 - 2017 Emory University
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

import com.sun.jersey.api.client.GenericType;
import java.util.List;
import javax.ws.rs.ext.ContextResolver;
import org.codehaus.jackson.map.ObjectMapper;
import org.eurekaclinical.common.comm.Role;

/**
 *
 * @author arpost
 */
public abstract class AuthorizingEurekaClinicalProxyClient extends EurekaClinicalClient {

    private static final GenericType<List<Role>> RoleList = new GenericType<List<Role>>() {
    };

    protected AuthorizingEurekaClinicalProxyClient(Class<? extends ContextResolver<? extends ObjectMapper>> cls) {
        super(cls);
    }

    public List<Role> getRoles() throws ClientException {
        final String path = "/proxy-resource/roles";
        return doGet(path, RoleList);
    }

    public Role getRole(Long inRoleId) throws ClientException {
        final String path = "/proxy-resource/roles/" + inRoleId;
        return doGet(path, Role.class);
    }

    public Role getRoleByName(String name) throws ClientException {
        return doGet("/proxy-resource/roles/byname/" + name, Role.class);
    }
}
