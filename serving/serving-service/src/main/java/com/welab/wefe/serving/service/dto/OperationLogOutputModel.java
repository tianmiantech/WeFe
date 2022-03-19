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

package com.welab.wefe.serving.service.dto;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.serving.service.service.CacheObjects;

/**
 * @author eval
 **/
public class OperationLogOutputModel {
    @Check(name = "请求接口")
    private String logInterface;
    @Check(name = "请求接口名称")
    private String interfaceName;
    @Check(name = "请求IP")
    private String requestIp;
    @Check(name = "操作人员编号")
    private String operatorId;
    @Check(name = "请求token")
    private String token;
    @Check(name = "操作行为")
    private String logAction;
    @Check(name = "请求结果编码")
    private int resultCode;
    @Check(name = "请求结果")
    private String resultMessage;
    @Check(name = "全局唯一标识")
    private String id;
    @Check(name = "创建者昵称")
    private String creatorNickname;
    @Check(name = "修改者昵称")
    private String updaterNickname;
    //region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorNickname() {
        return creatorNickname;
    }

    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }

    public String getUpdaterNickname() {
        return updaterNickname;
    }

    public void setUpdaterNickname(String updaterNickname) {
        this.updaterNickname = updaterNickname;
    }

    public String getOperatorNickname() {
        return CacheObjects.getNickname(operatorId);
    }

    public String getLogInterface() {
        return logInterface;
    }

    public void setLogInterface(String logInterface) {
        this.logInterface = logInterface;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLogAction() {
        return logAction;
    }

    public void setLogAction(String logAction) {
        this.logAction = logAction;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
