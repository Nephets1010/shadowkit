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
package io.github.nephets.shadowkit.core.shadow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceShadowDeltaTest {

    @Test
    void delta_empty_when_equal() {
        DeviceShadow s =
                DeviceShadow.of("d")
                        .applyDesired(Map.of("p", "on"), Instant.now())
                        .applyReported(Map.of("p", "on"), Instant.now());
        assertThat(s.delta().isEmpty()).isTrue();
    }

    @Test
    void delta_contains_diff() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("p", "on"), Instant.now());
        assertThat(s.delta().state()).containsEntry("p", "on");
    }

    @Test
    void delta_absent_reported_key() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("p", "on"), Instant.now());
        assertThat(s.delta().containsKey("p")).isTrue();
    }

    @Test
    void delta_shrinks_after_reported() {
        DeviceShadow s =
                DeviceShadow.of("d")
                        .applyDesired(Map.of("p", "on"), Instant.now())
                        .applyReported(Map.of("p", "on"), Instant.now());
        assertThat(s.delta().state()).doesNotContainKey("p");
    }

    @Test
    void delta_value_mismatch() {
        DeviceShadow s =
                DeviceShadow.of("d")
                        .applyDesired(Map.of("p", "on"), Instant.now())
                        .applyReported(Map.of("p", "off"), Instant.now());
        assertThat(s.delta().state()).containsEntry("p", "on");
    }

    @Test
    void delta_version_matches_shadow() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("p", "on"), Instant.now());
        assertThat(s.delta().version()).isEqualTo(s.version());
    }
}
