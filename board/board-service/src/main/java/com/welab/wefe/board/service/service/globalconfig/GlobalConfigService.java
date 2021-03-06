/**
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

package com.welab.wefe.board.service.service.globalconfig;

import com.welab.wefe.board.service.api.global_config.GlobalConfigUpdateApi;
import com.welab.wefe.board.service.dto.globalconfig.*;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.IpAddressUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

        input.groups.forEach((groupName, groupItems) ->
                groupItems.forEach((key, value) -> {
                    try {
                        put(groupName, key, value, null);
                    } catch (StatusCodeWithException e) {
                        e.printStackTrace();
                    }
                })
        );

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

        GatewayConfigModel gatewayConfig = getGatewayConfig();
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

        setGatewayConfig(gatewayConfig);
    }


    /**
     * init global config items
     */
    public void init() throws StatusCodeWithException {
        LOG.info("start init global config");
        GatewayConfigModel gatewayConfig = getGatewayConfig();
        if (gatewayConfig == null) {
            setGatewayConfig(new GatewayConfigModel());
        }

        MailServerModel mailServer = getMailServer();
        if (mailServer == null) {
            setMailServer(new MailServerModel());
        }

        BoardConfigModel boardConfig = getBoardConfig();
        if (boardConfig == null) {
            setBoardConfig(new BoardConfigModel());
        }

        AlertConfigModel alertConfig = getAlertConfig();
        if (alertConfig == null) {
            setAlertConfig(new AlertConfigModel());
        }

        FlowConfigModel flowConfig = getFlowConfig();
        if (flowConfig == null) {
            setFlowConfig(new FlowConfigModel());
        }

        ServingConfigModel servingConfig = getServingConfig();
        if (servingConfig == null) {
            setServingConfig(new ServingConfigModel());
        }

        LOG.info("init global config success!");
    }


    public GatewayConfigModel getGatewayConfig() {
        return getModel(Group.WEFE_GATEWAY, GatewayConfigModel.class);
    }

    public void setGatewayConfig(GatewayConfigModel model) throws StatusCodeWithException {
        put(Group.WEFE_GATEWAY, model);
    }

    public void setMemberInfo(MemberInfoModel model) throws StatusCodeWithException {
        put(Group.MEMBER_INFO, model);
    }

    public MemberInfoModel getMemberInfo() {
        return getModel(Group.MEMBER_INFO, MemberInfoModel.class);
    }

    public void setMailServer(MailServerModel model) throws StatusCodeWithException {
        put(Group.MAIL_SERVER, model);
    }

    public MailServerModel getMailServer() {
        return getModel(Group.MAIL_SERVER, MailServerModel.class);
    }

    public void setBoardConfig(BoardConfigModel model) throws StatusCodeWithException {
        put(Group.WEFE_BOARD, model);
    }

    public BoardConfigModel getBoardConfig() {
        return getModel(Group.WEFE_BOARD, BoardConfigModel.class);
    }

    public void setAlertConfig(AlertConfigModel model) throws StatusCodeWithException {
        put(Group.ALERT_CONFIG, model);
    }

    public AlertConfigModel getAlertConfig() {
        return getModel(Group.ALERT_CONFIG, AlertConfigModel.class);
    }


    public void setFlowConfig(FlowConfigModel model) throws StatusCodeWithException {
        put(Group.WEFE_FLOW, model);
    }

    public FlowConfigModel getFlowConfig() {
        return getModel(Group.WEFE_FLOW, FlowConfigModel.class);
    }

    public void setServingConfig(ServingConfigModel model) throws StatusCodeWithException {
        put(Group.WEFE_SERVING, model);
    }

    public ServingConfigModel getServingConfig() {
        return getModel(Group.WEFE_SERVING, ServingConfigModel.class);
    }

}
