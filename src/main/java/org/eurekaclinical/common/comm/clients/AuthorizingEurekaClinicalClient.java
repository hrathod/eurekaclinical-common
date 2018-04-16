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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.common.comm.User;

/**
 * Base class for creating REST API clients that implement Eureka! Clinical's
 * standard users and roles APIs.
 * 
 * @author Andrew Post
 */
public abstract class AuthorizingEurekaClinicalClient extends EurekaClinicalClient {

    private static final GenericType<List<User>> UserList = new GenericType<List<User>>() {
    };
    private static final GenericType<List<Role>> RoleList = new GenericType<List<Role>>() {
    };

    protected AuthorizingEurekaClinicalClient(Class<? extends ContextResolver<? extends ObjectMapper>> cls) {
        super(cls);
    }
    
    public List<? extends User> getUsers() throws ClientException {
        return getUsers(null);
    }

    public List<? extends User> getUsers(String authToken) throws ClientException {
        final String path = "/api/protected/users";
        MultivaluedMapImpl headers = null;
        if (authToken != null) {
            String key = "Authorization";
            String value = "BEARER " + authToken;
            headers = new MultivaluedMapImpl();
            headers.putSingle(key, value);
        }
        return doGet(path, UserList, headers);
    }

    public User getMe() throws ClientException {
        return getMe(null);
    }

    public User getMe(String authToken) throws ClientException {
        String path = "/api/protected/users/me";
        MultivaluedMapImpl headers = null;
        if (authToken != null) {
            String key = "Authorization";
            String value = "BEARER " + authToken;
            headers = new MultivaluedMapImpl();
            headers.putSingle(key, value);
        }
        return doGet(path, User.class, headers);
    }

    public User getUserById(Long inUserId) throws ClientException {
        return getUserById(inUserId, null);
    }

    public User getUserById(Long inUserId, String authToken) throws ClientException {
        final String path = "/api/protected/users/" + inUserId;
        MultivaluedMapImpl headers = null;
        if (authToken != null) {
            String key = "Authorization";
            String value = "BEARER " + authToken;
            headers = new MultivaluedMapImpl();
            headers.putSingle(key, value);
        }
        return doGet(path, User.class, headers);
    }

    public List<Role> getRoles() throws ClientException {
        return getRoles(null);
    }

    public List<Role> getRoles(String authToken) throws ClientException {
        final String path = "/api/protected/roles";
        MultivaluedMapImpl headers = null;
        if (authToken != null) {
            String key = "Authorization";
            String value = "BEARER " + authToken;
            headers = new MultivaluedMapImpl();
            headers.putSingle(key, value);
        }
        return doGet(path, RoleList, headers);
    }

    public Role getRole(Long inRoleId) throws ClientException {
        return getRole(null);
    }

    public Role getRole(Long inRoleId, String authToken) throws ClientException {
        final String path = "/api/protected/roles/" + inRoleId;
        MultivaluedMapImpl headers = null;
        if (authToken != null) {
            String key = "Authorization";
            String value = "BEARER " + authToken;
            headers = new MultivaluedMapImpl();
            headers.putSingle(key, value);
        }
        return doGet(path, Role.class, headers);
    }

    public Role getRoleByName(String name) throws ClientException {
        return getRoleByName(name, null);
    }

    public Role getRoleByName(String name, String authToken) throws ClientException {
        MultivaluedMapImpl headers = null;
        if (authToken != null) {
            String key = "Authorization";
            String value = "BEARER " + authToken;
            headers = new MultivaluedMapImpl();
            headers.putSingle(key, value);
        }
        return doGet("/api/protected/roles/byname/" + name, Role.class, headers);
    }
}
