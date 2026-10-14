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

import io.github.nephets.shadowkit.core.ShadowkitTenant;
import io.github.nephets.shadowkit.core.store.InMemoryShadowStore;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShadowkitEngineDeltaTest {

    private final ShadowkitTenant tenant = ShadowkitTenant.of("t");
    private final ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore());

    @Test
    void absent_empty() {
        assertThat(engine.getDelta(tenant, "x")).isEmpty();
    }

    @Test
    void present_with_diff() {
        engine.applyDesired(tenant, "d", Map.of("p", "on"), 1L, Instant.now());
        assertThat(engine.getDelta(tenant, "d")).map(d -> d.state()).contains(Map.of("p", "on"));
    }

    @Test
    void empty_after_reported() {
        engine.applyDesired(tenant, "d", Map.of("p", "on"), 1L, Instant.now());
        engine.applyReported(tenant, "d", Map.of("p", "on"), 2L, Instant.now());
        assertThat(engine.getDelta(tenant, "d")).map(d -> d.isEmpty()).contains(true);
    }
}
