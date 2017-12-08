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

import org.eurekaclinical.common.filter.RolesFromServiceFilter;
import org.eurekaclinical.standardapis.props.CasJerseyEurekaClinicalProperties;

/**
 * Extend to setup Eureka RESTful web services. This abstract class sets up
 * Guice and Jersey and binds the authentication and authorization filters that
 * every Eureka web service should have. Role information will come from a REST
 * API client that is bound to 
 * {@link org.eurekaclinical.common.comm.clients.AuthorizingEurekaClinicalClient} 
 * in your Guice configuration.
 *
 * @author hrathod
 */
public abstract class AbstractAuthorizingJerseyServletModule extends AbstractJerseyServletModule {

    protected AbstractAuthorizingJerseyServletModule(CasJerseyEurekaClinicalProperties inProperties,
            String inPackageNames, boolean inDoesProxy) {
        super(inProperties, inPackageNames, inDoesProxy);
    }

    @Override
    protected void setupFilters() {
        super.setupFilters();
        filter("/*").through(RolesFromServiceFilter.class);
    }
    
}
