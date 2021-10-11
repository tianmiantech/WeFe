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

package com.welab.wefe.union.service.listener;

import com.welab.wefe.union.service.common.BlockChainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author aaron.li
 **/
@Component
public class AppListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AppListener.class);

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        initBlockChain();
    }


    /**
     * Initialize blockchain information
     */
    private void initBlockChain() {
        if (!BlockChainContext.getInstance().init()) {
            LOG.error("Failed to initialize the blockchain context, the system exits!");
            System.exit(-1);
        }
        BlockChainContext.getInstance();
    }
}
