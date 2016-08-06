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

import com.google.inject.Inject;
import java.io.IOException;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eurekaclinical.common.config.DestroySessionServletJSPProvider;

/**
 * Destroys any session associated with this webapp. This has the effect of
 * logging the user out of the webapp without logging him/her out of CAS. As a
 * result, going to a page on the webapp will possibly log the user back in 
 * without needing to present credentials.
 * 
 * This is useful primarily for destroying the cookie associated with the 
 * webapp. You can bind a Guice provider to 
 * {@link DestroySessionServletJSPProvider}
 * that provides the path to a JSP page to show after the session has been
 * invalidated.
 * 
 * @author Andrew Post
 * @see LogoutServlet if you really want to log the user out of CAS.
 */
@Singleton
public class DestroySessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject(optional=true)
    DestroySessionServletJSPProvider jspPath;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        if (this.jspPath != null) {
            req.getServletContext().getRequestDispatcher(this.jspPath.get()).forward(req, resp);
        }
    }
    
}
