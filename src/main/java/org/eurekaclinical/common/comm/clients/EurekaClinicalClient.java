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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.Boundary;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Andrew Post
 * @author hrathod
 */
public abstract class EurekaClinicalClient implements AutoCloseable {

    private final WebResourceWrapperFactory webResourceWrapperFactory;
    private final Class<? extends ContextResolver<? extends ObjectMapper>> contextResolverCls;
    private ApacheHttpClient4 client;
    private final ClientConnectionManager clientConnManager;

    protected EurekaClinicalClient(Class<? extends ContextResolver<? extends ObjectMapper>> cls) {
        this.webResourceWrapperFactory = new CasWebResourceWrapperFactory();
        this.contextResolverCls = cls;
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
        Map<String, Object> properties = clientConfig.getProperties();
        properties.put(ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES, false);
        this.clientConnManager = new ThreadSafeClientConnManager();
        properties.put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, this.clientConnManager);
        clientConfig.getFeatures().put(
                JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        if (this.contextResolverCls != null) {
            clientConfig.getClasses().add(this.contextResolverCls);
        }
        this.client = ApacheHttpClient4.create(clientConfig);
        this.client.addFilter(new GZIPContentEncodingFilter(false));
    }
    
    @Override
    public void close() {
        this.client.destroy();
        this.clientConnManager.shutdown();
    }

    protected abstract URI getResourceUrl();

    private WebResourceWrapper getResourceWrapper() {
        return this.webResourceWrapperFactory.getInstance(this.client, getResourceUrl());
    }
    
    protected void doDelete(String path) throws ClientException {
        ClientResponse response = this.getResourceWrapper()
                .rewritten(path, HttpMethod.DELETE)
                .accept(MediaType.APPLICATION_JSON)
                .delete(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.NO_CONTENT, ClientResponse.Status.ACCEPTED);
        response.close();
    }

    protected void doPut(String path) throws ClientException {
        ClientResponse response = this.getResourceWrapper()
                .rewritten(path, HttpMethod.PUT)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.CREATED, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
        response.close();
    }

    protected void doPut(String path, Object o) throws ClientException {
        ClientResponse response = this.getResourceWrapper()
                .rewritten(path, HttpMethod.PUT)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class, o);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.CREATED, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
        response.close();
    }

    protected String doGet(String path) throws ClientException {
        ClientResponse response = doGetResponse(path);

        return response.getEntity(String.class);
    }

    protected String doGet(String path, MultivaluedMap<String, String> queryParams) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.GET, queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);

        return response.getEntity(String.class);
    }

    protected <T> T doGet(String path, Class<T> cls) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.GET)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(cls);
    }

    protected <T> T doGet(String path, Class<T> cls, MultivaluedMap<String, String> queryParams) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.GET)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(cls);
    }

    /**
     * Makes the GET call and returns the response. The response must be closed
     * explicitly unless the <code>getEntity</code> method is called.
     *
     * @param path the path to call.
     * @return the client response.
     * @throws ClientException if an error occurs making the call.
     */
    protected ClientResponse doGetResponse(String path) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.GET)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response;
    }

    protected <T> T doGet(String path, GenericType<T> genericType) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.GET)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(genericType);
    }

    protected <T> T doGet(String path, GenericType<T> genericType, MultivaluedMap<String, String> queryParams) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.GET, queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(genericType);
    }

    protected <T> T doPost(String path, Class<T> cls, MultivaluedMap<String, String> formParams) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, formParams);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(cls);
    }

    protected <T> T doPost(String path, GenericType<T> genericType, MultivaluedMap<String, String> formParams) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, formParams);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(genericType);
    }

    protected void doPost(String path) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.NO_CONTENT);
        response.close();
    }

    protected void doPost(String path, Object o) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, o);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.NO_CONTENT);
        response.close();
    }

    protected <T> T doPost(String path, Object o, Class<T> cls) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, o);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
        return response.getEntity(cls);
    }

    protected URI doPostCreate(String path, Object o) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, o);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.CREATED);
        try {
            return response.getLocation();
        } finally {
            response.close();
        }
    }

    protected URI doPostCreate(String path, InputStream inputStream) throws ClientException {
        ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, inputStream);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.CREATED);
        try {
            return response.getLocation();
        } finally {
            response.close();
        }
    }

    protected URI doPostMultipart(String path, String filename, InputStream inputStream) throws ClientException {
        FormDataMultiPart part = new FormDataMultiPart();
        part.bodyPart(
                new FormDataBodyPart(
                        FormDataContentDisposition
                                .name("file")
                                .fileName(filename)
                                .build(),
                        inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        ClientResponse response = getResourceWrapper()
                .rewritten(path, HttpMethod.POST)
                .type(Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE))
                .post(ClientResponse.class, part);
        errorIfStatusNotEqualTo(response, ClientResponse.Status.CREATED);
        try {
            return response.getLocation();
        } finally {
            response.close();
        }
    }

    /**
     * If there is an unexpected status code, it gets the status message, closes
     * the response, and throws an exception.
     *
     * @param response the response.
     * @param status the expected status code(s).
     * @throws ClientException
     */
    protected void errorIfStatusEqualTo(ClientResponse response,
            ClientResponse.Status... status) throws ClientException {
        errorIf(response, status, true);
    }

    /**
     * If there is an unexpected status code, it gets the status message, closes
     * the response, and throws an exception.
     *
     * @param response the response.
     * @param status the expected status code(s).
     * @throws ClientException
     */
    protected void errorIfStatusNotEqualTo(ClientResponse response,
            ClientResponse.Status... status) throws ClientException {
        errorIf(response, status, false);
    }

    protected Long extractId(URI uri) {
        String uriStr = uri.toString();
        return Long.valueOf(uriStr.substring(uriStr.lastIndexOf("/") + 1));
    }

    /**
     * If there is an unexpected status code, it gets the status message, closes
     * the response, and throws an exception.
     *
     * @throws ClientException
     */
    private void errorIf(ClientResponse response,
            ClientResponse.Status[] status, boolean bool)
            throws ClientException {
        ClientResponse.Status clientResponseStatus
                = response.getClientResponseStatus();
        if (bool) {
            if (contains(status, clientResponseStatus)) {
                String message = response.getEntity(String.class);
                throw new ClientException(clientResponseStatus, message);
            }
        } else if (!contains(status, clientResponseStatus)) {
            String message = response.getEntity(String.class);
            throw new ClientException(clientResponseStatus, message);
        }
    }

    private static boolean contains(Object[] arr, Object member) {
        for (Object mem : arr) {
            if (Objects.equals(mem, member)) {
                return true;
            }
        }
        return false;
    }
}
