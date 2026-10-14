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
package io.github.nephets.shadowkit.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowkitTenantTest {

    @Test
    void of_holds_id() {
        assertThat(ShadowkitTenant.of("t1").id()).isEqualTo("t1");
    }

    @Test
    void of_null_throws() {
        assertThatThrownBy(() -> ShadowkitTenant.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_blank_throws() {
        assertThatThrownBy(() -> ShadowkitTenant.of(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_id() {
        assertThat(ShadowkitTenant.of("a")).isEqualTo(ShadowkitTenant.of("a"));
        assertThat(ShadowkitTenant.of("a")).isNotEqualTo(ShadowkitTenant.of("b"));
    }

    @Test
    void hashCode_consistent() {
        assertThat(ShadowkitTenant.of("a").hashCode())
                .isEqualTo(ShadowkitTenant.of("a").hashCode());
    }

    @Test
    void toString_contains_id() {
        assertThat(ShadowkitTenant.of("x").toString()).contains("x");
    }
}
