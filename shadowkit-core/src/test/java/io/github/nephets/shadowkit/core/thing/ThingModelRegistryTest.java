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

import io.github.nephets.shadowkit.core.ShadowkitTenant;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThingModelRegistryTest {

    private final ShadowkitTenant tenant = ShadowkitTenant.of("factory-1");
    private final ThingModelRegistry registry = new ThingModelRegistry();

    @Test
    void register_then_lookup() {
        ThingModel model = ThingModel.of(PropertyDefinition.of("power", PropertyType.STRING));
        registry.register(tenant, "dev-1", model);
        assertThat(registry.lookup(tenant, "dev-1")).contains(model);
    }

    @Test
    void lookup_missing_empty() {
        assertThat(registry.lookup(tenant, "nope")).isEmpty();
    }

    @Test
    void overwrite_allowed() {
        ThingModel a = ThingModel.of(PropertyDefinition.of("power", PropertyType.STRING));
        ThingModel b = ThingModel.of(PropertyDefinition.of("temp", PropertyType.NUMBER));
        registry.register(tenant, "dev-1", a);
        registry.register(tenant, "dev-1", b);
        assertThat(registry.lookup(tenant, "dev-1")).contains(b);
    }

    @Test
    void unregister_removes() {
        ThingModel model = ThingModel.of(PropertyDefinition.of("power", PropertyType.STRING));
        registry.register(tenant, "dev-1", model);
        registry.unregister(tenant, "dev-1");
        assertThat(registry.lookup(tenant, "dev-1")).isEmpty();
    }

    @Test
    void null_model_rejected() {
        assertThatThrownBy(() -> registry.register(tenant, "dev-1", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_tenant_or_device_rejected() {
        ThingModel model = ThingModel.of(PropertyDefinition.of("power", PropertyType.STRING));
        assertThatThrownBy(() -> registry.register(null, "dev-1", model))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tenant");
        assertThatThrownBy(() -> registry.register(tenant, null, model))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("deviceId");
        assertThatThrownBy(() -> registry.lookup(null, "dev-1"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void concurrent_register_safe() throws InterruptedException {
        int n = 50;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            final int k = i;
            pool.submit(
                    () -> {
                        try {
                            start.await();
                            registry.register(
                                    tenant,
                                    "dev-" + k,
                                    ThingModel.of(
                                            PropertyDefinition.of("p" + k, PropertyType.STRING)));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            done.countDown();
                        }
                    });
        }
        start.countDown();
        done.await();
        pool.shutdown();
        for (int i = 0; i < n; i++) {
            assertThat(registry.lookup(tenant, "dev-" + i)).isPresent();
        }
    }
}
