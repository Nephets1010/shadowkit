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

/** 物模型属性的取值类型。 */
public enum PropertyType {
    STRING,
    NUMBER,
    BOOLEAN,
    OBJECT;

    /** 判断给定取值是否符合本类型。null 一律视为不符合。 */
    public boolean accepts(Object value) {
        if (value == null) {
            return false;
        }
        return switch (this) {
            case STRING -> value instanceof String;
            case NUMBER -> value instanceof Number;
            case BOOLEAN -> value instanceof Boolean;
            case OBJECT -> value instanceof java.util.Map;
        };
    }
}
