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

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.eurekaclinical.common.util.JwtUtil;
import org.eurekaclinical.standardapis.filter.RolesRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

/**
 * This class implements a filter to authenticate a request based on the
 * authorization HTTP header.  The header should look like:
 * Authorization: BEARER SOME-TOKEN-HERE
 *
 * For this filter, the token is expected to be a JWT token containing
 * both a "username" claim, as well as a "roles" claim.
 */
@Singleton
public class JwtFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtFilter.class);
    private static final String AUTH_TYPE = "BEARER";
    public static final String SECRET_PARAM_NAME = "JWT_SECRET";
    private String secret;

    @Override
    public void init(FilterConfig filterConfig) {
        this.secret = filterConfig.getInitParameter(SECRET_PARAM_NAME);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization != null && authorization.length() > AUTH_TYPE.length()) {
            String[] authParts = authorization.split(" ");
            if ("BEARER".equalsIgnoreCase(authParts[0].trim())) {
                try {
                    DecodedJWT decodedJWT = JwtUtil.verifyAndDecode(authParts[1].trim(), this.secret);
                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    String username = decodedJWT.getClaim("username").asString();
                    Principal principal = () -> username;
                    HttpSession session = httpRequest.getSession();
                    session.setAttribute("roles", roles);
                    RolesRequestWrapper wrapper = new RolesRequestWrapper(httpRequest, principal, roles, username);
                    chain.doFilter(wrapper, response);
                } catch (JWTVerificationException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}

