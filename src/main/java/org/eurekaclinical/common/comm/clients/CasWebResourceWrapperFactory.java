package org.eurekaclinical.common.comm.clients;

/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Date;
import java.util.List;
import org.apache.http.cookie.Cookie;

/**
 *
 * @author Andrew Post
 */
class CasWebResourceWrapperFactory implements WebResourceWrapperFactory {

    @Override
    public WebResourceWrapper getInstance(ApacheHttpClient4 client, URI resourceUrl) {
        if (hasCookieFor(client, resourceUrl)) {
            return new DefaultWebResourceWrapper(client.resource(resourceUrl));
        } else {
            return new CasWebResourceWrapper(client.resource(resourceUrl));
        }
    }

    private boolean hasCookieFor(ApacheHttpClient4 client, URI resourceUrl) {
        String path = resourceUrl.getPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        List<Cookie> cookies = client.getClientHandler().getCookieStore().getCookies();
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getName())
                    && domainMatches(cookie.getDomain(), resourceUrl.getHost())
                    && cookie.getPath().equals(path)
                    && (cookie.getExpiryDate() == null || cookie.getExpiryDate().after(new Date()))) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean domainMatches(String domain, String host) {
        if ("localhost".equalsIgnoreCase(domain)) {
            return host.equalsIgnoreCase("localhost");
        } else {
            return HttpCookie.domainMatches(domain, host);
        }
    }

}
