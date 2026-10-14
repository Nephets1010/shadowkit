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
package io.github.nephets.shadowkit.core.engine;

import io.github.nephets.shadowkit.core.ShadowVersionConflictException;
import io.github.nephets.shadowkit.core.ShadowkitTenant;
import io.github.nephets.shadowkit.core.store.InMemoryShadowStore;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowkitEngineVersionConflictTest {

    private final ShadowkitTenant tenant = ShadowkitTenant.of("t");
    private final ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore());

    @Test
    void desired_wrong_version_throws() {
        assertThatThrownBy(
                        () ->
                                engine.applyDesired(
                                        tenant, "d", Map.of("p", "on"), 9L, Instant.now()))
                .isInstanceOf(ShadowVersionConflictException.class);
    }

    @Test
    void reported_wrong_version_throws() {
        assertThatThrownBy(
                        () ->
                                engine.applyReported(
                                        tenant, "d", Map.of("p", "on"), 9L, Instant.now()))
                .isInstanceOf(ShadowVersionConflictException.class);
    }

    @Test
    void correct_version_passes() {
        assertThat(engine.applyDesired(tenant, "d", Map.of("p", "on"), 1L, Instant.now()).version())
                .isEqualTo(2);
    }

    @Test
    void message_has_device() {
        assertThatThrownBy(() -> engine.applyDesired(tenant, "d", Map.of(), 9L, Instant.now()))
                .isInstanceOf(ShadowVersionConflictException.class)
                .hasMessageContaining("d");
    }
}
