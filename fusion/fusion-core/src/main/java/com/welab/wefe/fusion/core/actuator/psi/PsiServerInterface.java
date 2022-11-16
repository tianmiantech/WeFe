/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.fusion.core.actuator.psi;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;

import java.util.List;

/**
 * @author hunter.zhao
 */
public interface PsiServerInterface {
    PsiActuatorMeta getActuatorParam();

    byte[][] dataTransform(List<String> bsList);

    /**
     * Receive fusion results
     * @param rs
     */
    void receiveResult(List<String> rs);

    /**
     * Alignment data into the library implementation method
     *
     * @param fruit
     */
    void dump(List<JObject> fruit);
}
