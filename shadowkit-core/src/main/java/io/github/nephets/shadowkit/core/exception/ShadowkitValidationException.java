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
package io.github.nephets.shadowkit.core.exception;

import io.github.nephets.shadowkit.core.ShadowkitException;

/** 物模型校验失败：状态中包含物模型未声明的属性，或属性类型不符。 */
public class ShadowkitValidationException extends ShadowkitException {

    public ShadowkitValidationException(String message) {
        super(message);
    }

    public ShadowkitValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
