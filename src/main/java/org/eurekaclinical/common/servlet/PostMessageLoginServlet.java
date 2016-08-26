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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.inject.Singleton;


/**
 * Designed for CAS login in an iframe. After login, the iframe uses 
 * <code>postMessage</code> to notify the parent that it is done loading.
 * 
 * @author Andrew Post
 */
@Singleton
public class PostMessageLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(resp.getOutputStream()))) {
            out.println("<html><head></head><body><script type=\"text/javascript\">");
            out.println("window.parent.postMessage('OK', '*');");
            out.println("</script></body></html>");
        }
    }
}
