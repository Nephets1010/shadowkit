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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThingModelTest {

    private final PropertyDefinition power = PropertyDefinition.of("power", PropertyType.STRING);
    private final PropertyDefinition temp =
            PropertyDefinition.of("targetTemp", PropertyType.NUMBER);

    @Test
    void of_empty_throws() {
        assertThatThrownBy(() -> ThingModel.of(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_with_defs() {
        ThingModel m = ThingModel.of(power, temp);
        assertThat(m.propertyNames()).containsExactlyInAnyOrder("power", "targetTemp");
    }

    @Test
    void duplicate_name_throws() {
        assertThatThrownBy(() -> ThingModel.of(power, power))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_def_throws() {
        assertThatThrownBy(() -> ThingModel.of((PropertyDefinition) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void typeOf_present() {
        assertThat(ThingModel.of(power, temp).typeOf("power")).contains(PropertyType.STRING);
    }

    @Test
    void typeOf_absent_empty() {
        assertThat(ThingModel.of(power).typeOf("missing")).isEmpty();
    }

    @Test
    void contains() {
        ThingModel m = ThingModel.of(power);
        assertThat(m.contains("power")).isTrue();
        assertThat(m.contains("x")).isFalse();
    }

    @Test
    void validate_ok() {
        ThingModel.of(power, temp).validateState(Map.of("power", "on", "targetTemp", 26));
    }

    @Test
    void validate_unknown_key_throws() {
        assertThatThrownBy(() -> ThingModel.of(power).validateState(Map.of("nope", 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validate_type_mismatch_throws() {
        assertThatThrownBy(() -> ThingModel.of(power).validateState(Map.of("power", 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validate_null_state_ok() {
        ThingModel.of(power).validateState(null);
    }
}
