/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.wefe.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 流程的统计状态，相比 ProjectFlowStatus 有所收敛。
 *
 * @author zane.luo
 */
public enum ProjectFlowStatisticsStatus {

    /**
     * 编辑中
     */
    editing,
    running,
    success,
    /**
     * 中断（暂停、异常）
     */
    interrupted;

    public List<ProjectFlowStatus> toList() {
        switch (this) {
            case editing:
                return Arrays.asList(ProjectFlowStatus.editing);

            case success:
                return Arrays.asList(ProjectFlowStatus.success);

            case running:
                return Arrays.asList(
                        ProjectFlowStatus.running,
                        ProjectFlowStatus.wait_run,
                        ProjectFlowStatus.wait_stop,
                        ProjectFlowStatus.wait_success
                );

            case interrupted:
                return Arrays.asList(
                        ProjectFlowStatus.stop_on_running,
                        ProjectFlowStatus.error_on_running
                );

            default:
                throw new RuntimeException("意料之外的枚举：" + this);
        }
    }

    public static ProjectFlowStatisticsStatus get(ProjectFlowStatus status) {
        switch (status) {
            case editing:
                return editing;

            case running:
            case wait_run:
            case wait_stop:
            case wait_success:
                return running;

            case success:
                return success;

            case stop_on_running:
            case error_on_running:
                return interrupted;

            default:
                throw new RuntimeException("意料之外的枚举：" + status);
        }
    }
}
