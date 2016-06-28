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

import com.google.inject.persist.PersistFilter;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import java.util.HashMap;
import java.util.Map;

/**
 * Extend to setup Eureka RESTful web services. This abstract class sets up
 * Guice and Jersey and binds the authentication and authorization filters that
 * every Eureka web service should have.
 *
 * @author hrathod
 */
public abstract class AbstractTestJerseyServletModuleWithPersist extends JerseyServletModule {

    private static final String CONTAINER_PATH = "/api/*";

    private final String packageNames;

    protected AbstractTestJerseyServletModuleWithPersist(String inPackageNames) {
        this.packageNames = inPackageNames;
    }
    
    public String getContainerPath() {
        return CONTAINER_PATH;
    }

    @Override
    protected void configureServlets() {
        /**
         * Guice docs say that PersistFilter must be registered before any other
         * filter.
         */
        filter(CONTAINER_PATH).through(PersistFilter.class);
        Map<String, String> params = new HashMap<>();
        params.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                this.packageNames);
        params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                RolesAllowedResourceFilterFactory.class.getName());
        serve(CONTAINER_PATH).with(GuiceContainer.class, params);
    }

}
