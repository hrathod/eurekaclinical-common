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
import java.io.InputStream;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Andrew Post
 */
public class ProxyingClient {

    private final Router config;

    @Inject
    public ProxyingClient(Router inConfig) {
        this.config = inConfig;
    }

    public ProxyResponse proxyPost(String path, InputStream inputStream,MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        return new ProxyResponse(client.doPostForProxy(replacementPath, inputStream, parameterMap, headers), replacementPathAndClient);
    }

    public ProxyResponse proxyDelete(String path, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        return new ProxyResponse(client.doDeleteForProxy(replacementPath, parameterMap, headers), replacementPathAndClient);
    }

    public ProxyResponse proxyPut(String path, InputStream inputStream, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        return new ProxyResponse(client.doPutForProxy(replacementPath, inputStream, parameterMap, headers), replacementPathAndClient);
    }

    public ProxyResponse proxyGet(String path, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers)
            throws ClientException {
        ReplacementPathAndClient replacementPathAndClient = this.config.getReplacementPathAndClient(path);
        EurekaClinicalClient client = replacementPathAndClient.getClient();
        String replacementPath = replacementPathAndClient.getPath();
        return new ProxyResponse(client.doGetForProxy(replacementPath, parameterMap, headers), replacementPathAndClient);
    }

}
