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
 * Manages the lifecycle of a Eureka! Clinical REST API client that is bound in
 * Guice with session scope. Use it as follows, where <code>Client</code> is 
 * the name of a subclass of {@link EurekaClinicalClient}:
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
    private EurekaClinicalClient client;

    /**
     * Creates a listener for instances of the specified session-scoped client 
     * class.
     *
     * @param inClientCls the client class. Cannot be <code>null</code>.
     */
    public ClientSessionListener(Class<? extends EurekaClinicalClient> inClientCls) {
        if (inClientCls == null) {
            throw new IllegalArgumentException("inClientCls cannot be null");
        }
        this.clientCls = inClientCls;
    }

    /**
     * Gets the client instance from Guice and stores it so that we can close 
     * it when the session is destroyed.
     * 
     * We cannot just get the client instance in {@link #sessionDestroyed(javax.servlet.http.HttpSessionEvent) }
     * because it may be called from outside of session scope. We have observed
     * this happening when Tomcat sweeps its list of sessions to determine if
     * any of them are expired and can be destroyed.
     *
     * @param hse the session event.
     */
    @Override
    public void sessionCreated(HttpSessionEvent hse) {
        ServletContext servletContext = hse.getSession().getServletContext();
        LOGGER.info("Creating client {} for service {}",
                this.clientCls.getName(),
                servletContext.getContextPath());
        Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());
        try {
            this.client = injector.getInstance(this.clientCls);
        } catch (ConfigurationException ce) {
            LOGGER.error("Error creating client "
                    + this.clientCls.getName()
                    + " for service "
                    + servletContext.getContextPath(), ce);
        }
    }

    /**
     * Attempts to close the client.
     *
     * @param hse the session event.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        ServletContext servletContext = hse.getSession().getServletContext();
        if (this.client != null) {
            LOGGER.info("Destroying client {} for service {}",
                    this.clientCls.getName(),
                    servletContext.getContextPath());

            this.client.close();
        } else {
            LOGGER.warn("The client {} for service {} was not created at the start of the session, so there is nothing to close.",
                    this.clientCls.getName(),
                    servletContext.getContextPath());
        }
    }
}
