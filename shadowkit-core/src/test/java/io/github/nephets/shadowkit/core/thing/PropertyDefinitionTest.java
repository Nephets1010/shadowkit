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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyDefinitionTest {

    @Test
    void of_ok() {
        PropertyDefinition d = PropertyDefinition.of("temp", PropertyType.NUMBER);
        assertThat(d.name()).isEqualTo("temp");
        assertThat(d.type()).isEqualTo(PropertyType.NUMBER);
    }

    @Test
    void null_name_throws() {
        assertThatThrownBy(() -> PropertyDefinition.of(null, PropertyType.STRING))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blank_name_throws() {
        assertThatThrownBy(() -> PropertyDefinition.of(" ", PropertyType.STRING))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_type_throws() {
        assertThatThrownBy(() -> PropertyDefinition.of("a", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_same() {
        assertThat(PropertyDefinition.of("a", PropertyType.STRING))
                .isEqualTo(PropertyDefinition.of("a", PropertyType.STRING));
    }

    @Test
    void not_equals_diff_type() {
        assertThat(PropertyDefinition.of("a", PropertyType.STRING))
                .isNotEqualTo(PropertyDefinition.of("a", PropertyType.NUMBER));
    }

    @Test
    void toString_has_name() {
        assertThat(PropertyDefinition.of("a", PropertyType.STRING).toString()).contains("a");
    }
}
