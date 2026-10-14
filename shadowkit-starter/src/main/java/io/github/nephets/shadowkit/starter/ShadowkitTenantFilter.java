/*
 * Copyright 2026 The shadowkit Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nephets.shadowkit.starter;

import io.github.nephets.shadowkit.core.ShadowkitTenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 从请求头解析租户并放入线程上下文，请求结束后清理。 */
public class ShadowkitTenantFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(ShadowkitTenantFilter.class);

    private final String tenantHeader;

    public ShadowkitTenantFilter(ShadowkitProperties properties) {
        this.tenantHeader = properties.getTenantHeader();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {
        log.debug("{} {} entry", request.getMethod(), request.getRequestURI());
        String value = request.getHeader(tenantHeader);
        if (value != null && !value.isBlank()) {
            ShadowkitTenantContext.set(ShadowkitTenant.of(value.trim()));
        }
        try {
            chain.doFilter(request, response);
        } finally {
            ShadowkitTenantContext.clear();
            log.debug("{} {} exit", request.getMethod(), request.getRequestURI());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
