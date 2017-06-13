package org.eurekaclinical.common.comm.clients;

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

import com.google.inject.servlet.SessionScoped;
import java.io.InputStream;
import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Andrew Post
 */
@SessionScoped
public class ProxyingClient {

    private final Router config;

    @Inject
    public ProxyingClient(Router inConfig) {
        this.config = inConfig;
    }
    
    public URI proxyPostMultipart(final String path, final InputStream inInputStream, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        return client.doPostCreateMultipart(replacementPath, inInputStream, headers);
    }

    public URI proxyPost(final String path, final InputStream inputStream, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        return client.doPostCreate(replacementPath, inputStream, headers);
    }

    public void proxyDelete(final String path, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        client.doDelete(replacementPath, headers);
    }

    public void proxyPut(final String path, final InputStream inputStream, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        client.doPut(replacementPath, inputStream, headers);
    }

    public String proxyGet(final String path, MultivaluedMap<String, String> queryParams, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        if (queryParams == null) {
            return client.doGet(replacementPath, headers);
        } else {
            return client.doGet(replacementPath, queryParams, headers);
        }
    }
    
}
