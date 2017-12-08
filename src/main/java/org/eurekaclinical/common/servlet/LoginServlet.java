package org.eurekaclinical.common.servlet;

/*-
 * #%L
 * Eureka! Clinical Common
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
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.inject.Inject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;

/**
 * Redirects to the URL specified in the <code>webclient</code> query parameter.
 * If the application properties provide a list of allowed webclient URLs, the
 * redirect will silently fail unless the given URL matches one of the allowed
 * URLs.
 * 
 * This servlet is intended to be combined with filters to redirect to the 
 * single sign-on login page to login to Eureka! Clinical webapps and 
 * webclients.
 *
 * @author Andrew Post
 */
@Singleton
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final URI[] EMPTY_URI_ARRAY = new URI[0];

    private URI[] allowedWebClientURIs;
    private final CasEurekaClinicalProperties properties;

    @Inject
    public LoginServlet(CasEurekaClinicalProperties inProperties) {
        this.properties = inProperties;
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        List<String> webClientUrls = this.properties.getAllowedWebClientUrls();
        if (webClientUrls != null) {
            this.allowedWebClientURIs = new URI[webClientUrls.size()];
            for (int i = 0; i < this.allowedWebClientURIs.length; i++) {
                this.allowedWebClientURIs[i] = URI.create(webClientUrls.get(i));
            }
        } else {
            this.allowedWebClientURIs = EMPTY_URI_ARRAY;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String webClient = req.getParameter("webclient");
        if ((webClient != null && isAllowed(URI.create(webClient)))) {
            resp.sendRedirect(webClient);
        }
    }

    private boolean isAllowed(URI actualWebClient) {
        if (this.allowedWebClientURIs.length == 0) {
            return true;
        } else {
            for (URI allowedWebClientURI : this.allowedWebClientURIs) {
                if (allowedWebClientURI.equals(actualWebClient)) {
                    return true;
                }
            }
            return false;
        }
    }
}
