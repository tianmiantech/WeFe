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

package com.welab.wefe.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * configuration system file
 *
 * @author aaron.li
 **/
@Component
@PropertySource(value = {"file:${config.path}"}, encoding = "utf-8")
@ConfigurationProperties
public class ConfigProperties {

    /**
     * rpc service port number
     */
    @Value("${rpc.server.port}")
    private Integer rpcServerPort;


    /**
     * Message persistence type to be forwarded（The default is localfilesys）
     */
    @Value("${send.transfer.meta.persistent.type:localFileSys}")
    private String sendTransferMetaPersistentType;

    /**
     * If the persistence type of the message to be forwarded is localfilesys: configure the corresponding persistence directory
     */
    @Value("${send.transfer.meta.persistent.temp.dir}")
    private String sendTransferMetaPersistentTempDir;

    /**
     * Persistent directory after receiving remote messages (localfilesys by default)
     */
    @Value("${recv.transfer.meta.persistent.type:localFileSys}")
    private String recvTransferMetaPersistentType;

    /**
     * If the persistence type of the received meta message is localfilesys, the persistence directory needs to be configured
     */
    @Value("${recv.transfer.meta.persistent.temp.dir}")
    private String recvTransferMetaPersistentTempDir;

    /**
     * Basic HTTP path of union service
     */
    @Value("${wefe.union.base-url}")
    private String wefeUnionBaseUrl;

    /**
     * Number of concurrent core threads when writing to the library when of type dsource processor(big data)
     */
    @Value("${data.sink.core.pool.size:30}")
    private int dataSinkCorePoolSize;

    /**
     * Size of data block fragment of forwarding dsource type (only valid for processor of dsource processor type), unit: M
     */
    @Value("${send.action.config.block.size:1}")
    private double sendActionConfigBlockSize;

    public Integer getRpcServerPort() {
        return rpcServerPort;
    }

    public void setRpcServerPort(Integer rpcServerPort) {
        this.rpcServerPort = rpcServerPort;
    }

    public String getSendTransferMetaPersistentTempDir() {
        return sendTransferMetaPersistentTempDir;
    }

    public void setSendTransferMetaPersistentTempDir(String sendTransferMetaPersistentTempDir) {
        this.sendTransferMetaPersistentTempDir = sendTransferMetaPersistentTempDir;
    }

    public String getSendTransferMetaPersistentType() {
        return sendTransferMetaPersistentType;
    }

    public void setSendTransferMetaPersistentType(String sendTransferMetaPersistentType) {
        this.sendTransferMetaPersistentType = sendTransferMetaPersistentType;
    }


    public String getRecvTransferMetaPersistentType() {
        return recvTransferMetaPersistentType;
    }

    public void setRecvTransferMetaPersistentType(String recvTransferMetaPersistentType) {
        this.recvTransferMetaPersistentType = recvTransferMetaPersistentType;
    }

    public String getRecvTransferMetaPersistentTempDir() {
        return recvTransferMetaPersistentTempDir;
    }

    public void setRecvTransferMetaPersistentTempDir(String recvTransferMetaPersistentTempDir) {
        this.recvTransferMetaPersistentTempDir = recvTransferMetaPersistentTempDir;
    }


    public String getWefeUnionBaseUrl() {
        return wefeUnionBaseUrl;
    }

    public void setWefeUnionBaseUrl(String wefeUnionBaseUrl) {
        this.wefeUnionBaseUrl = wefeUnionBaseUrl;
    }


    public int getDataSinkCorePoolSize() {
        return dataSinkCorePoolSize;
    }

    public void setDataSinkCorePoolSize(int dataSinkCorePoolSize) {
        this.dataSinkCorePoolSize = dataSinkCorePoolSize;
    }

    public double getSendActionConfigBlockSize() {
        return sendActionConfigBlockSize;
    }

    public void setSendActionConfigBlockSize(double sendActionConfigBlockSize) {
        this.sendActionConfigBlockSize = sendActionConfigBlockSize;
    }

}
