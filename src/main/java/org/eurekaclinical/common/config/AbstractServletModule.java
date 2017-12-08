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

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import java.util.HashMap;
import org.eurekaclinical.common.filter.HasAuthenticatedSessionFilter;
import org.eurekaclinical.common.filter.InvalidateSessionFilter;
import org.eurekaclinical.common.servlet.DestroySessionServlet;
import org.eurekaclinical.common.servlet.LoginServlet;
import org.eurekaclinical.common.servlet.PostMessageLoginServlet;
import org.eurekaclinical.common.servlet.ProxyServlet;
import org.eurekaclinical.common.servlet.SessionPropertiesServlet;
import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;

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

    private static final String UNPROTECTED_PATH = "/*";
    private static final String PROTECTED_PATH = "/protected/*";
    
    private final ServletModuleSupport servletModuleSupport;

    protected AbstractServletModule(CasEurekaClinicalProperties inProperties) {
        this.servletModuleSupport = new ServletModuleSupport(
                this.getServletContext().getContextPath(), inProperties);
    }

    protected String getCasProxyCallbackPath() {
        return this.servletModuleSupport.getCasProxyCallbackPath();
    }

    protected String getCasProxyCallbackUrl() {
        return this.servletModuleSupport.getCasProxyCallbackUrl();
    }

    protected abstract void setupServlets();

    /**
     * Override to setup additional filters. The default implementation sets
     * up default filters and must be called.
     */
    protected void setupFilters() {
        filter(getProtectedPath()).through(HasAuthenticatedSessionFilter.class);
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
    
    protected String getProtectedPath() {
        return PROTECTED_PATH;
    }
    
    protected void serveLogin() {
        serve("/protected/login").with(LoginServlet.class);
    }
    
    protected void serveGetSession() {
        serve("/protected/get-session").with(PostMessageLoginServlet.class);
    }
    
    protected void serveProxyResource() {
        serve("/proxy-resource/*").with(ProxyServlet.class);
    }
    
    protected void serveDestroySession() {
        serve("/destroy-session").with(DestroySessionServlet.class);  
    }
    
    protected void serveGetSessionProperties() {
        serve("/protected/get-session-properties").with(SessionPropertiesServlet.class);;
    }
    
    protected Map<String, String> getCasValidationFilterInitParams() {
        Map<String, String> params = new HashMap<>();
        CasEurekaClinicalProperties properties = 
                this.servletModuleSupport.getApplicationProperties();
        params.put("casServerUrlPrefix", properties.getCasUrl());
        params.put("serverName", properties.getProxyCallbackServer());
        params.put("proxyCallbackUrl", getCasProxyCallbackUrl());
        params.put("proxyReceptorUrl", getCasProxyCallbackPath());
        return params;
    }

    /*
     * Sets up CAS filters. The filter order is specified in
     * https://wiki.jasig.org/display/CASC/Configuring+Single+Sign+Out
     * and
     * https://wiki.jasig.org/display/CASC/CAS+Client+for+Java+3.1
     */
    private void setupCasFilters() {
        filter(UNPROTECTED_PATH).through(InvalidateSessionFilter.class);
        this.setupCasSingleSignOutFilter();
        this.setupCasAuthenticationFilter();
        this.setupCasValidationFilter();
        this.setupCasServletRequestWrapperFilter();
        this.setupCasThreadLocalAssertionFilter();
    }
    
    private void setupCasSingleSignOutFilter() {
        bind(SingleSignOutFilter.class).in(Singleton.class);
        filter(UNPROTECTED_PATH).through(SingleSignOutFilter.class);
    }

    private void setupCasValidationFilter() {
        bind(Cas20ProxyReceivingTicketValidationFilter.class).in(
                Singleton.class);
        Map<String, String> params = getCasValidationFilterInitParams();
        filter(this.servletModuleSupport.getCasProxyCallbackPath(), getProtectedPath()).through(
                Cas20ProxyReceivingTicketValidationFilter.class, params);
    }

    private void setupCasAuthenticationFilter() {
        bind(AuthenticationFilter.class).in(Singleton.class);
        Map<String, String> params
                = this.servletModuleSupport.getCasAuthenticationFilterInitParams();
        Map<String, String> sessionParams = new HashMap<>(params);
        sessionParams.put("gateway", "true");
        Map<String, String> loginParams = new HashMap<>(params);
        loginParams.put("gateway", "false");
        filter("/protected/login").through(AuthenticationFilter.class, loginParams);
        filter("/protected/get-session").through(AuthenticationFilter.class, sessionParams);
        filterRegex("^/protected/(?!get-session).*").through(AuthenticationFilter.class, params);
    }

    private void setupCasServletRequestWrapperFilter() {
        bind(HttpServletRequestWrapperFilter.class).in(Singleton.class);
        Map<String, String> params
                = this.servletModuleSupport.getServletRequestWrapperFilterInitParams();
        filter(UNPROTECTED_PATH).through(HttpServletRequestWrapperFilter.class, params);
    }

    private void setupCasThreadLocalAssertionFilter() {
        bind(AssertionThreadLocalFilter.class).in(Singleton.class);
        filter(UNPROTECTED_PATH).through(AssertionThreadLocalFilter.class);
    }
    
}
