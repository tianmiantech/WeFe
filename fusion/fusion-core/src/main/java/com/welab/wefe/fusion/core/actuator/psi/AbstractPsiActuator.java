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

import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.fusion.core.actuator.AbstractActuator;
import com.welab.wefe.fusion.core.enums.PSIActuatorStatus;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author hunter.zhao
 */
public abstract class AbstractPsiActuator extends AbstractActuator {
    protected BloomFilters bf;

    public AbstractPsiActuator(String businessId) {
        super(businessId);
    }

    public volatile PSIActuatorStatus status = PSIActuatorStatus.uninitialized;

    @Override
    public boolean isFinish() {
        return PSIActuatorStatus.running != status
                && PSIActuatorStatus.uninitialized != status;
    }

    public static void main(String[] args) throws IOException {
        for (int i = 90001; i < 110000; i++) {
            FileUtil.writeTextToFile(i + "," + i + "," + System.currentTimeMillis()+System.lineSeparator(), Paths.get("/Users/hunter.zhao/Documents/1w-手机号.csv"), true);
        }
    }
}
