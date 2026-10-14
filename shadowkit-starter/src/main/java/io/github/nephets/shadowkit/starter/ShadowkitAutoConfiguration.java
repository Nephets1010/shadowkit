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
import io.github.nephets.shadowkit.core.store.InMemoryShadowStore;
import io.github.nephets.shadowkit.core.store.ShadowStore;
import io.github.nephets.shadowkit.core.thing.ThingModelRegistry;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** shadowkit 自动装配：注册影子存储、引擎、租户过滤器与控制器。 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "shadowkit",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(ShadowkitProperties.class)
public class ShadowkitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ShadowStore shadowStore() {
        return new InMemoryShadowStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThingModelRegistry thingModelRegistry() {
        return new ThingModelRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShadowkitEngine shadowkitEngine(
            ShadowStore shadowStore, ThingModelRegistry thingModelRegistry) {
        return new ShadowkitEngine(shadowStore, thingModelRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShadowkitTenantFilter shadowkitTenantFilter(ShadowkitProperties properties) {
        return new ShadowkitTenantFilter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShadowkitController shadowkitController(
            ShadowkitEngine engine, ThingModelRegistry thingModelRegistry) {
        return new ShadowkitController(engine, thingModelRegistry);
    }
}
