package org.eurekaclinical.common.comm.clients;

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

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Andrew Post
 */
@Singleton
public class Router {
    private final Route[] routes;

    @Inject
    public Router(RouterTable routesParser) throws RouterTableLoadException {
        this.routes = routesParser.load();
    }
    
    public ReplacementPathAndClient getReplacementPathAndClient(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        for (Route route : this.routes) {
            String replacementPath = route.replace(path);
            if (replacementPath != null) {
                return new ReplacementPathAndClient(replacementPath, route.getClient());
            }
        }
        return null;
    }
}
