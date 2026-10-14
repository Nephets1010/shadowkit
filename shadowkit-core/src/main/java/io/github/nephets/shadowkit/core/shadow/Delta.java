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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** 设备影子差异：desired 中尚未被 reported 覆盖的属性集合。不可变。 */
public final class Delta {

    private final String deviceId;
    private final long version;
    private final Map<String, Object> state;

    private Delta(String deviceId, long version, Map<String, Object> state) {
        this.deviceId = deviceId;
        this.version = version;
        this.state = Collections.unmodifiableMap(new LinkedHashMap<>(state));
    }

    /** 用设备号、版本号与差异状态构造；deviceId 或 state 为 null 时抛异常。 */
    public static Delta of(String deviceId, long version, Map<String, Object> state) {
        Objects.requireNonNull(deviceId, "deviceId");
        Objects.requireNonNull(state, "state");
        return new Delta(deviceId, version, state);
    }

    public String deviceId() {
        return deviceId;
    }

    public long version() {
        return version;
    }

    /** 差异状态（不可变）。 */
    public Map<String, Object> state() {
        return state;
    }

    /** 差异是否为空（desired 已全部被 reported 覆盖）。 */
    public boolean isEmpty() {
        return state.isEmpty();
    }

    /** 差异是否包含某个属性。 */
    public boolean containsKey(String name) {
        return state.containsKey(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Delta)) {
            return false;
        }
        Delta delta = (Delta) o;
        return version == delta.version
                && deviceId.equals(delta.deviceId)
                && state.equals(delta.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, version, state);
    }

    @Override
    public String toString() {
        return "Delta{deviceId=" + deviceId + ", version=" + version + ", state=" + state + "}";
    }
}
