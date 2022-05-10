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

package com.welab.wefe.board.service.service.globalconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.welab.wefe.board.service.api.global_config.GlobalConfigUpdateApi;
import com.welab.wefe.board.service.dto.globalconfig.GatewayConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.GlobalConfigFlag;
import com.welab.wefe.board.service.dto.globalconfig.base.ConfigModel;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.IpAddressUtil;
import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Zane
 */
@Service
public class GlobalConfigService extends BaseGlobalConfigService {
    @Autowired
    private GatewayService gatewayService;


    public void update(GlobalConfigUpdateApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.ILLEGAL_REQUEST.throwException("只有管理员才能执行此操作。");
        }

        for (Map.Entry<String, Map<String, String>> group : input.groups.entrySet()) {
            String groupName = group.getKey();
            Map<String, String> groupItems = group.getValue();
            for (Map.Entry<String, String> item : groupItems.entrySet()) {
                String key = item.getKey();
                String value = item.getValue();
                put(groupName, key, value, null);
            }
        }

        // Notify the gateway to update the system configuration cache
        gatewayService.refreshSystemConfigCache();
    }


    /**
     * Add the ip of the board service to the gateway whitelist
     *
     * @param fuzzy Whether it is a fuzzy match, if it is,
     *              the last paragraph of the four paragraphs of ip will be changed to *.
     */
    public synchronized void appendIpToWhiteList(String ip, String comment, boolean fuzzy) throws StatusCodeWithException {

        if (fuzzy) {
            String[] array = ip.split("\\.");
            array[array.length - 1] = "*";
            ip = StringUtil.join(array, ".");
        }

        GatewayConfigModel gatewayConfig = getModel(GatewayConfigModel.class);
        List<String> list = IpAddressUtil.parseStringToIpList(gatewayConfig.ipWhiteList);

        // Already exist, do not add repeatedly.
        if (list.contains(ip)) {
            return;
        }

        gatewayConfig.ipWhiteList = gatewayConfig.ipWhiteList
                + System.lineSeparator()
                + "# " + comment
                + System.lineSeparator()
                + ip
                + System.lineSeparator();

        put(gatewayConfig);
    }


    /**
     * 初始化配置项
     */
    public synchronized void init() throws StatusCodeWithException, InstantiationException, IllegalAccessException {
        LOG.info("start init global config");

        // 反射获取所有 ConfigModel
        List<Class<?>> classes = ReflectionsUtil.getClassesWithAnnotation(
                GlobalConfigFlag.class.getPackage().getName(),
                ConfigModel.class
        );

        // 遍历所有 ConfigModel，将配置项添加到数据库。
        for (Class<?> aClass : classes) {
            SerializeConfig config = new SerializeConfig();
            config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
            String jsonString = JSON.toJSONString(aClass.newInstance(), config, SerializerFeature.WriteMapNullValue);

            ConfigModel annotation = aClass.getAnnotation(ConfigModel.class);

            JSONObject json = JSON.parseObject(jsonString);
            for (String name : json.keySet()) {
                // 当数据库中没有该配置项时，添加该配置项。
                if (findOne(annotation.group(), name) == null) {
                    put(annotation.group(), name, json.getString(name), null);
                }
            }
        }

        LOG.info("init global config success!");
    }


}
