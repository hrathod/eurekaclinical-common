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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.multipart.Boundary;
import com.sun.jersey.multipart.FormDataMultiPart;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Base class for creating REST API clients.
 *
 * @author Andrew Post
 * @author hrathod
 */
public abstract class EurekaClinicalClient implements AutoCloseable {

    private final WebResourceWrapperFactory webResourceWrapperFactory;
    private final Class<? extends ContextResolver<? extends ObjectMapper>> contextResolverCls;
    private final ApacheHttpClient4 client;
    private final ClientConnectionManager clientConnManager;
    private final Lock readLock;
    private final Lock writeLock;

    /**
     * Constructor for passing in the object mapper instance that is used for
     * converting from/to JSON.
     *
     * @param cls the class of the object mapper.
     */
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
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    @Override
    public void close() {
        this.writeLock.lock();
        try {
            this.client.destroy();
            this.clientConnManager.shutdown();
        } finally {
            this.writeLock.unlock();
        }
    }

    protected abstract URI getResourceUrl();

    private WebResourceWrapper getResourceWrapper() {
        return this.webResourceWrapperFactory.getInstance(this.client, getResourceUrl());
    }

    /**
     * Deletes the resource specified by the path. Passes no HTTP headers.
     *
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @throws ClientException if a status code other than 204 (No Content), 202
     * (Accepted), and 200 (OK) is returned.
     */
    protected void doDelete(String path) throws ClientException {
        doDelete(path, null);
    }

