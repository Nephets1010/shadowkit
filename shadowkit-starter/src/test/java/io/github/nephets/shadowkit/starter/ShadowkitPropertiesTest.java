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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShadowkitPropertiesTest {

    @Test
    void enabled_default_true() {
        assertThat(new ShadowkitProperties().isEnabled()).isTrue();
    }

    @Test
    void set_enabled() {
        ShadowkitProperties p = new ShadowkitProperties();
        p.setEnabled(false);
        assertThat(p.isEnabled()).isFalse();
    }

    @Test
    void tenant_header_default() {
        assertThat(new ShadowkitProperties().getTenantHeader()).isEqualTo("X-Tenant-Id");
    }

    @Test
    void set_tenant_header() {
        ShadowkitProperties p = new ShadowkitProperties();
        p.setTenantHeader("X-Org");
        assertThat(p.getTenantHeader()).isEqualTo("X-Org");
    }
}
