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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeltaTest {

    @Test
    void of_ok() {
        Delta d = Delta.of("d", 2, Map.of("p", "on"));
        assertThat(d.deviceId()).isEqualTo("d");
        assertThat(d.version()).isEqualTo(2);
    }

    @Test
    void null_deviceId_throws() {
        assertThatThrownBy(() -> Delta.of(null, 1, Map.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void null_state_throws() {
        assertThatThrownBy(() -> Delta.of("d", 1, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void isEmpty_true() {
        assertThat(Delta.of("d", 1, Map.of()).isEmpty()).isTrue();
    }

    @Test
    void isEmpty_false() {
        assertThat(Delta.of("d", 1, Map.of("a", 1)).isEmpty()).isFalse();
    }

    @Test
    void containsKey() {
        assertThat(Delta.of("d", 1, Map.of("a", 1)).containsKey("a")).isTrue();
    }

    @Test
    void immutable_state() {
        Delta d = Delta.of("d", 1, Map.of("a", 1));
        assertThat(d.state()).doesNotContainKey("z");
    }

    @Test
    void equals() {
        assertThat(Delta.of("d", 1, Map.of("a", 1))).isEqualTo(Delta.of("d", 1, Map.of("a", 1)));
    }
}
