package org.eurekaclinical.common.filter;

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
import org.eurekaclinical.standardapis.filter.AbstractRolesFilter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.common.comm.User;
import org.eurekaclinical.common.comm.clients.AuthorizingEurekaClinicalClient;
import org.eurekaclinical.common.comm.clients.ClientException;

/**
 * Filter that adds the user's roles from a REST API client to the request.
 * Users of this filter must bind
 * {@link AuthorizingEurekaClinicalClient} in their Guice configuration.
 *
 * @author Andrew Post
 */
@Singleton
public class RolesFromServiceFilter extends AbstractRolesFilter {

    private final Injector injector;

    @Inject
    public RolesFromServiceFilter(Injector inInjector) {
        this.injector = inInjector;
    }

    @Override
    protected String[] getRoles(Principal principal) throws ServletException {
        AuthorizingEurekaClinicalClient client = 
                this.injector.getInstance(AuthorizingEurekaClinicalClient.class);
        try {
            List<Role> roles = client.getRoles();//eureka project roles table
            User user = client.getMe();
            List<Long> roleIds = user.getRoles();

            List<String> roleNames = new ArrayList<>();
            for (Role role : roles) {
                if (roleIds.contains(role.getId())) {
                    roleNames.add(role.getName());
                }
            }
            return roleNames.toArray(new String[roleNames.size()]);
        } catch (ClientException ex) {
            throw new ServletException(ex);
        }
    }

}