    /**
     * Deletes the resource specified by the path.
     *
     * @param headers any HTTP headers. Can be <code>null</code>.
     * @param path the path to the resource. Cannot be <code>null</code>.
     *
     * @throws ClientException if a status code other than 204 (No Content), 202
     * (Accepted), and 200 (OK) is returned.
     */
    protected void doDelete(String path, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            ClientResponse response = this.getResourceWrapper()
                    .rewritten(path, HttpMethod.DELETE)
                    .delete(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT, ClientResponse.Status.ACCEPTED);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Updates the resource specified by the path, for situations where the
     * nature of the update is completely specified by the path alone.
     *
     * @param path the path to the resource. Cannot be <code>null</code>.
     *
     * @throws ClientException if a status code other than 204 (No Content) and
     * 200 (OK) is returned.
     */
    protected void doPut(String path) throws ClientException {
        this.readLock.lock();
        try {
            ClientResponse response = this.getResourceWrapper()
                    .rewritten(path, HttpMethod.PUT)
                    .put(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Updates the resource specified by the path. Sends to the server a Content
     * Type header for JSON.
     *
     * @param o the updated object, will be transmitted as JSON. It must be a
     * Java bean or an object that the object mapper that is in use knows about.
     * @param path the path to the resource. Cannot be <code>null</code>.
     *
     * @throws ClientException if a status code other than 204 (No Content) or
     * 200 (OK) is returned.
     */
    protected void doPut(String path, Object o) throws ClientException {
        doPut(path, o, null);
    }

    /**
     * Updates the resource specified by the path.
     *
     * @param o the updated object, will be transmitted as JSON. It must be a
     * Java bean or an object that the object mapper that is in use knows about.
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param headers any headers to pass along. Can be <code>null</code>. If
     * there is no content type header in the provided headers, this method will
     * add a Content Type header for JSON.
     *
     * @throws ClientException if a status code other than 204 (No Content) or
     * 200 (OK) is returned.
     */
    protected void doPut(String path, Object o, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource rewritten = this.getResourceWrapper()
                    .rewritten(path, HttpMethod.PUT);
            WebResource.Builder requestBuilder = rewritten.getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, true, false);
            ClientResponse response = requestBuilder.put(ClientResponse.class, o);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Gets the resource specified by the path. Sends to the server an Accepts
     * header for JSON.
     *
     * @param <T> the type of the resource.
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param cls the type of the resource. Cannot be <code>null</code>.
     *
     * @return the resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, Class<T> cls) throws ClientException {
        return doGet(path, cls, null);
    }

    /**
     * Gets the resource specified by the path.
     *
     * @param <T> the type of the resource.
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param cls the type of the resource. Cannot be <code>null</code>.
     * @param headers any headers. if no Accepts header is provided, an Accepts
     * header for JSON will be added.
     *
     * @return the resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, Class<T> cls, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(cls);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Gets the resource specified by the path and the provided query
     * parameters. Sends to the server an Accepts header for JSON.
     *
     * @param <T> the type of the resource.
     * @param path the path to the resource.
     * @param queryParams any query parameters. Cannot be <code>null</code>.
     * @param cls the type of the resource. Cannot be <code>null</code>.
     *
     * @return the resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, MultivaluedMap<String, String> queryParams, Class<T> cls) throws ClientException {
        return doGet(path, queryParams, cls, null);
    }

    /**
     * Gets the resource specified by the path and the provided query
     * parameters.
     *
     * @param <T> the type of the resource.
     * @param path the path to the resource.
     * @param queryParams any query parameters. Cannot be <code>null</code>.
     * @param cls the type of the resource. Cannot be <code>null</code>.
     * @param headers any headers. If no Accepts header is provided, an Accepts
     * header for JSON will be added.
     *
     * @return the resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, MultivaluedMap<String, String> queryParams, Class<T> cls, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET, queryParams).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(cls);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Makes the GET call and returns the response. The response must be closed
     * explicitly unless the <code>getEntity</code> method is called. Sends to
     * the server an Accepts header for JSON.
     *
     * @param path the path to call. Cannot be <code>null</code>.
     *
     * @return the client response.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected ClientResponse doGetResponse(String path) throws ClientException {
        return doGetResponse(path, null);
    }

    /**
     * Makes the GET call with the provided headers and returns the response.
     * The response must be closed explicitly unless the <code>getEntity</code>
     * method is called.
     *
     * @param path the path to call. Cannot be <code>null</code>.
     * @param headers any headers. If no Accepts header is provided, this method
     * will add an Accepts header for JSON.
     *
     * @return the client response.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected ClientResponse doGetResponse(String path, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Gets the requested resource. Adds an appropriate Accepts header.
     *
     * @param <T> the type of the requested resource.
     * @param path the path to the resource.
     * @param genericType the type of the requested resource.
     * @return the requested resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, GenericType<T> genericType) throws ClientException {
        return doGet(path, genericType, null);
    }

    /**
     * Gets the requested resource
     *
     * @param <T> the type of the requested resource.
     * @param path the path to the resource.
     * @param genericType the type of the requested resource.
     * @param headers any headers. If no Accepts header is provided, it adds an
     * Accepts header for JSON.
     * @return the requested resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, GenericType<T> genericType, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(genericType);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Gets the requested resource. Adds an appropriate Accepts header.
     *
     * @param <T> the type of the requested resource.
     * @param path the path to the resource.
     * @param queryParams any query parameters to send.
     * @param genericType the type of the requested resource.
     * @return the requested resource.
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, MultivaluedMap<String, String> queryParams, GenericType<T> genericType) throws ClientException {
        return doGet(path, queryParams, genericType, null);
    }

    /**
     * Gets the requested resource
     *
     * @param <T> the type of the requested resource.
     * @param path the path to the resource.
     * @param queryParams any query parameters to send.
     * @param genericType the type of the requested resource.
     * @param headers any headers. If no Accepts header is provided, it adds an
     * Accepts header for JSON.
     * @return the requested resource.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doGet(String path, MultivaluedMap<String, String> queryParams, GenericType<T> genericType, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET, queryParams).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(genericType);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Submits a form and gets back a JSON object. Adds appropriate Accepts and
     * Content Type headers.
     *
     * @param <T> the type of object that is expected in the response.
     * @param path the API to call.
     * @param formParams the form parameters to send.
     * @param cls the type of object that is expected in the response.
     * @return the object in the response.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doPost(String path, MultivaluedMap<String, String> formParams, Class<T> cls) throws ClientException {
        return doPost(path, formParams, cls, null);
    }

    /**
     * Submits a form and gets back a JSON object.
     *
     * @param <T> the type of the object that is expected in the response.
     * @param path the API to call.
     * @param formParams the form parameters to send.
     * @param cls the type of object that is expected in the response.
     * @param headers any headers. If there is no Accepts header, an Accepts
     * header is added for JSON. If there is no Content Type header, a Content
     * Type header is added for forms.
     * @return the object in the response.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doPost(String path, MultivaluedMap<String, String> formParams, Class<T> cls, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            ensurePostFormHeaders(headers, requestBuilder, true, true);
            ClientResponse response = requestBuilder.post(ClientResponse.class, formParams);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(cls);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Submits a form and gets back a JSON object. Adds appropriate Accepts and
     * Content Type headers.
     *
     * @param <T> the type of object that is expected in the response.
     * @param path the API to call.
     * @param formParams the form parameters to send.
     * @param genericType the type of object that is expected in the response.
     * @return the object in the response.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doPost(String path, MultivaluedMap<String, String> formParams, GenericType<T> genericType) throws ClientException {
        return doPost(path, formParams, genericType, null);
    }

    /**
     * Submits a form and gets back a JSON object.
     *
     * @param <T> the type of the object that is expected in the response.
     * @param path the API to call.
     * @param formParams the form parameters to send.
     * @param genericType the type of object that is expected in the response.
     * @param headers any headers. If there is no Accepts header, an Accepts
     * header is added for JSON. If there is no Content Type header, a Content
     * Type header is added for forms.
     * @return the object in the response.
     *
     * @throws ClientException if a status code other than 200 (OK) is returned.
     */
    protected <T> T doPost(String path, MultivaluedMap<String, String> formParams, GenericType<T> genericType, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            ensurePostFormHeaders(headers, requestBuilder, true, true);
            ClientResponse response = requestBuilder.post(ClientResponse.class, formParams);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(genericType);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Makes a POST call to the specified path.
     *
     * @param path the path to call.
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPost(String path) throws ClientException {
        this.readLock.lock();
        try {
            ClientResponse response = getResourceWrapper().rewritten(path, HttpMethod.POST)
                    .post(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Submits a form. Adds appropriate Accepts and Content Type headers.
     *
     * @param path the API to call.
     * @param formParams the form parameters to send.
     *
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPostForm(String path, MultivaluedMap<String, String> formParams) throws ClientException {
        doPostForm(path, formParams, null);
    }

    /**
     * Submits a form.
     *
     * @param path the API to call.
     * @param formParams the multi-part form content.
     * @param headers any headers to send. If no Content Type header is
     * specified, it adds a Content Type for form data.
     *
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPostForm(String path, MultivaluedMap<String, String> formParams, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            ensurePostFormHeaders(headers, requestBuilder, true, false);
            ClientResponse response = requestBuilder.post(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Makes a POST request with the provided object in the body as JSON. Adds a
     * Content Type header for JSON.
     *
     * @param path the API to call.
     * @param o the object to send.
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPost(String path, Object o) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(null, requestBuilder, true, false);
            ClientResponse response = requestBuilder.post(ClientResponse.class, o);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Makes a POST request with the provided object in the body as JSON.
     *
     * @param path the API to call.
     * @param o the object to send.
     * @param headers any headers. If no Content Type header is provided, this
     * method adds a Content Type header for JSON.
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPost(String path, Object o, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, true, false);
            ClientResponse response = requestBuilder.post(ClientResponse.class, o);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Submits a multi-part form. Adds appropriate Accepts and Content Type
     * headers.
     *
     * @param path the API to call.
     * @param formDataMultiPart the multi-part form content.
     *
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    public void doPostMultipart(String path, FormDataMultiPart formDataMultiPart) throws ClientException {
        this.readLock.lock();
        try {
            ClientResponse response = getResourceWrapper()
                    .rewritten(path, HttpMethod.POST)
                    .type(Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE))
                    .post(ClientResponse.class, formDataMultiPart);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Submits a multi-part form.
     *
     * @param path the API to call.
     * @param formDataMultiPart the multi-part form content.
     * @param headers any headers to add. If no Content Type header is provided,
     * this method adds a Content Type header for multi-part forms data.
     *
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    public void doPostMultipart(String path, FormDataMultiPart formDataMultiPart, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper()
                    .rewritten(path, HttpMethod.POST).getRequestBuilder();
            requestBuilder = ensurePostMultipartHeaders(headers, requestBuilder);
            ClientResponse response = requestBuilder
                    .post(ClientResponse.class, formDataMultiPart);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Submits a multi-part form in an input stream. Adds appropriate Accepts
     * and Content Type headers.
     *
     * @param path the the API to call.
     * @param inputStream the multi-part form content.
     *
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPostMultipart(String path, InputStream inputStream) throws ClientException {
        doPostMultipart(path, inputStream, null);
    }

    /**
     * Submits a multi-part form in an input stream.
     *
     * @param path the the API to call.
     * @param inputStream the multi-part form content.
     * @param headers the headers to send. If no Accepts header is provided,
     * this method as an Accepts header for text/plain. If no Content Type
     * header is provided, this method adds a Content Type header for multi-part
     * forms data.
     *
     * @throws ClientException if a status code other than 200 (OK) and 204 (No
     * Content) is returned.
     */
    protected void doPostMultipart(String path, InputStream inputStream, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            requestBuilder = ensurePostMultipartHeaders(headers, requestBuilder);
            ClientResponse response = requestBuilder.post(ClientResponse.class, inputStream);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.NO_CONTENT);
            response.close();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Creates a resource specified as a JSON object. Adds appropriate Accepts
     * and Content Type headers.
     *
     * @param path the the API to call.
     * @param o the object that will be converted to JSON for sending. Must
     * either be a Java bean or be recognized by the object mapper.
     * @return the URI representing the created resource, for use in subsequent
     * operations on the resource.
     * @throws ClientException if a status code other than 201 (Created) is
     * returned.
     */
    protected URI doPostCreate(String path, Object o) throws ClientException {
        return doPostCreate(path, o, null);
    }

    /**
     * Creates a resource specified as a JSON object.
     *
     * @param path the the API to call.
     * @param o the object that will be converted to JSON for sending. Must
     * either be a Java bean or be recognized by the object mapper.
     * @param headers If no Content Type header is provided, this method adds a
     * Content Type header for JSON.
     * @return the URI representing the created resource, for use in subsequent
     * operations on the resource.
     * @throws ClientException if a status code other than 200 (OK) and 201
     * (Created) is returned.
     */
    protected URI doPostCreate(String path, Object o, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            requestBuilder = ensurePostCreateJsonHeaders(headers, requestBuilder, true, false);
            ClientResponse response = requestBuilder.post(ClientResponse.class, o);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.CREATED);
            try {
                return response.getLocation();
            } finally {
                response.close();
            }
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Creates a resource specified as a multi-part form in an input stream.
     * Adds appropriate Accepts and Content Type headers.
     *
     * @param path the the API to call.
     * @param inputStream the multi-part form content.
     * @return the URI representing the created resource, for use in subsequent
     * operations on the resource.
     * @throws ClientException if a status code other than 200 (OK) and 201
     * (Created) is returned.
     */
    protected URI doPostCreateMultipart(String path, InputStream inputStream) throws ClientException {
        return doPostCreateMultipart(path, inputStream, null);
    }

    /**
     * Creates a resource specified as a multi-part form in an input stream.
     *
     * @param path the the API to call.
     * @param inputStream the multi-part form content.
     * @param headers any headers to send. If no Accepts header is provided,
     * this method as an Accepts header for text/plain. If no Content Type
     * header is provided, this method adds a Content Type header for multi-part
     * forms data.
     * @return the URI representing the created resource, for use in subsequent
     * operations on the resource.
     * @throws ClientException if a status code other than 200 (OK) and 201
     * (Created) is returned.
     */
    protected URI doPostCreateMultipart(String path, InputStream inputStream, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST).getRequestBuilder();
            requestBuilder = ensurePostCreateMultipartHeaders(headers, requestBuilder);
            ClientResponse response = requestBuilder.post(ClientResponse.class, inputStream);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.CREATED);
            try {
                return response.getLocation();
            } finally {
                response.close();
            }
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Creates a resource specified as a multi-part form. Adds appropriate
     * Accepts and Content Type headers.
     *
     * @param path the the API to call.
     * @param formDataMultiPart the form content.
     * @return the URI representing the created resource, for use in subsequent
     * operations on the resource.
     * @throws ClientException if a status code other than 200 (OK) and 201
     * (Created) is returned.
     */
    protected URI doPostCreateMultipart(String path, FormDataMultiPart formDataMultiPart) throws ClientException {
        this.readLock.lock();
        try {
            ClientResponse response = getResourceWrapper()
                    .rewritten(path, HttpMethod.POST)
                    .type(Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE))
                    .accept(MediaType.TEXT_PLAIN)
                    .post(ClientResponse.class, formDataMultiPart);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK, ClientResponse.Status.CREATED);
            try {
                return response.getLocation();
            } finally {
                response.close();
            }
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Passes a new resource, form or other POST body to a proxied server.
     *
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param inputStream the contents of the POST body. Cannot be
     * <code>null</code>.
     * @param parameterMap query parameters. May be <code>null</code>.
     * @param headers any request headers to add. May be <code>null</code>.
     *
     * @return ClientResponse the proxied server's response information.
     *
     * @throws ClientException if the proxied server responds with an "error"
     * status code, which is dependent on the server being called.
     * @see #getResourceUrl() for the URL of the proxied server.
     */
    protected ClientResponse doPostForProxy(String path, InputStream inputStream, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.POST, parameterMap).getRequestBuilder();
            copyHeaders(headers, requestBuilder);
            return requestBuilder.post(ClientResponse.class, inputStream);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Passes a resource update to a proxied server.
     *
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param inputStream the contents of the update. Cannot be
     * <code>null</code>.
     * @param parameterMap query parameters. May be <code>null</code>.
     * @param headers any request headers to add.
     *
     * @return ClientResponse the proxied server's response information.
     *
     * @throws ClientException if the proxied server responds with an "error"
     * status code, which is dependent on the server being called.
     * @see #getResourceUrl() for the URL of the proxied server.
     */
    protected ClientResponse doPutForProxy(String path, InputStream inputStream, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.PUT, parameterMap).getRequestBuilder();
            copyHeaders(headers, requestBuilder);
            return requestBuilder.put(ClientResponse.class, inputStream);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Gets a resource from a proxied server.
     *
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param parameterMap query parameters. May be <code>null</code>.
     * @param headers any request headers to add.
     *
     * @return ClientResponse the proxied server's response information.
     *
     * @throws ClientException if the proxied server responds with an "error"
     * status code, which is dependent on the server being called.
     * @see #getResourceUrl() for the URL of the proxied server.
     */
    protected ClientResponse doGetForProxy(String path, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET, parameterMap).getRequestBuilder();
            copyHeaders(headers, requestBuilder);
            return requestBuilder.get(ClientResponse.class);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Deletes a resource from a proxied server.
     *
     * @param path the path to the resource. Cannot be <code>null</code>.
     * @param parameterMap query parameters. May be <code>null</code>.
     * @param headers any request headers to add.
     *
     * @return ClientResponse the proxied server's response information.
     *
     * @throws ClientException if the proxied server responds with an "error"
     * status code, which is dependent on the server being called.
     * @see #getResourceUrl() for the URL of the proxied server.
     */
    protected ClientResponse doDeleteForProxy(String path, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.DELETE, parameterMap).getRequestBuilder();
            copyHeaders(headers, requestBuilder);
            return requestBuilder.delete(ClientResponse.class);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * If there is an unexpected status code, this method gets the status
     * message, closes the response, and throws an exception.
     *
     * @param response the response.
     * @param status the expected status code(s).
     * @throws ClientException if the response had a status code other than
     * those listed.
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
     * @throws ClientException if the response had a status code other than
     * those listed.
     */
    protected void errorIfStatusNotEqualTo(ClientResponse response,
            ClientResponse.Status... status) throws ClientException {
        errorIf(response, status, false);
    }

    /**
     * Extracts the id of the resource specified in the response body from a
     * POST call.
     *
     * @param uri The URI.
     * @return the id of the resource.
     */
    protected Long extractId(URI uri) {
        String uriStr = uri.toString();
        return Long.valueOf(uriStr.substring(uriStr.lastIndexOf("/") + 1));
    }

    /**
     * Gets the specified resource as a string.
     *
     * @param path the path to the resource.
     * @param headers any headers. If no Accepts header is provided, an Accepts
     * header is added specifying JSON.
     * @return a string containing the requested resource.
     *
     * @throws ClientException if the response had a status code other than 200
     * (OK).
     */
    String doGet(String path, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(String.class);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Gets the specified resource as a string.
     *
     * @param path the path to the resource.
     * @param queryParams any query parameters. Cannot be <code>null</code>.
     * @param headers any headers. If no Accepts header is provided, an Accepts
     * header is added specifying JSON.
     * @return a string containing the requested resource.
     *
     * @throws ClientException if the response had a status code other than 200
     * (OK).
     */
    String doGet(String path, MultivaluedMap<String, String> queryParams, MultivaluedMap<String, String> headers) throws ClientException {
        this.readLock.lock();
        try {
            WebResource.Builder requestBuilder = getResourceWrapper().rewritten(path, HttpMethod.GET, queryParams).getRequestBuilder();
            requestBuilder = ensureJsonHeaders(headers, requestBuilder, false, true);
            ClientResponse response = requestBuilder.get(ClientResponse.class);
            errorIfStatusNotEqualTo(response, ClientResponse.Status.OK);
            return response.getEntity(String.class);
        } finally {
            this.readLock.unlock();
        }
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

    /**
     * Tests array membership.
     *
     * @param arr the array.
     * @param member an object.
     * @return <code>true</code> if the provided object is a member of the
     * provided array, or <code>false</code> if not.
     */
    private static boolean contains(Object[] arr, Object member) {
        for (Object mem : arr) {
            if (Objects.equals(mem, member)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the specified headers to the request builder. Provides default
     * headers for JSON objects requests and submissions.
     *
     * @param headers the headers to add.
     * @param requestBuilder the request builder. Cannot be <code>null</code>.
     * @param contentType <code>true</code> to add a default Content Type header
     * if no Content Type header is provided.
     * @param accept <code>true</code> to add a default Accepts header if no
     * Accepts header is provided.
     * @return the resulting request builder. Guaranteed not <code>null</code>.
     */
    private static WebResource.Builder ensureJsonHeaders(MultivaluedMap<String, String> headers, WebResource.Builder requestBuilder, boolean contentType, boolean accept) {
        boolean hasContentType = false;
        boolean hasAccept = false;
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (contentType && HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(key)) {
                        hasContentType = true;
                    } else if (accept && HttpHeaders.ACCEPT.equalsIgnoreCase(key)) {
                        hasAccept = true;
                    }
                    requestBuilder = requestBuilder.header(key, val);
                }
            }
        }
        if (!hasContentType) {
            requestBuilder = requestBuilder.type(MediaType.APPLICATION_JSON);
        }
        if (!hasAccept) {
            requestBuilder = requestBuilder.accept(MediaType.APPLICATION_JSON);
        }
        return requestBuilder;
    }

    /**
     * Adds the specified headers to the request builder. Provides default
     * headers for form submissions, optionally with JSON responses.
     *
     * @param headers the headers to add.
     * @param requestBuilder the request builder. Cannot be <code>null</code>.
     * @param contentType <code>true</code> to add a default Content Type header
     * if no Content Type header is provided.
     * @param accept <code>true</code> to add a default Accepts header if no
     * Accepts header is provided.
     * @return the resulting request builder. Guaranteed not <code>null</code>.
     */
    private static WebResource.Builder ensurePostFormHeaders(MultivaluedMap<String, String> headers, WebResource.Builder requestBuilder, boolean contentType, boolean accept) {
        boolean hasContentType = false;
        boolean hasAccept = false;
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (contentType && HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(key)) {
                        hasContentType = true;
                    } else if (accept && HttpHeaders.ACCEPT.equalsIgnoreCase(key)) {
                        hasAccept = true;
                    }
                    requestBuilder = requestBuilder.header(key, val);
                }
            }
        }
        if (!hasContentType) {
            requestBuilder = requestBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        }
        if (!hasAccept) {
            requestBuilder = requestBuilder.accept(MediaType.APPLICATION_JSON);
        }
        return requestBuilder;
    }

    private static WebResource.Builder copyHeaders(MultivaluedMap<String, String> headers, WebResource.Builder requestBuilder) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    requestBuilder = requestBuilder.header(key, val);
                }
            }
        }
        return requestBuilder;
    }

    /**
     * Adds the specified headers to the request builder. Provides default
     * headers for multi-part submissions.
     *
     * @param headers the headers to add.
     * @param requestBuilder the request builder. Cannot be <code>null</code>.
     * @return the resulting request builder. Guaranteed not <code>null</code>.
     */
    private static WebResource.Builder ensurePostMultipartHeaders(MultivaluedMap<String, String> headers, WebResource.Builder requestBuilder) {
        boolean hasContentType = false;
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(key)) {
                        hasContentType = true;
                    }
                    requestBuilder = requestBuilder.header(key, val);
                }
            }
        }
        if (!hasContentType) {
            requestBuilder = requestBuilder.type(Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE));
        }
        return requestBuilder;
    }

    /**
     * Adds the specified headers to the request builder. Provides default
     * headers for JSON request and/or response bodies.
     *
     * @param headers the headers to add.
     * @param requestBuilder the request builder. Cannot be <code>null</code>.
     * @param contentType <code>true</code> to add a default Content Type header
     * if no Content Type header is provided.
     * @param accept <code>true</code> to add a default Accepts header if no
     * Accepts header is provided.
     * @return the resulting request builder. Guaranteed not <code>null</code>.
     */
    private static WebResource.Builder ensurePostCreateJsonHeaders(MultivaluedMap<String, String> headers, WebResource.Builder requestBuilder, boolean contentType, boolean accept) {
        boolean hasContentType = false;
        boolean hasAccept = false;
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (contentType && HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(key)) {
                        hasContentType = true;
                    } else if (accept && HttpHeaders.ACCEPT.equalsIgnoreCase(key)) {
                        hasAccept = true;
                    }
                    requestBuilder = requestBuilder.header(key, val);
                }
            }
        }
        if (!hasContentType) {
            requestBuilder = requestBuilder.type(MediaType.APPLICATION_JSON);
        }
        if (!hasAccept) {
            requestBuilder = requestBuilder.accept(MediaType.TEXT_PLAIN);
        }
        return requestBuilder;
    }

    /**
     * Adds the specified headers to the request builder. Provides default
     * headers for multi-part submissions.
     *
     * @param headers the headers to add.
     * @param requestBuilder the request builder. Cannot be <code>null</code>.
     * @return the resulting request builder. Guaranteed not <code>null</code>.
     */
    private static WebResource.Builder ensurePostCreateMultipartHeaders(MultivaluedMap<String, String> headers, WebResource.Builder requestBuilder) {
        boolean hasContentType = false;
        boolean hasAccept = false;
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(key)) {
                        hasContentType = true;
                    } else if (HttpHeaders.ACCEPT.equalsIgnoreCase(key)) {
                        hasAccept = true;
                    }
                    requestBuilder = requestBuilder.header(key, val);
                }
            }
        }
        if (!hasContentType) {
            requestBuilder = requestBuilder.type(Boundary.addBoundary(MediaType.MULTIPART_FORM_DATA_TYPE));
        }
        if (!hasAccept) {
            requestBuilder = requestBuilder.accept(MediaType.TEXT_PLAIN);
        }
        return requestBuilder;
    }
}
