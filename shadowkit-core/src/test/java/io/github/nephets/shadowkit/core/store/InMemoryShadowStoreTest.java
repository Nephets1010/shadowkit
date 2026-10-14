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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryShadowStoreTest {

    private final ShadowkitTenant tenant = ShadowkitTenant.of("t");
    private final InMemoryShadowStore store = new InMemoryShadowStore();

    private DeviceShadow base() {
        return DeviceShadow.of("d");
    }

    @Test
    void save_then_find() {
        DeviceShadow s = store.save(tenant, base().applyDesired(Map.of("p", "on"), Instant.now()));
        assertThat(store.find(tenant, "d")).contains(s);
    }

    @Test
    void find_absent_empty() {
        assertThat(store.find(tenant, "nope")).isEmpty();
    }

    @Test
    void getOrCreate_new() {
        DeviceShadow s = store.getOrCreate(tenant, "d");
        assertThat(s.version()).isEqualTo(1);
    }

    @Test
    void getOrCreate_existing_returns_same() {
        DeviceShadow s = store.getOrCreate(tenant, "d");
        DeviceShadow s2 = store.getOrCreate(tenant, "d");
        assertThat(s2).isSameAs(s);
    }

    @Test
    void save_overwrites() {
        store.save(tenant, base());
        DeviceShadow s2 = store.save(tenant, base().applyDesired(Map.of("p", "x"), Instant.now()));
        assertThat(store.find(tenant, "d")).contains(s2);
    }

    @Test
    void delete_existing_true() {
        store.save(tenant, base());
        assertThat(store.delete(tenant, "d")).isTrue();
    }

    @Test
    void delete_absent_false() {
        assertThat(store.delete(tenant, "missing")).isFalse();
    }

    @Test
    void delete_then_find_empty() {
        store.save(tenant, base());
        store.delete(tenant, "d");
        assertThat(store.find(tenant, "d")).isEmpty();
    }

    @Test
    void overflow_evicts_oldest_lru() {
        int capacity = 10;
        InMemoryShadowStore bounded = new InMemoryShadowStore(capacity);
        ShadowkitTenant t = ShadowkitTenant.of("evictTenant");
        int extra = 5;
        for (int i = 0; i < capacity + extra; i++) {
            bounded.save(t, DeviceShadow.of("dev" + i));
        }
        // 最旧的一批（先插入的 extra 条）应被淘汰
        for (int i = 0; i < extra; i++) {
            assertThat(bounded.find(t, "dev" + i)).isEmpty();
        }
        // 最近的 capacity 条应保留
        for (int i = extra; i < capacity + extra; i++) {
            assertThat(bounded.find(t, "dev" + i)).isPresent();
        }
    }

    @Test
    void nonPositiveCapacity_rejected() {
        assertThatThrownBy(() -> new InMemoryShadowStore(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InMemoryShadowStore(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
