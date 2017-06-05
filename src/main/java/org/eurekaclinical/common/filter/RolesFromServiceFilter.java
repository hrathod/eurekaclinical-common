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
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.common.comm.User;
import org.eurekaclinical.common.comm.clients.AuthorizingEurekaClinicalProxyClient;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.standardapis.filter.RolesFilter;
import org.eurekaclinical.standardapis.filter.RolesRequestWrapper;

/**
 * To use this filter, bind an implementation of 
 * {@link AuthorizingEurekaClinicalProxyClient} in your Guice module.
 * 
 * @author Andrew Post
 */
@Singleton
public class RolesFromServiceFilter implements RolesFilter {

    private final Injector injector;

    @Inject
    public RolesFromServiceFilter(Injector inInjector) {
        this.injector = inInjector;
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest inRequest, ServletResponse inResponse, FilterChain inChain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) inRequest;
        String username = servletRequest.getRemoteUser();
        if (username != null) {
            AuthorizingEurekaClinicalProxyClient client = this.injector.getInstance(AuthorizingEurekaClinicalProxyClient.class);
            try {
                HttpSession session = servletRequest.getSession(false);
                assert session != null : "session should not be null";

                Principal principal = servletRequest.getUserPrincipal();
                assert principal != null : "principal should not be null";
                
                List<Role> roles = client.getRoles();//eureka project roles table
                User user = client.getMe();
                List<Long> roleIds = user.getRoles();

                Set<String> roleNames = new HashSet<>();
                for (Role role : roles) {
                    if (roleIds.contains(role.getId())) {
                        roleNames.add(role.getName());
                    }
                }

                HttpServletRequest wrappedRequest = new RolesRequestWrapper(
                        servletRequest, principal, roleNames);

                inChain.doFilter(wrappedRequest, inResponse);
            } catch (ClientException ce) {
                throw new ServletException(ce);
            }
        } else {
            inChain.doFilter(inRequest, inResponse);
        }

    }

    @Override
    public void destroy() {
    }
}
