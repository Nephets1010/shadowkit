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
import io.github.nephets.shadowkit.core.shadow.Delta;
import io.github.nephets.shadowkit.core.shadow.DeviceShadow;
import io.github.nephets.shadowkit.core.thing.ThingModelRegistry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShadowkitController.class)
class ShadowkitControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ShadowkitEngine engine;
    @MockBean ThingModelRegistry registry;

    @BeforeEach
    void setTenant() {
        ShadowkitTenantContext.set(ShadowkitTenant.of("t"));
    }

    @AfterEach
    void clear() {
        ShadowkitTenantContext.clear();
    }

    @Test
    void shadow_ok() throws Exception {
        when(engine.getShadow(any(), any()))
                .thenReturn(
                        Optional.of(
                                DeviceShadow.of("d")
                                        .applyDesired(Map.of("p", "on"), Instant.now())));
        mvc.perform(get("/shadowkit/devices/d/shadow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("d"))
                .andExpect(jsonPath("$.desired.p").value("on"));
    }

    @Test
    void shadow_404() throws Exception {
        when(engine.getShadow(any(), any())).thenReturn(Optional.empty());
        mvc.perform(get("/shadowkit/devices/d/shadow")).andExpect(status().isNotFound());
    }

    @Test
    void desired_ok() throws Exception {
        DeviceShadow s = DeviceShadow.of("d").applyDesired(Map.of("p", "on"), Instant.now());
        when(engine.applyDesired(any(), any(), any(), anyLong(), any())).thenReturn(s);
        mvc.perform(
                        put("/shadowkit/devices/d/desired")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"state\":{\"p\":\"on\"},\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void desired_conflict_409() throws Exception {
        when(engine.applyDesired(any(), any(), any(), anyLong(), any()))
                .thenThrow(new ShadowVersionConflictException("冲突"));
        mvc.perform(
                        put("/shadowkit/devices/d/desired")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"state\":{\"p\":\"on\"},\"version\":9}"))
                .andExpect(status().isConflict());
    }

    @Test
    void reported_ok() throws Exception {
        DeviceShadow s = DeviceShadow.of("d").applyReported(Map.of("p", "on"), Instant.now());
        when(engine.applyReported(any(), any(), any(), anyLong(), any())).thenReturn(s);
        mvc.perform(
                        put("/shadowkit/devices/d/reported")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"state\":{\"p\":\"on\"},\"version\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void delta_ok() throws Exception {
        when(engine.getDelta(any(), any()))
                .thenReturn(Optional.of(Delta.of("d", 2, Map.of("p", "on"))));
        mvc.perform(get("/shadowkit/devices/d/delta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.p").value("on"));
    }

    @Test
    void delta_404() throws Exception {
        when(engine.getDelta(any(), any())).thenReturn(Optional.empty());
        mvc.perform(get("/shadowkit/devices/d/delta")).andExpect(status().isNotFound());
    }

    @Test
    void delete_ok() throws Exception {
        when(engine.delete(any(), any())).thenReturn(true);
        mvc.perform(delete("/shadowkit/devices/d/shadow")).andExpect(status().isOk());
    }

    @Test
    void missing_tenant_400() throws Exception {
        ShadowkitTenantContext.clear();
        mvc.perform(get("/shadowkit/devices/d/shadow")).andExpect(status().isBadRequest());
        ShadowkitTenantContext.set(ShadowkitTenant.of("t"));
    }

    @Test
    void model_register_ok() throws Exception {
        mvc.perform(
                        put("/shadowkit/devices/d/model")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"properties\":[{\"name\":\"power\",\"type\":\"STRING\"}]}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void model_register_bad_type_400() throws Exception {
        mvc.perform(
                        put("/shadowkit/devices/d/model")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"properties\":[{\"name\":\"power\",\"type\":\"NOPE\"}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void model_register_null_properties_400() throws Exception {
        mvc.perform(
                        put("/shadowkit/devices/d/model")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"properties\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void model_register_empty_properties_400() throws Exception {
        mvc.perform(
                        put("/shadowkit/devices/d/model")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"properties\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void desired_validation_400() throws Exception {
        when(engine.applyDesired(any(), any(), any(), anyLong(), any()))
                .thenThrow(new ShadowkitValidationException("属性类型不匹配：power"));
        mvc.perform(
                        put("/shadowkit/devices/d/desired")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"state\":{\"power\":\"on\"},\"version\":1}"))
                .andExpect(status().isBadRequest());
    }
}
