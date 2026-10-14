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
package io.github.nephets.shadowkit.core;

import java.util.Objects;

/** 租户标识。仅持有不可变的字符串编号，用于多租户影子隔离。 */
public final class ShadowkitTenant {

    private final String id;

    private ShadowkitTenant(String id) {
        this.id = id;
    }

    /** 用编号构造租户；编号为空或仅空白时抛异常。 */
    public static ShadowkitTenant of(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("tenant id 不能为空");
        }
        return new ShadowkitTenant(id);
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShadowkitTenant)) {
            return false;
        }
        return id.equals(((ShadowkitTenant) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ShadowkitTenant{" + id + "}";
    }
}
