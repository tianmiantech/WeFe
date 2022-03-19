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

package com.welab.wefe.serving.service.database.serving.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author eval
 **/
@Entity(name = "operator_log")
public class OperationLogMysqlModel extends AbstractMySqlModel {

	private static final long serialVersionUID = 6979249624126197334L;

	/**
     * 请求接口
     */
	@Column(name="log_interface")
    private String logInterface;

    /**
     * 请求接口名称
     */
    @Column(name="interface_name")
    private String interfaceName;

    /**
     * 请求IP
     */
    @Column(name="request_ip")
    private String requestIp;

    /**
     * 操作人员编号
     */
    @Column(name="operator_id")
    private String operatorId;

    /**
     * 请求token
     */
    private String token;

    /**
     * 操作行为
     */
    @Column(name="log_action")
    private String logAction;

    /**
     * 请求结果编码
     */
    @Column(name="result_code")
    private int resultCode;

    /**
     * 请求结果
     */
    @Column(name="result_message")
    private String resultMessage;

    /**
     * 请求时间
     */
    @Column(name="request_time")
    private Date requestTime;

    /**
     * 耗时
     */
    private long spend;

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

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }
}
