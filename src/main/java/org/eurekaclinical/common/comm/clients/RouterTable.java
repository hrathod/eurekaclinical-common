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

/**
 * Interface for a routing table for the 
 * {@link org.eurekaclinical.common.servlet.ProxyServlet}.
 * 
 * A route is a prefix string of the path part of a URL. This class parses a
 * file or other representation of a set of routes, each mapped to a
 * {@link EurekaClinicalClient} that implements connectivity to a Eureka!
 * Clinical web service.
 * 
 * An implementation of this interface might parse a file, read from a 
 * database, or hard-code routes in Java code.
 * 
 * @author Andrew Post
 */
public interface RouterTable {
    Route[] load() throws RouterTableLoadException;
}
