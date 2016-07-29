package org.eurekaclinical.common.config;

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
import java.util.Map;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import org.eurekaclinical.standardapis.props.EurekaClinicalProperties;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;

/**
 * Extend to setup Eureka web applications. This abstract class sets up Guice
 * and binds the authentication and authorization filters that every Eureka web
 * application should have.
 *
 * @author hrathod
 */
public abstract class AbstractServletModule extends ServletModule {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractServletModule.class);
    private final String protectedPath;
    private final ServletModuleSupport servletModuleSupport;
    private final String logoutPath;
    
    protected AbstractServletModule(EurekaClinicalProperties inProperties,
            String inContainerPath, String inProtectedPath,
            String inLogoutPath) {
        if (inProtectedPath == null) {
            throw new IllegalArgumentException("inProtectedPath cannot be null");
        }
        this.servletModuleSupport = new ServletModuleSupport(
                this.getServletContext().getContextPath(), inProperties);
        this.protectedPath = inProtectedPath;
        this.logoutPath = inLogoutPath;
    }

    protected void printParams(Map<String, String> inParams) {
        for (Map.Entry<String, String> entry : inParams.entrySet()) {
            LOGGER.debug(entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void setupCasSingleSignOutFilter() {
        if (this.logoutPath != null) {
            bind(SingleSignOutFilter.class).in(Singleton.class);
            filter(this.logoutPath).through(SingleSignOutFilter.class);
        }
    }

    private void setupCasValidationFilter() {
        bind(Cas20ProxyReceivingTicketValidationFilter.class).in(
                Singleton.class);
        Map<String, String> params = getCasValidationFilterInitParams();
        filter(this.servletModuleSupport.getCasProxyCallbackPath(), this.protectedPath).through(
                Cas20ProxyReceivingTicketValidationFilter.class, params);
    }

    private void setupCasAuthenticationFilter() {
        bind(AuthenticationFilter.class).in(Singleton.class);
        Map<String, String> params
                = this.servletModuleSupport.getCasAuthenticationFilterInitParams();
        filter(this.protectedPath).through(AuthenticationFilter.class, params);
    }

    private void setupCasServletRequestWrapperFilter() {
        bind(HttpServletRequestWrapperFilter.class).in(Singleton.class);
        Map<String, String> params
                = this.servletModuleSupport.getServletRequestWrapperFilterInitParams();
        filter("/*").through(HttpServletRequestWrapperFilter.class, params);
    }

    private void setupCasThreadLocalAssertionFilter() {
        bind(AssertionThreadLocalFilter.class).in(Singleton.class);
        filter("/*").through(AssertionThreadLocalFilter.class);
    }

    protected String getCasProxyCallbackPath() {
        return this.servletModuleSupport.getCasProxyCallbackPath();
    }

    protected String getCasProxyCallbackUrl() {
        return this.servletModuleSupport.getCasProxyCallbackUrl();
    }

    protected abstract Map<String, String> getCasValidationFilterInitParams();

    protected abstract void setupServlets();

    /**
     * Override to setup additional filters. The default implementation does
     * nothing.
     */
    protected void setupFilters() {
    }

    @Override
    protected final void configureServlets() {
        super.configureServlets();
        /*
		 * CAS filters must go before other filters.
         */
        this.setupCasFilters();
        this.setupFilters();
        this.setupServlets();
    }

    /*
     * Sets up CAS filters. The filter order is specified in
     * https://wiki.jasig.org/display/CASC/Configuring+Single+Sign+Out
     * and
     * https://wiki.jasig.org/display/CASC/CAS+Client+for+Java+3.1
     */
    private void setupCasFilters() {
        this.setupCasSingleSignOutFilter();
        this.setupCasAuthenticationFilter();
        this.setupCasValidationFilter();
        this.setupCasServletRequestWrapperFilter();
        this.setupCasThreadLocalAssertionFilter();
    }

}
