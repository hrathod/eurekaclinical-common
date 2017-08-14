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


import com.google.inject.Singleton;

/**
 * Filter that sets a request attribute, <code>userIsActivated</code>, if there 
 * is an active session with a non-null remote user.
 * 
 * @author miaoai
 */
@Singleton
public class HasAuthenticatedSessionFilter implements Filter {

    public HasAuthenticatedSessionFilter() {
    }

    /**
     * Initializes the filter. Does nothing.
     * 
     * @param inFilterConfig the filter's configuration.
     */
    @Override
    public void init(FilterConfig inFilterConfig) {
    }

    /**
     * Sets the request attribute and passes the request and response onto the
     * next filter. If there is no session or the remote user is not set,
     * the response status is set to 400 (Bad Request).
     * 
     * @param inRequest the HTTP request.
     * @param inResponse the HTTP response.
     * @param inFilterChain the filter chain.
     * @throws IOException if there is an error in subsequent filters or in
     * the subsequent response.
     * @throws ServletException if there is an error in subsequent filters.
     */
    @Override
    public void doFilter(ServletRequest inRequest, ServletResponse inResponse, FilterChain inFilterChain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) inRequest;
        HttpServletResponse servletResponse = (HttpServletResponse) inResponse;

        String remoteUser = servletRequest.getRemoteUser();
        HttpSession session = servletRequest.getSession(false);
        if (remoteUser != null && session != null) {
            inRequest.setAttribute("userIsActivated", Boolean.TRUE);
            inFilterChain.doFilter(inRequest, inResponse);
        } else {
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Destroys the filter. Does nothing.
     */
    @Override
    public void destroy() {
    }

}
