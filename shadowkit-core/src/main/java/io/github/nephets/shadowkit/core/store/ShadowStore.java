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

import java.util.Optional;

/** 设备影子存储接口。按租户与设备号读写。 */
public interface ShadowStore {

    /** 查询某个设备的影子；不存在返回空。 */
    Optional<DeviceShadow> find(ShadowkitTenant tenant, String deviceId);

    /** 保存（覆盖）某个设备的影子并返回被保存的实例。 */
    DeviceShadow save(ShadowkitTenant tenant, DeviceShadow shadow);

    /** 删除某个设备的影子；存在并删除成功返回 true。 */
    boolean delete(ShadowkitTenant tenant, String deviceId);

    /** 获取或创建：不存在则按 deviceId 生成空影子并保存后返回。 */
    DeviceShadow getOrCreate(ShadowkitTenant tenant, String deviceId);
}
