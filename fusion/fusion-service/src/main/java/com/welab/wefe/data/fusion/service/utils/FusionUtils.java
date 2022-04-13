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
package com.welab.wefe.data.fusion.service.utils;

import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.fusion.core.utils.PSIUtils;

import java.net.Socket;
import java.util.List;

/**
 * @author hunter.zhao
 * @date 2022/2/25
 */
public class FusionUtils {


    /**
     * Send data with an index
     *
     * @param bs
     * @param index
     */
    public static void sendByteAndIndex(Socket socket, byte[][] bs, int index) {
        PSIUtils.send2DBytes(socket, addIndex(bs, index));
    }

    /**
     * add index
     *
     * @param bs
     */
    public static byte[][] addIndex(byte[][] bs, int index) {
        byte[][] ret = new byte[bs.length + 1][];
        for (int i = 0; i < bs.length; i++) {
            ret[i] = bs[i];
        }
        ret[bs.length] = ByteUtils.intToByteArray(index);
        return ret;
    }

    /**
     * Extract action
     *
     * @param body
     */
    public static String extractAction(List<String> body) {
        String action = body.get(0);
        body.remove(0);
        return action;
    }

    /**
     * Extract index
     *
     * @param body
     */
    public static int extractIndex(List<String> body) {
        int index = Integer.valueOf(body.get(0));
        body.remove(0);
        return index;
    }

    /**
     * Extract index
     *
     * @param bs
     */
    public static int extractIndex(byte[][] bs) {
        return ByteUtils.byteArrayToInt(bs[bs.length - 1]);
    }

    /**
     * Extract data
     *
     * @param body
     */
    public static byte[][] extractData(List<String> body) {

        byte[][] bs = new byte[body.size()][];

        //加密
        for (int i = 0; i < body.size(); i++) {
            bs[i] = Base64Util.base64ToByteArray(body.get(i));
        }

        return bs;
    }

    /**
     * Extract data
     *
     * @param bs
     */
    public static byte[][] extractData(byte[][] bs) {
        byte[][] ret = new byte[bs.length - 1][];
        for (int i = 0; i < bs.length - 1; i++) {
            ret[i] = bs[i];
        }

        return ret;
    }
}
