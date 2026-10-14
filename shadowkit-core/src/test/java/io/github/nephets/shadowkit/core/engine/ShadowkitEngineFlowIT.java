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
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;
import io.github.nephets.shadowkit.core.store.InMemoryShadowStore;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 端到端流程：下发期望 → 计算差异 → 设备上报 → 差异清空 → 陈旧版本被拦截。 */
class ShadowkitEngineFlowIT {

    @Test
    void full_sync_flow() {
        ShadowkitTenant tenant = ShadowkitTenant.of("tenant");
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore());

        DeviceShadow s =
                engine.applyDesired(tenant, "dev-1", Map.of("power", "on"), 1L, Instant.now());
        assertThat(engine.getDelta(tenant, "dev-1"))
                .map(d -> d.state())
                .contains(Map.of("power", "on"));

        DeviceShadow reported =
                engine.applyReported(
                        tenant, "dev-1", Map.of("power", "on"), s.version(), Instant.now());
        assertThat(reported.delta().isEmpty()).isTrue();

        assertThatThrownBy(
                        () ->
                                engine.applyDesired(
                                        tenant, "dev-1", Map.of("power", "off"), 1L, Instant.now()))
                .isInstanceOf(ShadowVersionConflictException.class);
    }
}
