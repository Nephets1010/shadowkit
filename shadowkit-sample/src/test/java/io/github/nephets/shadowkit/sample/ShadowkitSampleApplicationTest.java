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
package io.github.nephets.shadowkit.sample;

import io.github.nephets.shadowkit.core.engine.ShadowkitEngine;
import io.github.nephets.shadowkit.core.store.ShadowStore;
import io.github.nephets.shadowkit.starter.ShadowkitController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShadowkitSampleApplicationTest {

    @Autowired ApplicationContext ctx;

    @Test
    void engine_bean_present() {
        assertThat(ctx.getBean(ShadowkitEngine.class)).isNotNull();
    }

    @Test
    void controller_bean_present() {
        assertThat(ctx.getBean(ShadowkitController.class)).isNotNull();
    }

    @Test
    void shadowStore_bean_present() {
        assertThat(ctx.getBean(ShadowStore.class)).isNotNull();
    }
}
