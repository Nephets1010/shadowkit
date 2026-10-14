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

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** 设备影子：保存设备期望状态 desired 与上报状态 reported，版本号单调递增。不可变，变更返回新副本。 */
public final class DeviceShadow {

    private final String deviceId;
    private final long version;
    private final Map<String, Object> desired;
    private final Map<String, Object> reported;
    private final Instant updatedAt;

    private DeviceShadow(
            String deviceId,
            long version,
            Map<String, Object> desired,
            Map<String, Object> reported,
            Instant updatedAt) {
        this.deviceId = deviceId;
        this.version = version;
        this.desired = Collections.unmodifiableMap(new LinkedHashMap<>(desired));
        this.reported = Collections.unmodifiableMap(new LinkedHashMap<>(reported));
        this.updatedAt = updatedAt;
    }

    /** 空影子：版本 1，desired/reported 均为空，更新时间取 EPOCH。 */
    public static DeviceShadow of(String deviceId) {
        return of(deviceId, 1L, Map.of(), Map.of(), Instant.EPOCH);
    }

    /** 用完整字段构造；deviceId 空、version<1 或任意映射为 null 时抛异常。 */
    public static DeviceShadow of(
            String deviceId,
            long version,
            Map<String, Object> desired,
            Map<String, Object> reported,
            Instant updatedAt) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId 不能为空");
        }
        if (version < 1) {
            throw new IllegalArgumentException("version 必须 >= 1");
        }
        Objects.requireNonNull(desired, "desired");
        Objects.requireNonNull(reported, "reported");
        Objects.requireNonNull(updatedAt, "updatedAt");
        return new DeviceShadow(deviceId, version, desired, reported, updatedAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String deviceId() {
        return deviceId;
    }

    public long version() {
        return version;
    }

    /** 期望状态（不可变）。 */
    public Map<String, Object> desired() {
        return desired;
    }

    /** 上报状态（不可变）。 */
    public Map<String, Object> reported() {
        return reported;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    /** 合入期望补丁并使版本 +1，返回新副本。 */
    public DeviceShadow applyDesired(Map<String, Object> patch, Instant now) {
        return new DeviceShadow(deviceId, version + 1, merge(desired, patch), reported, now);
    }

    /** 合入上报补丁并使版本 +1，返回新副本。 */
    public DeviceShadow applyReported(Map<String, Object> patch, Instant now) {
        return new DeviceShadow(deviceId, version + 1, desired, merge(reported, patch), now);
    }

    /** 计算差异：desired 中值与 reported 不一致的属性集合。 */
    public Delta delta() {
        Map<String, Object> d = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : desired.entrySet()) {
            if (!Objects.equals(reported.get(e.getKey()), e.getValue())) {
                d.put(e.getKey(), e.getValue());
            }
        }
        return Delta.of(deviceId, version, d);
    }

    /** 复制为构造器，便于小幅修改后重建。 */
    public Builder toBuilder() {
        return new Builder()
                .deviceId(deviceId)
                .version(version)
                .desired(desired)
                .reported(reported)
                .updatedAt(updatedAt);
    }

    private static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> patch) {
        Map<String, Object> m = new LinkedHashMap<>(base);
        if (patch != null) {
            m.putAll(patch);
        }
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceShadow)) {
            return false;
        }
        DeviceShadow that = (DeviceShadow) o;
        return version == that.version
                && deviceId.equals(that.deviceId)
                && desired.equals(that.desired)
                && reported.equals(that.reported)
                && updatedAt.equals(that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, version, desired, reported, updatedAt);
    }

    @Override
    public String toString() {
        return "DeviceShadow{deviceId="
                + deviceId
                + ", version="
                + version
                + ", desired="
                + desired
                + ", reported="
                + reported
                + ", updatedAt="
                + updatedAt
                + "}";
    }

    /** 设备影子构造器。 */
    public static final class Builder {
        private String deviceId;
        private long version = 1L;
        private Map<String, Object> desired = Map.of();
        private Map<String, Object> reported = Map.of();
        private Instant updatedAt = Instant.EPOCH;

        public Builder deviceId(String v) {
            this.deviceId = v;
            return this;
        }

        public Builder version(long v) {
            this.version = v;
            return this;
        }

        public Builder desired(Map<String, Object> v) {
            this.desired = v;
            return this;
        }

        public Builder reported(Map<String, Object> v) {
            this.reported = v;
            return this;
        }

        public Builder updatedAt(Instant v) {
            this.updatedAt = v;
            return this;
        }

        public DeviceShadow build() {
            return DeviceShadow.of(deviceId, version, desired, reported, updatedAt);
        }
    }
}
