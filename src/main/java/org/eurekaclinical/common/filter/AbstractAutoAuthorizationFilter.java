package org.eurekaclinical.common.filter;

/*-
 * #%L
 * Eureka! Clinical Standard APIs
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
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eurekaclinical.standardapis.dao.UserDao;
import org.eurekaclinical.standardapis.dao.UserTemplateDao;
import org.eurekaclinical.standardapis.entity.RoleEntity;
import org.eurekaclinical.standardapis.entity.UserEntity;
import org.eurekaclinical.standardapis.entity.UserTemplateEntity;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractAutoAuthorizationFilter<R extends RoleEntity, U extends UserEntity<R>, T extends UserTemplateEntity<R>> implements AutoAuthorizationFilter {

    private final UserTemplateDao<T> userTemplateDao;
    private final UserDao<U> userDao;

    @Inject
    public AbstractAutoAuthorizationFilter(UserTemplateDao<T> inUserTemplateDao,
            UserDao<U> inUserDao) {
        this.userTemplateDao = inUserTemplateDao;
        this.userDao = inUserDao;
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest inRequest, ServletResponse inResponse, FilterChain inFilterChain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) inRequest;
        AttributePrincipal userPrincipal = (AttributePrincipal) servletRequest.getUserPrincipal();
        boolean autoAuthorizationPermitted = true;
        if (autoAuthorizationPermitted) {
            String remoteUser = servletRequest.getRemoteUser();
            if (remoteUser != null) {
                preAuthorizationHook(servletRequest);
                T autoAuthorizationTemplate = this.userTemplateDao.getAutoAuthorizationTemplate();
                if (this.userDao.getByName(remoteUser) == null) {
                    if (autoAuthorizationTemplate != null) {
                        U user = toUserEntity(autoAuthorizationTemplate, remoteUser);
                        this.userDao.create(user);
                    }
                }
                postAuthorizationHook(autoAuthorizationTemplate, servletRequest);
            }
        }
        inFilterChain.doFilter(inRequest, inResponse);
    }

    @Override
    public void destroy() {
    }

    protected abstract U toUserEntity(T userTemplate, String username);

    protected void preAuthorizationHook(HttpServletRequest req) {
    }

    protected void postAuthorizationHook(T userTemplate, HttpServletRequest req) {
    }
}
