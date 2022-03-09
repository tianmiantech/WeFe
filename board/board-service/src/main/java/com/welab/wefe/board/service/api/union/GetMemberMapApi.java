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
package com.welab.wefe.board.service.api.union;

import com.welab.wefe.common.web.api.base.Api;

/**
 * @author zane
 * @date 2021/12/27
 */
@Api(path = "union/member/map", name = "获取全量的成员信息列表")
public class GetMemberMapApi extends AbstractThroughUnionApi {
    @Override
    protected String api() {
        return "member/map";
    }
}
