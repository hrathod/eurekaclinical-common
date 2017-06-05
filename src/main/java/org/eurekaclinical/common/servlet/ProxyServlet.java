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
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.BufferedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.common.comm.clients.ProxyingClient;

/**
 * @author Sanjay Agravat, Miao Ai
 */
@Singleton
public class ProxyServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProxyServlet.class);
    private static final long serialVersionUID = 1L;

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

        String content = extractContent(servletRequest);
        String path = servletRequest.getPathInfo();

        try {
            client.proxyPut(path, content);
        } catch (ClientException e) {
            servletResponse.setStatus(e.getResponseStatus().getStatusCode());
            servletResponse.getOutputStream().print(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws IOException {
        ProxyingClient client = this.injector.getInstance(ProxyingClient.class);

        String content = extractContent(servletRequest);
        String path = servletRequest.getPathInfo();

        try {
            URI created = client.proxyPost(path, content);
            if (created != null) {
                servletResponse.setStatus(HttpServletResponse.SC_CREATED);
                servletResponse.setHeader("Location", created.toString());
            }
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

        try {
            client.proxyDelete(path);
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

        try {
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            MultivaluedMap<String, String> multivaluedMap = toMultivaluedMap(parameterMap);
            String response = client.proxyGet(path, multivaluedMap);
            servletResponse.getWriter().write(response);
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

    private static String extractContent(HttpServletRequest servletRequest) throws IOException {
        InputStream inputStream = servletRequest.getInputStream();
        String charEncoding = servletRequest.getCharacterEncoding();
        StringBuilder buf = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, charEncoding))) {
            String line;
            while ((line = r.readLine()) != null) {
                buf.append(line);
            }
        }
        String content = buf.toString();
        LOGGER.debug("json: {}", content);
        return content;
    }

}
