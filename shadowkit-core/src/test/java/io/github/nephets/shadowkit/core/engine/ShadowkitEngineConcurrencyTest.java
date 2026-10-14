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
package io.github.nephets.shadowkit.core.engine;

import io.github.nephets.shadowkit.core.ShadowVersionConflictException;
import io.github.nephets.shadowkit.core.ShadowkitTenant;
import io.github.nephets.shadowkit.core.store.InMemoryShadowStore;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 并发合入回归测试：用 per-(tenant,deviceId) 锁串行化后，多个并发且 expectedVersion 相同的 applyDesired 只有一个能把版本从 1 推到
 * 2，其余被版本冲突拒绝（乐观锁生效）。 修复前（版本校验与 save 非原子）会有多个请求都成功、丢失更新、版本被跳过。
 */
class ShadowkitEngineConcurrencyTest {

    private final ShadowkitTenant tenant = ShadowkitTenant.of("t");
    private final ShadowkitEngine engine = new ShadowkitEngine(new InMemoryShadowStore());
    private final Instant now = Instant.now();

    @Test
    void concurrentApplyDesiredWithSameVersionExactlyOneWins() throws Exception {
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            int idx = i;
            pool.submit(
                    () -> {
                        try {
                            start.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        try {
                            engine.applyDesired(tenant, "d", Map.of("p" + idx, idx), 1L, now);
                            successes.incrementAndGet();
                        } catch (ShadowVersionConflictException ignored) {
                            // 并发抢占，应被版本冲突拒绝
                        }
                    });
        }
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        // 乐观锁生效：只有一个请求成功把版本推到 2，其余被拒绝。
        assertThat(successes.get()).isEqualTo(1);
        assertThat(engine.getShadow(tenant, "d").get().version()).isEqualTo(2);
    }
}
