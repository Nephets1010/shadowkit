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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceShadowTest {

    @Test
    void of_empty_version_one() {
        DeviceShadow s = DeviceShadow.of("d1");
        assertThat(s.deviceId()).isEqualTo("d1");
        assertThat(s.version()).isEqualTo(1);
        assertThat(s.desired()).isEmpty();
        assertThat(s.reported()).isEmpty();
    }

    @Test
    void of_blank_id_throws() {
        assertThatThrownBy(() -> DeviceShadow.of(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_version_lt1_throws() {
        assertThatThrownBy(() -> DeviceShadow.of("d", 0, Map.of(), Map.of(), Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void applyDesired_bumps_version() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("p", "on"), Instant.now());
        assertThat(s.version()).isEqualTo(2);
    }

    @Test
    void applyDesired_merges() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("p", "on"), Instant.now());
        assertThat(s.desired()).containsEntry("p", "on");
    }

    @Test
    void applyReported_bumps_version() {
        DeviceShadow s = DeviceShadow.of("d").applyReported(Map.of("p", "on"), Instant.now());
        assertThat(s.version()).isEqualTo(2);
    }

    @Test
    void applyReported_merges() {
        DeviceShadow s = DeviceShadow.of("d").applyReported(Map.of("p", "on"), Instant.now());
        assertThat(s.reported()).containsEntry("p", "on");
    }

    @Test
    void applyDesired_sets_updatedAt() {
        Instant now = Instant.ofEpochSecond(123);
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of(), now);
        assertThat(s.updatedAt()).isEqualTo(now);
    }

    @Test
    void null_patch_treated_empty() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(null, Instant.now());
        assertThat(s.desired()).isEmpty();
    }

    @Test
    void builder_roundtrip() {
        DeviceShadow s =
                DeviceShadow.builder()
                        .deviceId("d")
                        .version(3)
                        .desired(Map.of("a", 1))
                        .reported(Map.of("b", 2))
                        .updatedAt(Instant.EPOCH)
                        .build();
        assertThat(s.version()).isEqualTo(3);
        assertThat(s.desired()).containsEntry("a", 1);
    }

    @Test
    void toBuilder_roundtrip() {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("a", 1), Instant.now());
        assertThat(s.toBuilder().build()).isEqualTo(s);
    }

    @Test
    void equals_and_hashCode() {
        DeviceShadow a = DeviceShadow.of("d");
        DeviceShadow b = DeviceShadow.of("d");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
