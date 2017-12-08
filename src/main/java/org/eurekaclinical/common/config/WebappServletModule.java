package org.eurekaclinical.common.config;

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

import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;

/**
 * Servlet module that configures a webapp as a combined webapp and API 
 * gateway. It populates a user's role information for use within traditional
 * servlets and JSP pages.
 * 
 * @author Andrew Post
 */
public class WebappServletModule extends AbstractAuthorizingServletModule {
    public WebappServletModule(CasEurekaClinicalProperties inProperties) {
        super(inProperties);
    }

    /**
     * Makes the following calls available: 
     * <ul>
     * <li><code>/proxy-resource/*</code>
     * <li><code>/protected/login</code>
     * <li><code>/protected/get-session</code>
     * <li><code>/destroy-session</code>
     * <li><code>/protected/get-session-properties</code>
     * </ul>
     */
    @Override
    protected void setupServlets() {
        serveProxyResource();
        serveLogin();
        serveGetSession();
        serveDestroySession();
        serveGetSessionProperties();
    }
}
