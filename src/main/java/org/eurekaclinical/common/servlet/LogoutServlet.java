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
import javax.inject.Inject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.inject.Singleton;

import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;

@Singleton
public class LogoutServlet extends HttpServlet {

    private final CasEurekaClinicalProperties webappProperties;

    @Inject
    public LogoutServlet(CasEurekaClinicalProperties inProperties) {
        this.webappProperties = inProperties;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        /*
         * We need to redirect here rather than forward so that 
         * logout.jsp gets a request object without a user. Otherwise,
         * the button bar will think we're still logged in.
         */
        StringBuilder buf = new StringBuilder();
        String casLogoutUrl = webappProperties.getCasLogoutUrl();
        buf.append(casLogoutUrl);
        String awaitingActivation = req.getParameter("awaitingActivation");
        boolean aaEmpty = awaitingActivation == null || awaitingActivation.length() == 0;
        if (!aaEmpty && toBooleanObject(awaitingActivation) == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String notRegistered = req.getParameter("notRegistered");
        boolean nrEmpty = notRegistered == null || notRegistered.length() == 0;
        if (!nrEmpty && toBooleanObject(notRegistered) == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!aaEmpty || !nrEmpty) {
            buf.append('?');
        }
        if (!aaEmpty) {
            buf.append("awaitingActivation=").append(awaitingActivation);
        }
        if (!aaEmpty && !nrEmpty) {
            buf.append('&');
        }
        if (!nrEmpty) {
            buf.append("notRegistered=").append(notRegistered);
        }
        log("URL IS " + buf.toString());
        resp.sendRedirect(buf.toString());
    }
    
    /*
     * Local implementation of Apache Commons BooleanUtils.toBooleanObject.
     */

    private static final String[] TRUTHY_STRINGS = {
        "true",
        "on",
        "y",
        "t",
        "yes"
    };

    private static final String[] FALSY_STRINGS = {
        "false",
        "off",
        "n",
        "f",
        "no"
    };

    private static Boolean toBooleanObject(String str) {
        if (str != null) {
            for (String truthyString : TRUTHY_STRINGS) {
                if (str.equalsIgnoreCase(truthyString)) {
                    return Boolean.TRUE;
                }
            }
            for (String falsyString : FALSY_STRINGS) {
                if (str.equalsIgnoreCase(falsyString)) {
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }
}
