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

import io.github.nephets.shadowkit.core.ShadowVersionConflictException;
import io.github.nephets.shadowkit.core.ShadowkitTenant;
import io.github.nephets.shadowkit.core.engine.ShadowkitEngine;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/** 启动即演示：下发期望、计算差异、上报同步、以及版本冲突保护。 */
@Component
public class ShadowkitDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShadowkitDemo.class);
    private final ShadowkitEngine engine;

    public ShadowkitDemo(ShadowkitEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run(String... args) throws Exception {
        ShadowkitTenant tenant = ShadowkitTenant.of("demo");
        String deviceId = "thermostat-1";

        DeviceShadow s =
                engine.applyDesired(
                        tenant,
                        deviceId,
                        Map.of("power", "on", "targetTemp", 26),
                        1L,
                        Instant.now());
        log.info("下发期望后差异（待设备同步）：{}", s.delta().state());

        engine.applyReported(
                tenant,
                deviceId,
                Map.of("power", "on", "targetTemp", 26),
                s.version(),
                Instant.now());
        var after = engine.getDelta(tenant, deviceId).orElseThrow();
        log.info("设备上报后差异（应为空）：{}", after.state());

        try {
            engine.applyDesired(tenant, deviceId, Map.of("power", "off"), 1L, Instant.now());
        } catch (ShadowVersionConflictException e) {
            log.info("版本冲突被正确拦截：{}", e.getMessage());
        }
    }
}
