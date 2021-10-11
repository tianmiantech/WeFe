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

package com.welab.wefe.board.service.config;

import com.welab.wefe.board.service.service.MemberChatService;
import com.welab.wefe.board.service.service.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author Johnny.lin
 */
@Configuration
public class WebSocketConfig {

    /**
     * This Bean will automatically register the web socket endpoint
     * declared with the @ServerEndpoint annotation
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * Because SpringBoot WebSocket creates a WebSocketServer
     * (corresponding to the @ServerEndpoint annotation)
     * object for each client connection, the Bean injection operation
     * will be directly skipped, so manually inject a global variable
     */
    @Autowired
    public void setMemberChatService(MemberChatService memberChatService) {
        WebSocketServer.memberChatService = memberChatService;
    }
}
