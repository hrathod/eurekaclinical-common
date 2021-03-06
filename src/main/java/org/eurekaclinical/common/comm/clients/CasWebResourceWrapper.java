package org.eurekaclinical.common.comm.clients;

/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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
import javax.ws.rs.core.MultivaluedMap;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author Andrew Post
 */
class CasWebResourceWrapper extends AbstractWebResourceWrapper {

    CasWebResourceWrapper(WebResource inWebResource) {
        super(inWebResource);
    }

    @Override
    public WebResource rewritten(String path, String method, MultivaluedMap<String, String> queryParams) throws ClientException {
        WebResource webResource = getWebResource();
        if (queryParams != null) {
            webResource = webResource.queryParams(queryParams);
        }
        webResource = webResource.path(path);
        webResource = withProxyTicket(webResource);
        return webResource;
    }

    private static WebResource withProxyTicket(WebResource webResource) throws ClientException {
        Assertion assertion = AssertionHolder.getAssertion();
        if (assertion != null) {
            AttributePrincipal principal = assertion.getPrincipal();
            String proxyTicket = principal.getProxyTicketFor(
                    webResource.getURI().toString());
            if (proxyTicket == null) {
                throw new ClientException(
                        ClientResponse.Status.BAD_REQUEST,
                        "Could not get proxy ticket for service call " + webResource.getURI().toString());
            }
            return webResource.queryParam("ticket", proxyTicket);
        } else {
            return webResource;
        }
    }
}
