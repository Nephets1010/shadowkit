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

import java.util.Objects;

/** 物模型中的单个属性定义：名称 + 类型。不可变。 */
public final class PropertyDefinition {

    private final String name;
    private final PropertyType type;

    private PropertyDefinition(String name, PropertyType type) {
        this.name = name;
        this.type = type;
    }

    /** 用名称与类型构造属性定义；名称为空/空白或类型为 null 时抛异常。 */
    public static PropertyDefinition of(String name, PropertyType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("属性名不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("属性类型不能为空");
        }
        return new PropertyDefinition(name, type);
    }

    public String name() {
        return name;
    }

    public PropertyType type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyDefinition)) {
            return false;
        }
        PropertyDefinition that = (PropertyDefinition) o;
        return name.equals(that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "PropertyDefinition{" + name + "=" + type + "}";
    }
}
