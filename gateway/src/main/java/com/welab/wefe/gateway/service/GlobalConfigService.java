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

package com.welab.wefe.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.gateway.dto.BoardConfigModel;
import com.welab.wefe.gateway.dto.GatewayConfigModel;
import com.welab.wefe.gateway.dto.MemberInfoModel;
import com.welab.wefe.gateway.entity.GlobalConfigEntity;
import com.welab.wefe.gateway.repository.GlobalConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane
 */
@Service
public class GlobalConfigService extends AbstractService {
    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    protected static class Group {
        public static String MEMBER_INFO = "member_info";
        public static String MAIL_SERVER = "mail_server";
        public static String ALERT_CONFIG = "alert_config";
        public static String WEFE_GATEWAY = "wefe_gateway";
        public static String WEFE_BOARD = "wefe_board";
    }

    public GatewayConfigModel getGatewayConfig() {
        return getModel(Group.WEFE_GATEWAY, GatewayConfigModel.class);
    }

    /**
     * Get board service config
     */
    public BoardConfigModel getBoardConfig() {
        return getModel(Group.WEFE_BOARD, BoardConfigModel.class);
    }

    /**
     * Get member information
     */
    public MemberInfoModel getMemberInfo() {
        return getModel(Group.MEMBER_INFO, MemberInfoModel.class);
    }

    /**
     * Query list by group
     */
    public List<GlobalConfigEntity> list(String group) {
        return globalConfigRepository.findByGroup(group);
    }

    /**
     * Gets the entity corresponding to the specified group
     */
    protected <T> T getModel(String group, Class<T> clazz) {
        List<GlobalConfigEntity> list = list(group);
        return toModel(list, clazz);
    }

    /**
     * Convert configuration item list to entity
     */
    private <T> T toModel(List<GlobalConfigEntity> list, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        JSONObject json = new JSONObject();
        for (GlobalConfigEntity item : list) {
            json.put(item.getName(), item.getValue());
        }
        return json.toJavaObject(clazz);
    }
}
