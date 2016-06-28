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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.eurekaclinical.standardapis.props.EurekaClinicalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author arpost
 */
public class InjectorSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(InjectorSupport.class);
    private final Injector injector;

    public InjectorSupport(Module[] modules, Stage stage) {
        LOGGER.debug("Creating Guice injector");
        this.injector = Guice.createInjector(
                stage,
                modules.clone());
    }

    public InjectorSupport(Module[] modules, EurekaClinicalProperties properties) {
        LOGGER.debug("Creating Guice injector");
        this.injector = Guice.createInjector(
                Stage.valueOf(properties.getStage()),
                modules.clone());
    }

    public Injector getInjector() {
        return this.injector;
    }
}
