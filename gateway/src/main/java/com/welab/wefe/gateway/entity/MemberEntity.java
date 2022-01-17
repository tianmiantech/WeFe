/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.entity;

import com.welab.wefe.common.constant.SecretKeyType;

import java.io.Serializable;

/**
 * Member entity
 *
 * @author aaron.li
 **/
public class MemberEntity implements Serializable {
    /**
     * Member ID
     */
    private String id;
    /**
     * Member name
     */
    private String name;
    /**
     * IP address
     */
    private String ip;
    /**
     * port
     */
    private int port;
    /**
     * secret key
     */
    private String privateKey;
    /**
     * Public key
     */
    private String publicKey;
    /**
     * Base address of board service
     */
    private String boardUri;
    /**
     * Secret key type
     */
    private SecretKeyType secretKeyType = SecretKeyType.rsa;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getBoardUri() {
        return boardUri;
    }

    public void setBoardUri(String boardUri) {
        this.boardUri = boardUri;
    }

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }
}
