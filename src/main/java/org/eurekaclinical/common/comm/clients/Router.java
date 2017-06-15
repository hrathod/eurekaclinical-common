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
import com.google.inject.servlet.SessionScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;

/**
 *
 * @author Andrew Post
 */
@SessionScoped
public class Router {

    private final Route[] routes;
    private Map<EurekaClinicalClient, Pattern> patterns;

    @Inject
    public Router(RouterTable routesParser) throws RouterTableLoadException {
        this.routes = routesParser.load();
        this.patterns = new HashMap<>();
        for (Route route : this.routes) {
            EurekaClinicalClient client = route.getClient();
            if (this.patterns.containsKey(client)) {
                this.patterns.put(client, Pattern.compile(client.getResourceUrl().toString()));
            }
        }
    }

    public ReplacementPathAndClient getReplacementPathAndClient(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        for (Route route : this.routes) {
            String replacementPath = route.replace(path);
            EurekaClinicalClient client = route.getClient();
            if (replacementPath != null) {
                return new ReplacementPathAndClient(replacementPath, client, this.patterns.get(client));
            }
        }
        return null;
    }
}
