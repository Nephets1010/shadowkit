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

/** 当前请求的租户上下文（线程局部）。 */
public final class ShadowkitTenantContext {

    private static final ThreadLocal<ShadowkitTenant> HOLDER = new ThreadLocal<>();

    private ShadowkitTenantContext() {}

    public static void set(ShadowkitTenant tenant) {
        HOLDER.set(tenant);
    }

    public static ShadowkitTenant get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
