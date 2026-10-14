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
package io.github.nephets.shadowkit.core.store;

import io.github.nephets.shadowkit.core.ShadowkitTenant;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** 基于内存的影子存储，仅适合单进程演示与测试。容量有上限，超限按 LRU（最近最少使用）淘汰最旧条目。 */
public class InMemoryShadowStore implements ShadowStore {

    /** 默认容量上限：一百万条设备影子。 */
    public static final int DEFAULT_MAX_CAPACITY = 1_000_000;

    private final int maxCapacity;
    private final Object lock = new Object();
    // accessOrder=true：每次 get/put 把条目移动到队尾，队首即最近最少使用的条目。
    private final LinkedHashMap<String, DeviceShadow> map;

    public InMemoryShadowStore() {
        this(DEFAULT_MAX_CAPACITY);
    }

    /** 自定义容量上限的构造器。容量必须为正，否则抛 IllegalArgumentException。 */
    public InMemoryShadowStore(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity 必须为正数: " + maxCapacity);
        }
        this.maxCapacity = maxCapacity;
        this.map =
                new LinkedHashMap<String, DeviceShadow>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, DeviceShadow> eldest) {
                        return size() > InMemoryShadowStore.this.maxCapacity;
                    }
                };
    }

    private static String key(ShadowkitTenant tenant, String deviceId) {
        return tenant.id() + ":" + deviceId;
    }

    @Override
    public Optional<DeviceShadow> find(ShadowkitTenant tenant, String deviceId) {
        synchronized (lock) {
            return Optional.ofNullable(map.get(key(tenant, deviceId)));
        }
    }

    @Override
    public DeviceShadow save(ShadowkitTenant tenant, DeviceShadow shadow) {
        synchronized (lock) {
            map.put(key(tenant, shadow.deviceId()), shadow);
            return shadow;
        }
    }

    @Override
    public boolean delete(ShadowkitTenant tenant, String deviceId) {
        synchronized (lock) {
            return map.remove(key(tenant, deviceId)) != null;
        }
    }

    @Override
    public DeviceShadow getOrCreate(ShadowkitTenant tenant, String deviceId) {
        String k = key(tenant, deviceId);
        synchronized (lock) {
            DeviceShadow existing = map.get(k);
            if (existing != null) {
                return existing;
            }
            DeviceShadow created = DeviceShadow.of(deviceId);
            map.put(k, created);
            return created;
        }
    }
}
