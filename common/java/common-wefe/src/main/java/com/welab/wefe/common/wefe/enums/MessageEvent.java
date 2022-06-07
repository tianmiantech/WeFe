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
package com.welab.wefe.common.wefe.enums;

/**
 * @author zane
 * @date 2022/6/6
 */
public enum MessageEvent {
    /**
     * 创建项目
     */
    CreateProject,
    /**
     * 同意加入项目
     */
    AgreeJoinProject,
    /**
     * 拒绝加入项目
     */
    DisagreeJoinProject,
    /**
     * 申请数据资源
     */
    ApplyDataResource,
    /**
     * 同意数据资源申请
     */
    AgreeApplyDataResource,
    /**
     * 拒绝数据资源申请
     */
    DisagreeApplyDataResource,
    /**
     * Gateway服务错误
     */
    OnGatewayError,
}
