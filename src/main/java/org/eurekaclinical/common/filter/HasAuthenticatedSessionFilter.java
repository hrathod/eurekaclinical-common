/*-
 * #%L
 * Eureka! Clinical User Webapp
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
package org.eurekaclinical.common.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 *
 * @author miaoai
 */
@Singleton
public class HasAuthenticatedSessionFilter implements Filter {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(HasAuthenticatedSessionFilter.class);

    public HasAuthenticatedSessionFilter() {
    }

    @Override
    public void init(FilterConfig inFilterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest inRequest, ServletResponse inResponse, FilterChain inFilterChain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) inRequest;
        HttpServletResponse servletResponse = (HttpServletResponse) inResponse;

        String remoteUser = servletRequest.getRemoteUser();

        if (remoteUser != null) {
            HttpSession session = servletRequest.getSession(false);
            if (session != null) {
                inFilterChain.doFilter(inRequest, inResponse);
            } else {
                goHome(servletRequest, servletResponse);
            }
        } else {
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void goHome(HttpServletRequest inRequest, HttpServletResponse inResponse) throws IOException {
        inResponse.sendRedirect(inRequest.getContextPath() + "/logout?goHome=true");
    }

    @Override
    public void destroy() {
    }

}
