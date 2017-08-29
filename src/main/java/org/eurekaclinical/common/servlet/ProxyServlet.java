package org.eurekaclinical.common.servlet;

/*
 * #%L
 * Eureka WebApp
 * %%
 * Copyright (C) 2012 - 2015 Emory University
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
import com.google.inject.Injector;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.common.comm.clients.ProxyResponse;
import org.eurekaclinical.common.comm.clients.ProxyingClient;
import org.eurekaclinical.common.comm.clients.ReplacementPathAndClient;

/**
 * @author Sanjay Agravat, Miao Ai
 */
@Singleton
public class ProxyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Set<String> requestHeadersToExclude;

    static {
        requestHeadersToExclude = new HashSet<>();
        for (String header : new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade", HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.COOKIE
        }) {
            requestHeadersToExclude.add(header.toUpperCase());
        }
    }

    private static final Set<String> responseHeadersToExclude;

    static {
        responseHeadersToExclude = new HashSet<>();
        for (String header : new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade", HttpHeaders.SET_COOKIE
        }) {
            responseHeadersToExclude.add(header.toUpperCase());
        }
    }

    private final Injector injector;

    @Inject
    public ProxyServlet(Injector inInjector) {
        this.injector = inInjector;
    }

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doPut(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        ProxyingClient client = this.injector.getInstance(ProxyingClient.class);
        String path = servletRequest.getPathInfo();
        MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(servletRequest);
        MultivaluedMap<String, String> parameterMap = toMultivaluedMap(servletRequest.getParameterMap());
        try {
            ProxyResponse proxyResponse = client.proxyPut(path, servletRequest.getInputStream(), parameterMap, requestHeaders);
            ClientResponse clientResponse = proxyResponse.getClientResponse();
            servletResponse.setStatus(clientResponse.getStatus());
            copyResponseHeaders(clientResponse.getHeaders(), proxyResponse.getReplacementPathAndClient(), baseUrl(servletRequest.getContextPath(), servletRequest).toString(), servletResponse);
            copyStream(clientResponse.getEntityInputStream(), servletResponse.getOutputStream());
        } catch (ClientException e) {
            servletResponse.setStatus(e.getResponseStatus().getStatusCode());
            servletResponse.getOutputStream().print(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws IOException {
        ProxyingClient client = this.injector.getInstance(ProxyingClient.class);
        String path = servletRequest.getPathInfo();
        MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(servletRequest);
        MultivaluedMap<String, String> parameterMap = toMultivaluedMap(servletRequest.getParameterMap());
        try {
            ProxyResponse proxyResponse = client.proxyPost(path, servletRequest.getInputStream(), parameterMap, requestHeaders);
            ClientResponse clientResponse = proxyResponse.getClientResponse();
            servletResponse.setStatus(clientResponse.getStatus());
            copyResponseHeaders(clientResponse.getHeaders(), proxyResponse.getReplacementPathAndClient(), baseUrl(servletRequest.getContextPath(), servletRequest).toString(), servletResponse);
            copyStream(clientResponse.getEntityInputStream(), servletResponse.getOutputStream());
        } catch (ClientException e) {
            servletResponse.setStatus(e.getResponseStatus().getStatusCode());
            servletResponse.getOutputStream().print(e.getMessage());
        }

    }

    @Override
    protected void doDelete(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws IOException {
        ProxyingClient client = this.injector.getInstance(ProxyingClient.class);
        String path = servletRequest.getPathInfo();
        MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(servletRequest);
        MultivaluedMap<String, String> parameterMap = toMultivaluedMap(servletRequest.getParameterMap());
        try {
            ProxyResponse proxyResponse = client.proxyDelete(path, parameterMap, requestHeaders);
            ClientResponse clientResponse = proxyResponse.getClientResponse();
            servletResponse.setStatus(clientResponse.getStatus());
            copyResponseHeaders(clientResponse.getHeaders(), proxyResponse.getReplacementPathAndClient(), baseUrl(servletRequest.getContextPath(), servletRequest).toString(), servletResponse);
            copyStream(clientResponse.getEntityInputStream(), servletResponse.getOutputStream());
        } catch (ClientException e) {
            servletResponse.setStatus(e.getResponseStatus().getStatusCode());
            servletResponse.getOutputStream().print(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws IOException {
        ProxyingClient client = this.injector.getInstance(ProxyingClient.class);
        String path = servletRequest.getPathInfo();
        MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(servletRequest);
        MultivaluedMap<String, String> parameterMap = toMultivaluedMap(servletRequest.getParameterMap());
        try {
            ProxyResponse proxyResponse = client.proxyGet(path, parameterMap, requestHeaders);
            ClientResponse clientResponse = proxyResponse.getClientResponse();
            servletResponse.setStatus(clientResponse.getStatus());
            copyResponseHeaders(clientResponse.getHeaders(), proxyResponse.getReplacementPathAndClient(), baseUrl(servletRequest.getContextPath(), servletRequest).toString(), servletResponse);
            copyStream(clientResponse.getEntityInputStream(), servletResponse.getOutputStream());
        } catch (ClientException e) {
            servletResponse.setStatus(e.getResponseStatus().getStatusCode());
            servletResponse.getOutputStream().print(e.getMessage());
        }
    }

    private static MultivaluedMap<String, String> toMultivaluedMap(Map<String, String[]> inQueryParameters) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        for (Map.Entry<String, String[]> parameter : inQueryParameters.entrySet()) {
            String[] values = parameter.getValue();
            for (String value : values) {
                queryParams.add(parameter.getKey(), value);
            }
        }
        return queryParams;
    }

    private static MultivaluedMap<String, String> extractRequestHeaders(HttpServletRequest servletRequest) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        for (Enumeration<String> enm = servletRequest.getHeaderNames(); enm.hasMoreElements();) {
            String headerName = enm.nextElement();
            for (Enumeration<String> enm2 = servletRequest.getHeaders(headerName); enm2.hasMoreElements();) {
                String nextValue = enm2.nextElement();
                if (!requestHeadersToExclude.contains(headerName.toUpperCase())) {
                    headers.add(headerName, nextValue);
                }
            }
        }
        addXForwardedForHeader(servletRequest, headers);
        return headers;
    }

    private static void addXForwardedForHeader(HttpServletRequest servletRequest,
            MultivaluedMap<String, String> headers) {
        String forHeaderName = "X-Forwarded-For";
        String forHeader = servletRequest.getRemoteAddr();
        String existingForHeader = servletRequest.getHeader(forHeaderName);
        if (existingForHeader != null) {
            forHeader = existingForHeader + ", " + forHeader;
        }
        headers.add(forHeaderName, forHeader);

        String protoHeaderName = "X-Forwarded-Proto";
        String protoHeader = servletRequest.getScheme();
        headers.add(protoHeaderName, protoHeader);

    }

    private static void copyResponseHeaders(
            MultivaluedMap<String, String> headers, 
            ReplacementPathAndClient replacementPathAndClient, 
            String proxyResourceUrl,
            HttpServletResponse response) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (!responseHeadersToExclude.contains(key.toUpperCase())) {
                        if ("Location".equals(key.toUpperCase())) {
                            response.addHeader(key, replacementPathAndClient.revertPath(proxyResourceUrl));
                        }
                        response.addHeader(key, val);
                    }
                }
            }
        }
    }

    private static int copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
    
    private static URI baseUrl(String contextPath, HttpServletRequest request) {
        return URI.create(request.getRequestURL().toString()).resolve(contextPath);
    }

}
