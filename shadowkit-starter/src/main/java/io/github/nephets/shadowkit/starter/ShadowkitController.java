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

import io.github.nephets.shadowkit.core.ShadowVersionConflictException;
import io.github.nephets.shadowkit.core.ShadowkitTenant;
import io.github.nephets.shadowkit.core.engine.ShadowkitEngine;
import io.github.nephets.shadowkit.core.exception.ShadowkitValidationException;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;
import io.github.nephets.shadowkit.core.thing.PropertyDefinition;
import io.github.nephets.shadowkit.core.thing.PropertyType;
import io.github.nephets.shadowkit.core.thing.ThingModel;
import io.github.nephets.shadowkit.core.thing.ThingModelRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 设备影子的 HTTP 端点。 */
@RestController
@RequestMapping("/shadowkit")
public class ShadowkitController {

    private static final Logger log = LoggerFactory.getLogger(ShadowkitController.class);

    private final ShadowkitEngine engine;
    private final ThingModelRegistry registry;

    public ShadowkitController(ShadowkitEngine engine, ThingModelRegistry registry) {
        this.engine = engine;
        this.registry = registry;
    }

    private ShadowkitTenant tenant() {
        ShadowkitTenant t = ShadowkitTenantContext.get();
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少租户头");
        }
        return t;
    }

    @GetMapping("/devices/{id}/shadow")
    public ShadowResponse shadow(@PathVariable String id) {
        ShadowkitTenant t = tenant();
        log.info("shadow request tenant={} deviceId={}", t.id(), id);
        DeviceShadow s =
                engine.getShadow(t, id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "影子不存在：" + id));
        return toResponse(s);
    }

    @PutMapping("/devices/{id}/desired")
    public ShadowResponse desired(@PathVariable String id, @RequestBody DesiredRequest req) {
        ShadowkitTenant t = tenant();
        log.info("desired request tenant={} deviceId={} version={}", t.id(), id, req.version());
        try {
            DeviceShadow s = engine.applyDesired(t, id, req.state(), req.version(), Instant.now());
            return toResponse(s);
        } catch (ShadowVersionConflictException e) {
            log.error(
                    "desired conflict tenant={} deviceId={} version={}",
                    t.id(),
                    id,
                    req.version(),
                    e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (ShadowkitValidationException e) {
            log.warn(
                    "desired 物模型校验被拒 tenant={} deviceId={} version={}: {}",
                    t.id(),
                    id,
                    req.version(),
                    e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/devices/{id}/reported")
    public ShadowResponse reported(@PathVariable String id, @RequestBody ReportedRequest req) {
        ShadowkitTenant t = tenant();
        log.info("reported request tenant={} deviceId={} version={}", t.id(), id, req.version());
        try {
            DeviceShadow s = engine.applyReported(t, id, req.state(), req.version(), Instant.now());
            return toResponse(s);
        } catch (ShadowVersionConflictException e) {
            log.error(
                    "reported conflict tenant={} deviceId={} version={}",
                    t.id(),
                    id,
                    req.version(),
                    e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (ShadowkitValidationException e) {
            log.warn(
                    "reported 物模型校验被拒 tenant={} deviceId={} version={}: {}",
                    t.id(),
                    id,
                    req.version(),
                    e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/devices/{id}/delta")
    public DeltaResponse delta(@PathVariable String id) {
        ShadowkitTenant t = tenant();
        log.info("delta request tenant={} deviceId={}", t.id(), id);
        var d =
                engine.getDelta(t, id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "影子不存在：" + id));
        return new DeltaResponse(d.deviceId(), d.version(), d.state());
    }

    @DeleteMapping("/devices/{id}/shadow")
    public Map<String, Object> delete(@PathVariable String id) {
        ShadowkitTenant t = tenant();
        log.info("delete request tenant={} deviceId={}", t.id(), id);
        boolean removed = engine.delete(t, id);
        return Map.of("deleted", removed);
    }

    @PutMapping("/devices/{id}/model")
    public ResponseEntity<Void> registerModel(
            @PathVariable String id, @RequestBody ModelRegistrationRequest req) {
        ShadowkitTenant t = tenant();
        if (req == null || req.properties() == null || req.properties().isEmpty()) {
            // 缺 properties 时下游会裸 NPE/IAE，收敛成明确的 400
            log.warn("register model 缺少 properties：tenant={}, deviceId={}", t.id(), id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "properties 不能为空");
        }
        log.info(
                "register model tenant={} deviceId={} props={}",
                t.id(),
                id,
                req.properties().size());
        try {
            ThingModel model =
                    ThingModel.of(
                            req.properties().stream()
                                    .map(
                                            p ->
                                                    PropertyDefinition.of(
                                                            p.name(),
                                                            PropertyType.valueOf(p.type())))
                                    .collect(Collectors.toList()));
            registry.register(t, id, model);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // 含 PropertyType.valueOf 的非法枚举名与 PropertyDefinition.of 的空名/空类型
            log.warn("register model 参数非法 tenant={} deviceId={}: {}", t.id(), id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** 注册物模型的请求体：属性名 + 类型枚举名（STRING/NUMBER/BOOLEAN/OBJECT）列表。 */
    public record ModelRegistrationRequest(List<PropertySpec> properties) {}

    /** 单个属性定义：name 为属性名，type 为 PropertyType 枚举名。 */
    public record PropertySpec(String name, String type) {}

    private ShadowResponse toResponse(DeviceShadow s) {
        return new ShadowResponse(
                s.deviceId(),
                s.version(),
                s.desired(),
                s.reported(),
                s.delta().state(),
                s.updatedAt().toString());
    }
}
