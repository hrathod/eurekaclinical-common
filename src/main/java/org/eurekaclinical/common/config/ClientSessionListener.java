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

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eurekaclinical.common.comm.clients.EurekaClinicalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the lifecycle of a session-scoped REST API client. Invoke it as
 * follows, where <code>Client</code> is the name of a subclass of
 * {@link EurekaClinicalClient}:
 * 
 * <pre>
 * servletContext.addSessionListener(new ClientSessionListener(Client.class));
 * </pre>
 * 
 * @author Andrew Post
 */
public class ClientSessionListener implements HttpSessionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSessionListener.class);

    private final Class<? extends EurekaClinicalClient> clientCls;
    
    /**
     * Creates a listener for instances of the specified client class.
     * 
     * @param inClientCls the client class.
     */
    public ClientSessionListener(Class<? extends EurekaClinicalClient> inClientCls) {
        this.clientCls = inClientCls;
    }

    /**
     * Just logs the creation of the session.
     * 
     * @param hse the session event.
     */
    @Override
    public void sessionCreated(HttpSessionEvent hse) {
        LOGGER.info("Creating session for client {} for service {}", 
                this.clientCls.getName(), 
                hse.getSession().getServletContext().getContextPath());
    }

    /**
     * Logs the destruction of the session and attempts to close the client.
     * Logs any error in closing the client.
     * 
     * @param hse the session event.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        ServletContext servletContext = hse.getSession().getServletContext();
        LOGGER.info("Destroying session for client {} for service {}", 
                this.clientCls.getName(), 
                servletContext.getContextPath());
        Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());
        try {
            injector.getInstance(this.clientCls).close();
        } catch (ConfigurationException ce) {
            LOGGER.error("Error destroying session for client " + 
                    this.clientCls.getName() + 
                    " for service " + 
                    servletContext.getContextPath(), ce);
        }
    }
}
