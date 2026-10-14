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
import io.github.nephets.shadowkit.core.exception.ShadowkitValidationException;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;
import io.github.nephets.shadowkit.core.store.InMemoryShadowStore;
import io.github.nephets.shadowkit.core.thing.PropertyDefinition;
import io.github.nephets.shadowkit.core.thing.PropertyType;
import io.github.nephets.shadowkit.core.thing.ThingModel;
import io.github.nephets.shadowkit.core.thing.ThingModelRegistry;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowkitEngineThingModelTest {

    private final ShadowkitTenant tenant = ShadowkitTenant.of("t");
    private final ThingModel model =
            ThingModel.of(
                    PropertyDefinition.of("power", PropertyType.STRING),
                    PropertyDefinition.of("targetTemp", PropertyType.NUMBER));

    @Test
    void unknown_property_rejected_and_shadow_unchanged() {
        ThingModelRegistry registry = new ThingModelRegistry();
        registry.register(tenant, "d", model);
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore(), registry);

        engine.applyDesired(tenant, "d", Map.of("power", "on"), 1L, Instant.now());

        assertThatThrownBy(
                        () ->
                                engine.applyDesired(
                                        tenant, "d", Map.of("unknown", 1), 2L, Instant.now()))
                .isInstanceOf(ShadowkitValidationException.class);

        DeviceShadow after = engine.getShadow(tenant, "d").orElseThrow();
        assertThat(after.version()).isEqualTo(2);
        assertThat(after.desired()).containsEntry("power", "on");
        assertThat(after.desired()).doesNotContainKey("unknown");
    }

    @Test
    void type_mismatch_rejected() {
        ThingModelRegistry registry = new ThingModelRegistry();
        registry.register(tenant, "d", model);
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore(), registry);

        assertThatThrownBy(
                        () ->
                                engine.applyDesired(
                                        tenant, "d", Map.of("power", 123), 1L, Instant.now()))
                .isInstanceOf(ShadowkitValidationException.class);
    }

    @Test
    void valid_patch_updates() {
        ThingModelRegistry registry = new ThingModelRegistry();
        registry.register(tenant, "d", model);
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore(), registry);

        DeviceShadow s =
                engine.applyDesired(
                        tenant, "d", Map.of("power", "on", "targetTemp", 26), 1L, Instant.now());
        assertThat(s.version()).isEqualTo(2);
        assertThat(s.desired()).containsEntry("power", "on").containsEntry("targetTemp", 26);
    }

    @Test
    void reported_validated_too() {
        ThingModelRegistry registry = new ThingModelRegistry();
        registry.register(tenant, "d", model);
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore(), registry);

        assertThatThrownBy(
                        () ->
                                engine.applyReported(
                                        tenant, "d", Map.of("flux", true), 1L, Instant.now()))
                .isInstanceOf(ShadowkitValidationException.class);

        DeviceShadow s =
                engine.applyReported(tenant, "d", Map.of("power", "on"), 1L, Instant.now());
        assertThat(s.reported()).containsEntry("power", "on");
    }

    @Test
    void no_registry_means_no_validation_backward_compat() {
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore());
        DeviceShadow s =
                engine.applyDesired(tenant, "d", Map.of("anything", 999), 1L, Instant.now());
        assertThat(s.version()).isEqualTo(2);
        assertThat(s.desired()).containsEntry("anything", 999);
    }

    @Test
    void registry_present_but_device_unregistered_passes() {
        ThingModelRegistry registry = new ThingModelRegistry();
        ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore(), registry);
        DeviceShadow s =
                engine.applyDesired(tenant, "d", Map.of("wild", "west"), 1L, Instant.now());
        assertThat(s.desired()).containsEntry("wild", "west");
    }
}
