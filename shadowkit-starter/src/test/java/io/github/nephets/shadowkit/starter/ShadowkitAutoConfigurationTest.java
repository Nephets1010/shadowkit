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
package io.github.nephets.shadowkit.starter;

import io.github.nephets.shadowkit.core.engine.ShadowkitEngine;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;
import io.github.nephets.shadowkit.core.store.ShadowStore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ShadowkitAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(ShadowkitAutoConfiguration.class));

    @Test
    void registers_beans_by_default() {
        runner.run(
                ctx -> {
                    assertThat(ctx).hasSingleBean(ShadowkitEngine.class);
                    assertThat(ctx).hasSingleBean(ShadowStore.class);
                    assertThat(ctx).hasSingleBean(ShadowkitController.class);
                    assertThat(ctx).hasSingleBean(ShadowkitTenantFilter.class);
                });
    }

    @Test
    void disabled_does_not_register() {
        runner.withPropertyValues("shadowkit.enabled=false")
                .run(
                        ctx -> {
                            assertThat(ctx).doesNotHaveBean(ShadowkitEngine.class);
                            assertThat(ctx).doesNotHaveBean(ShadowkitController.class);
                        });
    }

    @Test
    void respects_user_shadow_store() {
        runner.withUserConfiguration(CustomConfig.class)
                .run(
                        ctx -> {
                            assertThat(ctx).hasSingleBean(ShadowStore.class);
                            assertThat(ctx.getBean(ShadowStore.class))
                                    .isInstanceOf(CustomStore.class);
                        });
    }

    static class CustomStore implements ShadowStore {
        @Override
        public Optional<DeviceShadow> find(
                io.github.nephets.shadowkit.core.ShadowkitTenant tenant, String deviceId) {
            return Optional.empty();
        }

        @Override
        public DeviceShadow save(
                io.github.nephets.shadowkit.core.ShadowkitTenant tenant, DeviceShadow shadow) {
            return shadow;
        }

        @Override
        public boolean delete(
                io.github.nephets.shadowkit.core.ShadowkitTenant tenant, String deviceId) {
            return false;
        }

        @Override
        public DeviceShadow getOrCreate(
                io.github.nephets.shadowkit.core.ShadowkitTenant tenant, String deviceId) {
            return DeviceShadow.of(deviceId);
        }
    }

    @Configuration
    static class CustomConfig {
        @Bean
        ShadowStore shadowStore() {
            return new CustomStore();
        }
    }
}
