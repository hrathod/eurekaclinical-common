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
import com.sun.jersey.api.client.ClientResponse;
import org.eurekaclinical.standardapis.filter.AbstractRolesFilter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.common.comm.User;
import org.eurekaclinical.common.comm.clients.AuthorizingEurekaClinicalClient;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that adds the user's roles from a REST API client to the request.
 * Users of this filter must bind {@link AuthorizingEurekaClinicalClient} in
 * their Guice configuration. It accepts an init parameter, 
 * <code>protectedPath</code>, which takes a path corresponding to the part of 
 * the web application that requires authentication. The filter will ignore 
 * HTTP status codes under 500 when trying to get the user's role list if the
 * requested path does not start with the application's protected path.
 *
 * @author Andrew Post
 */
@Singleton
public class RolesFromServiceFilter extends AbstractRolesFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RolesFromServiceFilter.class);

    private final Injector injector;
    private String protectedPath;

    @Inject
    public RolesFromServiceFilter(Injector inInjector) {
        this.injector = inInjector;
    }
    
    /**
     * Does nothing.
     *
     * @param fc the filter configuration.
     * 
     */
    @Override
    public void init(FilterConfig fc) {
        this.protectedPath = fc.getInitParameter("protectedPath");
    }

    /**
     * Gets the user's roles from the service that maintains the user's role 
     * information.
     * 
     * @param inPrincipal the user principal.
     * @param inRequest the request.
     * @return the user's roles.
     * 
     * @throws ServletException if the service responded with a 500 status,
     * the <code>protectedPath</code> init parameter was not given or is
     * <code>null</code>, or the requested servlet path starts with the 
     * protected path.
     */
    @Override
    protected String[] getRoles(Principal inPrincipal, ServletRequest inRequest) throws ServletException {
        AuthorizingEurekaClinicalClient client
                = this.injector.getInstance(AuthorizingEurekaClinicalClient.class);
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
            ClientResponse.Status responseStatus = ex.getResponseStatus();
            if (responseStatus.getStatusCode() >= 500 
                    || this.protectedPath == null 
                    || ((HttpServletRequest) inRequest).getServletPath().startsWith(this.protectedPath)) {
                throw new ServletException("The user's role information is not available", ex);
            } else {
                LOGGER.debug("The user's role information is not available", ex);
            }
            return null;
        }
    }

}
