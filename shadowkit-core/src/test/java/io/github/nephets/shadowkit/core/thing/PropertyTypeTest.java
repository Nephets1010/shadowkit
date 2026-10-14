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
package io.github.nephets.shadowkit.core.thing;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyTypeTest {

    @Test
    void string_accepts() {
        assertThat(PropertyType.STRING.accepts("x")).isTrue();
        assertThat(PropertyType.STRING.accepts(1)).isFalse();
    }

    @Test
    void number_accepts() {
        assertThat(PropertyType.NUMBER.accepts(1)).isTrue();
        assertThat(PropertyType.NUMBER.accepts("1")).isFalse();
    }

    @Test
    void boolean_accepts() {
        assertThat(PropertyType.BOOLEAN.accepts(true)).isTrue();
        assertThat(PropertyType.BOOLEAN.accepts("t")).isFalse();
    }

    @Test
    void object_accepts() {
        assertThat(PropertyType.OBJECT.accepts(Map.of("a", 1))).isTrue();
        assertThat(PropertyType.OBJECT.accepts("s")).isFalse();
    }

    @Test
    void null_rejected() {
        assertThat(PropertyType.STRING.accepts(null)).isFalse();
    }

    @Test
    void enum_values() {
        assertThat(PropertyType.values())
                .containsExactly(
                        PropertyType.STRING,
                        PropertyType.NUMBER,
                        PropertyType.BOOLEAN,
                        PropertyType.OBJECT);
    }
}
