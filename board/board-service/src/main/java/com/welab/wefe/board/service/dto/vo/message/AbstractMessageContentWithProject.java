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
package com.welab.wefe.board.service.dto.vo.message;

import com.welab.wefe.board.service.service.CacheObjects;

/**
 * 与项目有关的 MessageContent
 *
 * @author zane
 * @date 2022/6/7
 */
public abstract class AbstractMessageContentWithProject extends AbstractMessageContent {
    public String fromMemberId;
    public String projectId;
    public String projectName;

    public String getFromMemberName() {
        return CacheObjects.getMemberName(fromMemberId);
    }

    @Override
    public String getRelatedId1() {
        return projectId;
    }
}
