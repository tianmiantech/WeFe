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

package com.welab.wefe.fusion.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author hunter.zhao
 */
public class SocketUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SocketUtils.class);

    private int retryCount = 1;
    private long retryDelay = 0;

    private String ip;
    private Integer port;

    public SocketUtils(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    /**
     * Set the number of retries. The default value is 1.
     */
    public SocketUtils setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * Set the retry interval. The default value is 0.
     */
    public SocketUtils setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }


    public static SocketUtils create(String ip, int port) {
        return new SocketUtils(ip, port);
    }

    public Socket builder() {
        Socket socket = null;
        for (int remainingCount = retryCount; remainingCount >= 0; remainingCount--) {
            try {
                socket = new Socket(InetAddress.getByName(ip), port);

                if (socket != null) {
                    break;
                }
            } catch (IOException e) {
                LOG.warn("new socket error , {}", e.getMessage());
            } finally {
                if (retryDelay > 0) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        LOG.warn("thread sleep error , {}", e.getMessage());
                    }
                }
            }
        }

        return socket;
    }

    public static void close(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
