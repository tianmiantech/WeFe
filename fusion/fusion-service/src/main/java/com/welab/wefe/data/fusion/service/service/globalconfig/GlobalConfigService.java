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

package com.welab.wefe.data.fusion.service.service.globalconfig;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.data.fusion.service.dto.entity.globalconfig.FusionConfigModel;
import com.welab.wefe.data.fusion.service.dto.entity.globalconfig.MemberInfoModel;
import org.springframework.stereotype.Service;

/**
 * @author Zane
 */
@Service
public class GlobalConfigService extends BaseGlobalConfigService {


//
//    public void update(GlobalConfigUpdateApi.Input input) throws StatusCodeWithException {
//        if (!CurrentAccount.isAdmin()) {
//            StatusCode.ILLEGAL_REQUEST.throwException("只有管理员才能执行此操作。");
//        }
//
//        for (Map.Entry<String, Map<String, String>> group : input.groups.entrySet()) {
//            String groupName = group.getKey();
//            Map<String, String> groupItems = group.getValue();
//            for (Map.Entry<String, String> item : groupItems.entrySet()) {
//                String key = item.getKey();
//                String value = item.getValue();
//                put(groupName, key, value, null);
//            }
//        }
//    }


    /**
     * init global config items
     */
    public void init() throws StatusCodeWithException {
        LOG.info("start init global config");

        FusionConfigModel boardConfig = getFusionConfig();
        if (boardConfig == null) {
            setFusionConfig(new FusionConfigModel());
        }

        LOG.info("init global config success!");
    }

    public void setMemberInfo(MemberInfoModel model) throws StatusCodeWithException {
        put(Group.MEMBER_INFO, model);
    }

    public MemberInfoModel getMemberInfo() {
        return getModel(Group.MEMBER_INFO, MemberInfoModel.class);
    }


    public void setFusionConfig(FusionConfigModel model) throws StatusCodeWithException {
        put(Group.WEFE_FUSION, model);
    }

    public FusionConfigModel getFusionConfig() {
        return getModel(Group.WEFE_FUSION, FusionConfigModel.class);
    }
//
//    public void setAlertConfig(AlertConfigModel model) throws StatusCodeWithException {
//        put(Group.ALERT_CONFIG, model);
//    }
//
//    public AlertConfigModel getAlertConfig() {
//        return getModel(Group.ALERT_CONFIG, AlertConfigModel.class);
//    }


}
