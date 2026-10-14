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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** 物模型：设备支持的全部属性定义。不可变，按名称索引。 */
public final class ThingModel {

    private final Map<String, PropertyDefinition> properties;

    private ThingModel(Map<String, PropertyDefinition> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    /** 用若干属性定义构造物模型；至少需一个且名称不得重复。 */
    public static ThingModel of(PropertyDefinition... defs) {
        return of(Arrays.asList(defs));
    }

    /** 用属性定义列表构造物模型；空列表、null 定义或重名均抛异常。 */
    public static ThingModel of(List<PropertyDefinition> defs) {
        if (defs == null || defs.isEmpty()) {
            throw new IllegalArgumentException("物模型至少需要一个属性");
        }
        Map<String, PropertyDefinition> map = new LinkedHashMap<>();
        for (PropertyDefinition d : defs) {
            Objects.requireNonNull(d, "属性定义不能为空");
            if (map.containsKey(d.name())) {
                throw new IllegalArgumentException("属性名重复：" + d.name());
            }
            map.put(d.name(), d);
        }
        return new ThingModel(map);
    }

    /** 全部属性名。 */
    public Set<String> propertyNames() {
        return properties.keySet();
    }

    /** 物模型是否包含某个属性名。 */
    public boolean contains(String name) {
        return properties.containsKey(name);
    }

    /** 查询属性名对应的类型；不存在则返回空。 */
    public Optional<PropertyType> typeOf(String name) {
        return Optional.ofNullable(properties.get(name)).map(PropertyDefinition::type);
    }

    /** 校验一份状态（desired 或 reported）是否全部落在物模型内且类型匹配；不符抛异常。 */
    public void validateState(Map<String, Object> state) {
        if (state == null) {
            return;
        }
        for (Map.Entry<String, Object> e : state.entrySet()) {
            PropertyDefinition def = properties.get(e.getKey());
            if (def == null) {
                throw new IllegalArgumentException("物模型中不存在的属性：" + e.getKey());
            }
            if (!def.type().accepts(e.getValue())) {
                throw new IllegalArgumentException("属性类型不匹配：" + e.getKey());
            }
        }
    }
}
