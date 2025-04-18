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

package com.welab.wefe.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * SHA-1 已在数学算法层面被破解，请使用 SHA-256 或 SHA-512。
 *
 * @author hunter.zhao
 */
@Deprecated
public class Sha1 {
    public static String of(final String data) {
        return data == null ? null : DigestUtils.sha1Hex(data);
    }

    public static String of(final byte[] data) {
        return data == null ? null : DigestUtils.sha1Hex(data);
    }

    public static String of(final InputStream data) throws IOException {
        return data == null ? null : DigestUtils.sha1Hex(data);
    }
}
