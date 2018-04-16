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
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import org.eurekaclinical.common.filter.AutoAuthorizationFilter;
import org.eurekaclinical.common.filter.ConditionalCasAuthenticationFilter;
import org.eurekaclinical.common.filter.ConditionalHttpServletRequestWrapperFilter;
import org.eurekaclinical.common.filter.HasAuthenticatedSessionFilter;
import org.eurekaclinical.common.filter.JwtFilter;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eurekaclinical.standardapis.props.CasJerseyEurekaClinicalProperties;

/**
 * Extend to setup Eureka RESTful web services. This abstract class sets up
 * Guice and Jersey and binds the authentication and authorization filters that
 * every Eureka web service should have.
 *
 * @author hrathod
 */
public abstract class AbstractJerseyServletModule extends JerseyServletModule {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractJerseyServletModule.class);
    private static final String UNPROTECTED_PATH = "/*";
    private static final String UNPROTECTED_API_PATH = "/api/*";
    private static final String PROTECTED_API_PATH = "/api/protected/*";
    private static final String TEMPLATES_PATH = "/WEB-INF/templates";
    private static final String WEB_CONTENT_REGEX = "(/(image|js|css)/?.*)|(/.*\\.jsp)|(/WEB-INF/.*\\.jsp)|(/WEB-INF/.*\\.jspf)|(/.*\\.html)|(/favicon\\.ico)|(/robots\\.txt)";
    private final String packageNames;
    private final ServletModuleSupport servletModuleSupport;
    private final CasJerseyEurekaClinicalProperties properties;

    @Inject(optional = true)
    private AutoAuthorizationFilter autoAuthorizationFilter;
    private final boolean doesProxy;

    protected AbstractJerseyServletModule(CasJerseyEurekaClinicalProperties inProperties,
            String inPackageNames, boolean inDoesProxy) {
        this.servletModuleSupport = new ServletModuleSupport(this
                .getServletContext().getContextPath(), inProperties);
        this.packageNames = inPackageNames;
        this.properties = inProperties;
        this.doesProxy = inDoesProxy;
    }

    protected void printParams(Map<String, String> inParams) {
        for (Map.Entry<String, String> entry : inParams.entrySet()) {
            LOGGER.debug(entry.getKey() + " -> " + entry.getValue());
        }
    }

    protected String getCasProxyCallbackPath() {
        return this.servletModuleSupport.getCasProxyCallbackPath();
    }

    protected String getCasProxyCallbackUrl() {
        return this.servletModuleSupport.getCasProxyCallbackUrl();
    }

    protected Map<String, String> getCasValidationFilterInitParams() {
        Map<String, String> params = new HashMap<>();
        params.put("casServerUrlPrefix", this.properties.getCasUrl());
        params.put("serverName", this.properties.getProxyCallbackServer());
        params.put("redirectAfterValidation", "false");
        params.put("acceptAnyProxy", "true");
        if (this.doesProxy) {
            params.put("proxyCallbackUrl", getCasProxyCallbackUrl());
            params.put("proxyReceptorUrl", getCasProxyCallbackPath());
        }
        return params;
    }

    private void setupCasValidationFilter() {
        bind(Cas20ProxyReceivingTicketValidationFilter.class).in(
                Singleton.class);
        Map<String, String> params = getCasValidationFilterInitParams();
        filter(this.servletModuleSupport.getCasProxyCallbackPath(), PROTECTED_API_PATH).through(
                Cas20ProxyReceivingTicketValidationFilter.class, params);
    }

    private void setupCasAuthenticationFilter() {
        Map<String, String> params = this.servletModuleSupport
                .getCasAuthenticationFilterInitParams();
        if (this.properties.isJwtEnabled()) {
            bind(ConditionalCasAuthenticationFilter.class).in(Singleton.class);
            filter(PROTECTED_API_PATH).through(ConditionalCasAuthenticationFilter.class, params);
        } else {
            bind(AuthenticationFilter.class).in(Singleton.class);
            filter(PROTECTED_API_PATH).through(AuthenticationFilter.class, params);
        }
    }

    private void setupCasServletRequestWrapperFilter() {
        Map<String, String> params = this.servletModuleSupport
                .getServletRequestWrapperFilterInitParams();
        if (this.properties.isJwtEnabled()) {
            bind(ConditionalHttpServletRequestWrapperFilter.class).in(Singleton.class);
            filter(UNPROTECTED_PATH).through(ConditionalHttpServletRequestWrapperFilter.class, params);
        } else {
            bind(HttpServletRequestWrapperFilter.class).in(Singleton.class);
            filter(UNPROTECTED_PATH).through(HttpServletRequestWrapperFilter.class, params);
        }
    }

    private void setupCasThreadLocalAssertionFilter() {
        bind(AssertionThreadLocalFilter.class).in(Singleton.class);
        filter(UNPROTECTED_PATH).through(AssertionThreadLocalFilter.class);
    }

    private void setupContainer() {
        Map<String, String> params = new HashMap<>();
        params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, this.packageNames);
        params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                RolesAllowedResourceFilterFactory.class.getName());
        params.put(ServletContainer.JSP_TEMPLATES_BASE_PATH, TEMPLATES_PATH);
        params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
                WEB_CONTENT_REGEX);
        if (LOGGER.isDebugEnabled()) {
            this.printParams(params);
        }
        serve(UNPROTECTED_API_PATH).with(GuiceContainer.class, params);
    }

    protected void setupFilters() {
        filter(PROTECTED_API_PATH).through(HasAuthenticatedSessionFilter.class);
    }

    protected void setupAutoAuthorization() {
        if (this.autoAuthorizationFilter != null) {
            filter(UNPROTECTED_PATH).through(AutoAuthorizationFilter.class);
        }
    }

    protected void setupJwtFilter () {
        Map<String, String> params = new HashMap<>();
        params.put(JwtFilter.SECRET_PARAM_NAME, this.properties.getJwtSecret());
        params.put(JwtFilter.WHITELIST_PARAM_NAME, this.properties.getJwtWhitelist());
        filter(PROTECTED_API_PATH).through(JwtFilter.class, params);
    }

    @Override
    protected void configureServlets() {
        super.configureServlets();
        /*
         * JWT and CAS filters must go before other filters.
         */
        if (this.properties.isJwtEnabled()) {
            this.setupJwtFilter();
        }
        this.setupCasFilters();
        this.setupAutoAuthorization();
        this.setupFilters();
        this.setupContainer();
    }

    /**
     * Sets up CAS filters. The filter order is specified in
     * https://wiki.jasig.org/display/casc/configuring+the+jasig+cas+client+for+java+in+the+web.xml
     */
    private void setupCasFilters() {
        this.setupCasValidationFilter();
        this.setupCasAuthenticationFilter();
        this.setupCasServletRequestWrapperFilter();
        this.setupCasThreadLocalAssertionFilter();
    }

}
