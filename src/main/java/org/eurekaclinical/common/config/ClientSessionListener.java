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
import java.text.MessageFormat;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eurekaclinical.common.comm.clients.EurekaClinicalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the lifecycle of a Eureka! Clinical REST API client that is bound in
 * Guice with session scope. Use it as follows, where <code>Client</code> is the
 * name of a subclass of {@link EurekaClinicalClient}:
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
     * Format of the session attributes that Guice creates when creating
     * session-scoped class instances.
     */
    private final MessageFormat clientAttributeFormat
            = new MessageFormat("Key[type={0}, annotation=[none]]");

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
     * Does nothing.
     *
     * @param hse the session event.
     */
    @Override
    public void sessionCreated(HttpSessionEvent hse) {
    }

    /**
     * Attempts to close the client.
     *
     * @param hse the session event.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        ServletContext servletContext = session.getServletContext();
        /**
         * We cannot use regular Guice injection to get the client instance
         * because injection will fail with an OutOfScopeException if the
         * session times out and Tomcat has to reap it. Guice stores the
         * session-scoped class instances that it manages as session
         * attributes, so instead we can get the client instance using its 
         * attribute name.
         *
         * @param hse the session event.
         */
        String sessionAttr = this.clientAttributeFormat.format(
                new Object[]{this.clientCls.getName()});
        EurekaClinicalClient client
                = (EurekaClinicalClient) session.getAttribute(sessionAttr);
        if (client != null) {
            client.close();
            LOGGER.info("Client {} for service {} closed",
                    this.clientCls.getName(),
                    servletContext.getContextPath());
        }
    }
}
