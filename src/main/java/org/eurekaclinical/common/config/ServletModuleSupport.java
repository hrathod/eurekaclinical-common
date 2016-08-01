package org.eurekaclinical.common.config;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;

/**
 *
 * @author Andrew Post
 */
final class ServletModuleSupport {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServletModuleSupport.class);
    private static final String CAS_PROXY_CALLBACK_PATH = "/proxyCallback";
    private final CasEurekaClinicalProperties properties;
    private final String contextPath;

    ServletModuleSupport(String contextPath,
            CasEurekaClinicalProperties inProperties) {
        this.properties = inProperties;
        this.contextPath = contextPath;
    }

    Map<String, String> getCasAuthenticationFilterInitParams() {
        Map<String, String> params = new HashMap<>();
        params.put("casServerLoginUrl", this.getCasLoginUrl());
        params.put("serverName", this.properties.getProxyCallbackServer());
        params.put("renew", "false");
        params.put("gateway", "false");
        if (LOGGER.isDebugEnabled()) {
            this.printParams(params);
        }
        return params;
    }

    Map<String, String> getServletRequestWrapperFilterInitParams() {
        Map<String, String> params = new HashMap<>();
        params.put("roleAttribute", "authorities");
        if (LOGGER.isDebugEnabled()) {
            this.printParams(params);
        }
        return params;
    }
    
    private void printParams(Map<String, String> inParams) {
        for (Map.Entry<String, String> entry : inParams.entrySet()) {
            LOGGER.debug(entry.getKey() + " -> " + entry.getValue());
        }
    }
    
    private String getCasLoginUrl() {
        return this.properties.getCasLoginUrl();
    }

    protected String getCasProxyCallbackUrl() {
        return this.properties.getProxyCallbackServer() + this.contextPath
                + CAS_PROXY_CALLBACK_PATH;
    }

    protected String getCasProxyCallbackPath() {
        return CAS_PROXY_CALLBACK_PATH;
    }
    
}
