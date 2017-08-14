package org.eurekaclinical.common.config;

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

import com.google.inject.Injector;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.common.comm.User;
import org.eurekaclinical.common.comm.clients.AuthorizingEurekaClinicalProxyClient;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session listener that sets a session attribute, <code>roles</code>, which is 
 * a {@link java.util.List} of role names.
 * 
 * @author Andrew Post
 */
public class RolesSessionListener implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RolesSessionListener.class);

    private final Injector injector;

    @Inject
    public RolesSessionListener(Injector inInjector) {
        this.injector = inInjector;
    }

    @Override
    public void sessionCreated(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        @SuppressWarnings("unchecked")
        AuthorizingEurekaClinicalProxyClient client = this.injector.getInstance(AuthorizingEurekaClinicalProxyClient.class);
        try {
            List<Role> roles = client.getRoles();//eureka project roles table
            User user = client.getMe();
            List<Long> roleIds = user.getRoles();

            Set<String> roleNames = new HashSet<>();
            for (Role role : roles) {
                if (roleIds.contains(role.getId())) {
                    roleNames.add(role.getName());
                }
            }
            session.setAttribute("roles", roleNames);
        } catch (ClientException ex) {
            LOGGER.error("Error getting roles", ex);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        hse.getSession().setAttribute("roles", null);
    }

}
