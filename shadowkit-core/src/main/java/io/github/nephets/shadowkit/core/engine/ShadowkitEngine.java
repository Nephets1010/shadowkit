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
import io.github.nephets.shadowkit.core.exception.ShadowkitValidationException;
import io.github.nephets.shadowkit.core.shadow.Delta;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;
import io.github.nephets.shadowkit.core.store.ShadowStore;
import io.github.nephets.shadowkit.core.thing.ThingModelRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** 影子引擎：把存储、版本乐观锁与差异计算串起来。 */
public final class ShadowkitEngine {

    private static final Logger log = LoggerFactory.getLogger(ShadowkitEngine.class);

    private final ShadowStore store;
    private ThingModelRegistry modelRegistry = null;

    public ShadowkitEngine(ShadowStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /** 带可选物模型注册表的构造器；modelRegistry 为 null 时不校验（向后兼容）。 */
    public ShadowkitEngine(ShadowStore store, ThingModelRegistry modelRegistry) {
        this.store = Objects.requireNonNull(store, "store");
        this.modelRegistry = modelRegistry;
    }

    /** 可选：注入/替换物模型注册表；传 null 表示关闭校验。 */
    public void setModelRegistry(ThingModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    // 同一 (tenant,deviceId) 的影子在并发合入时必须串行，否则「读版本→校验 expectedVersion→
    // 生成新副本→save」不是原子的：两个并发且 expectedVersion 相同的请求会都通过校验、后者覆盖
    // 前者，造成丢失更新、版本号被跳过（乐观锁失效）。per-key 锁把临界区串行化；不同设备之间仍并发。
    private final ConcurrentHashMap<String, Object> deviceLocks = new ConcurrentHashMap<>();

    private static String deviceLockKey(ShadowkitTenant tenant, String deviceId) {
        return tenant.id() + ":" + deviceId;
    }

    private <T> T withDevice(ShadowkitTenant tenant, String deviceId, Supplier<T> action) {
        Object lock =
                deviceLocks.computeIfAbsent(deviceLockKey(tenant, deviceId), k -> new Object());
        synchronized (lock) {
            return action.get();
        }
    }

    /** 查询设备影子；不存在返回空。 */
    public Optional<DeviceShadow> getShadow(ShadowkitTenant tenant, String deviceId) {
        return store.find(tenant, deviceId);
    }

    /** 合入期望状态：期望版本与当前版本不一致抛 ShadowVersionConflictException。 */
    public DeviceShadow applyDesired(
            ShadowkitTenant tenant,
            String deviceId,
            Map<String, Object> desired,
            long expectedVersion,
            Instant now) {
        log.info(
                "applyDesired tenant={} deviceId={} expectedVersion={}",
                tenant.id(),
                deviceId,
                expectedVersion);
        return withDevice(
                tenant,
                deviceId,
                () -> {
                    DeviceShadow current = store.getOrCreate(tenant, deviceId);
                    if (current.version() != expectedVersion) {
                        throw new ShadowVersionConflictException(
                                "影子版本冲突：deviceId="
                                        + deviceId
                                        + " 当前版本="
                                        + current.version()
                                        + " 期望版本="
                                        + expectedVersion);
                    }
                    if (modelRegistry != null) {
                        modelRegistry
                                .lookup(tenant, deviceId)
                                .ifPresent(
                                        m -> {
                                            try {
                                                m.validateState(desired);
                                            } catch (IllegalArgumentException ex) {
                                                throw new ShadowkitValidationException(
                                                        ex.getMessage(), ex);
                                            }
                                        });
                    }
                    DeviceShadow next = current.applyDesired(desired, now);
                    return store.save(tenant, next);
                });
    }

    /** 合入上报状态：期望版本与当前版本不一致抛 ShadowVersionConflictException。 */
    public DeviceShadow applyReported(
            ShadowkitTenant tenant,
            String deviceId,
            Map<String, Object> reported,
            long expectedVersion,
            Instant now) {
        log.info(
                "applyReported tenant={} deviceId={} expectedVersion={}",
                tenant.id(),
                deviceId,
                expectedVersion);
        return withDevice(
                tenant,
                deviceId,
                () -> {
                    DeviceShadow current = store.getOrCreate(tenant, deviceId);
                    if (current.version() != expectedVersion) {
                        throw new ShadowVersionConflictException(
                                "影子版本冲突：deviceId="
                                        + deviceId
                                        + " 当前版本="
                                        + current.version()
                                        + " 期望版本="
                                        + expectedVersion);
                    }
                    if (modelRegistry != null) {
                        modelRegistry
                                .lookup(tenant, deviceId)
                                .ifPresent(
                                        m -> {
                                            try {
                                                m.validateState(reported);
                                            } catch (IllegalArgumentException ex) {
                                                throw new ShadowkitValidationException(
                                                        ex.getMessage(), ex);
                                            }
                                        });
                    }
                    DeviceShadow next = current.applyReported(reported, now);
                    return store.save(tenant, next);
                });
    }

    /** 查询当前差异；影子不存在返回空。 */
    public Optional<Delta> getDelta(ShadowkitTenant tenant, String deviceId) {
        return store.find(tenant, deviceId).map(DeviceShadow::delta);
    }

    /** 删除设备影子；存在并删除成功返回 true。 */
    public boolean delete(ShadowkitTenant tenant, String deviceId) {
        // 与 applyDesired/applyReported 串行，避免删除与合入交叉导致刚删除的影子被重新写入。
        return withDevice(tenant, deviceId, () -> store.delete(tenant, deviceId));
    }
}
