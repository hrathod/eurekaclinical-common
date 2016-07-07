package org.eurekaclinical.common.util;

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
import javax.servlet.http.HttpServletRequest;
import org.eurekaclinical.standardapis.entity.RoleEntity;
import org.eurekaclinical.standardapis.entity.UserEntity;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 *
 * @author Andrew Post
 */
public class UserSupport {

    public AttributePrincipal getUserPrincipal(HttpServletRequest request) {
        return (AttributePrincipal) request.getUserPrincipal();
    }

    public Map<String, Object> getUserPrincipalAttributes(HttpServletRequest request) {
        AttributePrincipal principal = getUserPrincipal(request);
        return principal.getAttributes();
    }

    public boolean isSameUser(HttpServletRequest servletRequest, UserEntity<? extends RoleEntity> user) {
        return isSameUser(servletRequest, user.getUsername());
    }

    public boolean isSameUser(HttpServletRequest servletRequest, String username) {
        AttributePrincipal principal = getUserPrincipal(servletRequest);
        if (!principal.getName().equals(username)) {
            return false;
        }
        return true;
    }
}
