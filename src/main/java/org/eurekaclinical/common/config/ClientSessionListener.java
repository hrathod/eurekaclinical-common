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
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import org.eurekaclinical.common.comm.clients.EurekaClinicalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the lifecycle of Eureka! Clinical REST API clients that are bound in
 * Guice with session scope.
 *
 * @author Andrew Post
 */
public class ClientSessionListener implements HttpSessionAttributeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSessionListener.class);

    /**
     * Does nothing.
     *
     * @param hse the session binding event.
     */
    @Override
    public void attributeAdded(HttpSessionBindingEvent hse) {
    }

    /**
     * Attempts to close the old client.
     *
     * @param hse the session binding event.
     */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent hse) {
        Object possibleClient = hse.getValue();
        closeClient(possibleClient);
    }

    /**
     * Attempts to close the client.
     *
     * @param hse the session event.
     */
    @Override
    public void attributeRemoved(HttpSessionBindingEvent hse) {
        Object possibleClient = hse.getValue();
        closeClient(possibleClient);
    }

    /**
     * Actually closes the client, if it is a {@link EurekaClinicalClient}.
     *
     * @param possibleClient the client. If it is not a
     * {@link EurekaClinicalClient}, it is ignored.
     */
    private void closeClient(Object possibleClient) {
        if (possibleClient instanceof EurekaClinicalClient) {
            LOGGER.info("closing EurekaClinicalClient {}", possibleClient.getClass().getName());
            ((EurekaClinicalClient) possibleClient).close();
        }
    }
}
