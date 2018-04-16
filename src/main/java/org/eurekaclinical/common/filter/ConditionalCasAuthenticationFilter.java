package org.eurekaclinical.common.filter;

/*-
 * #%L
 * Eureka! Clinical Common
 * %%
 * Copyright (C) 2016 - 2018 Emory University
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

import org.jasig.cas.client.authentication.AuthenticationFilter;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * This class implements a CAS authentication filter, based on the
 * condition that the request has not already been authenticated.
 *
 * @author hrathod
 */
@Singleton
public class ConditionalCasAuthenticationFilter implements Filter {

    private final AuthenticationFilter authenticationFilter;

    public ConditionalCasAuthenticationFilter() {
        this.authenticationFilter = new AuthenticationFilter();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.authenticationFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (httpRequest.getUserPrincipal() == null) {
            this.authenticationFilter.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        this.authenticationFilter.destroy();
    }
}
