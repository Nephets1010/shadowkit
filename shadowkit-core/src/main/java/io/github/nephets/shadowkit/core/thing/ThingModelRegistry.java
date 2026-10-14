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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 物模型注册表：按 (租户, 设备号) 维护可选的 {@link ThingModel}。缺失即视为该设备无物模型（不做校验）。 */
public class ThingModelRegistry {

    private static final Logger log = LoggerFactory.getLogger(ThingModelRegistry.class);

    private final Map<String, ThingModel> models = new ConcurrentHashMap<>();

    private static String key(ShadowkitTenant tenant, String deviceId) {
        // 显式校验，避免 tenant.id() 抛裸 NPE 时看不出是哪个参数为空
        Objects.requireNonNull(tenant, "tenant 不能为空");
        Objects.requireNonNull(deviceId, "deviceId 不能为空");
        return tenant.id() + ":" + deviceId;
    }

    /** 注册/覆盖某设备的物模型；model 为 null 抛 IllegalArgumentException。 */
    public void register(ShadowkitTenant tenant, String deviceId, ThingModel model) {
        if (model == null) {
            throw new IllegalArgumentException("ThingModel 不能为空");
        }
        models.put(key(tenant, deviceId), model);
        // 注册/注销是物模型体系的全部状态迁移，留 debug 级痕迹便于排查「校验为何没生效」
        if (log.isDebugEnabled()) {
            log.debug(
                    "物模型已注册：tenant={}, deviceId={}, 属性数={}",
                    tenant.id(),
                    deviceId,
                    model.propertyNames().size());
        }
    }

    /** 注销某设备的物模型；未注册时静默忽略。 */
    public void unregister(ShadowkitTenant tenant, String deviceId) {
        ThingModel removed = models.remove(key(tenant, deviceId));
        if (removed != null && log.isDebugEnabled()) {
            log.debug("物模型已注销：tenant={}, deviceId={}", tenant.id(), deviceId);
        }
    }

    /** 查询某设备的物模型；未注册返回空。 */
    public Optional<ThingModel> lookup(ShadowkitTenant tenant, String deviceId) {
        return Optional.ofNullable(models.get(key(tenant, deviceId)));
    }
}
