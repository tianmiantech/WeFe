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

import com.welab.wefe.common.constant.Constant;
import com.welab.wefe.common.constant.ZipType;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Base64Util.java
 * <p>
 * Copyright 2015 WeLab Holdings, Inc. All rights reserved.
 * WELAB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author andy.zhang
 */
public class Base64Util {
    protected static final Logger LOG = LoggerFactory.getLogger(Base64Util.class);


    /**
     * The string goes to base64, using UTF-8 encoding.
     */
    public static String encode(String content) {
        return encode(content, Constant.ENCODING_UTF8);
    }

    /**
     * The string goes to base64
     */
    public static String encode(String content, String charsetName) {
        return encode(content, charsetName, null);
    }

    /**
     * The string goes to base64
     *
     * @param content     The content to transcode
     * @param charsetName Encoding of text
     * @param zipType     Decompression mode: compressed when null is used.
     */
    public static String encode(String content, String charsetName, String zipType) {
        byte[] bytes = new byte[0];
        try {
            bytes = content.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }

        if (!StringUtil.isEmpty(zipType)) {
            switch (zipType.toLowerCase()) {
                case ZipType.ZIP:
                    bytes = ZipUtil.zipBytes(bytes);
                    break;
                case ZipType.GZIP:
                    bytes = GzipUtil.zip(bytes);
                    break;
                default:
            }
        }

        bytes = Base64.encodeBase64(bytes);
        return new String(bytes, Charset.forName(charsetName));
    }

    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return new String(Base64.encodeBase64(data));
    }

    /**
     * Decoding base64 content, using UTF-8 encoding by default.
     *
     * @param base64 What to decode
     */
    public static String decode(String base64) {
        return decode(base64, Constant.ENCODING_UTF8);
    }

    /**
     * Decodes base64 content
     *
     * @param base64 The character string is Base64 encrypted
     */
    public static String decode(String base64, String charsetName) {
        return decode(base64, charsetName, null);
    }

    /**
     * Decodes base64 content into text
     *
     * @param base64      What to decode
     * @param charsetName Encoding of text
     * @param unZipType   Decompression mode. If the value is null, decompression is not required.
     */
    public static String decode(String base64, String charsetName, String unZipType) {
        byte[] bytes = Base64.decodeBase64(base64);

        if (!StringUtil.isEmpty(unZipType)) {
            switch (unZipType.toLowerCase()) {
                case ZipType.ZIP:
                    bytes = ZipUtil.unzipBytes(bytes);
                    break;
                case ZipType.GZIP:
                    bytes = GzipUtil.unzip(bytes);
                    break;
                default:
            }
        }

        if (bytes == null) {
            return null;
        }

        return new String(bytes, Charset.forName(charsetName));
    }


    /**
     * Translates the specified Base64 string (as per Preferences.get(byte[]))
     * into a byte array.
     *
     * @throw IllegalArgumentException if <tt>s</tt> is not a valid Base64
     * string.
     */
    public static byte[] base64ToByteArray(String s) {
        return base64ToByteArray(s, false);
    }

    private static byte[] base64ToByteArray(String s, boolean alternate) {
        byte[] alphaToInt = (alternate ? ALT_BASE64_TO_INT : BASE64_TO_INT);
        int sLen = s.length();
        int numGroups = sLen / 4;
        if (4 * numGroups != sLen) {
            throw new IllegalArgumentException("String length must be a multiple of four.");
        }
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (s.charAt(sLen - 1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (s.charAt(sLen - 2) == '=') {
                missingBytesInLastGroup++;
            }
        }
        byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];
        char[] a = s.toCharArray();
        // Translate all full groups from base64 to byte array elements
        int inCursor = 0, outCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int ch0 = base64toInt(a[inCursor++], alphaToInt);
            int ch1 = base64toInt(a[inCursor++], alphaToInt);
            int ch2 = base64toInt(a[inCursor++], alphaToInt);
            int ch3 = base64toInt(a[inCursor++], alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (missingBytesInLastGroup != 0) {
            int ch0 = base64toInt(a[inCursor++], alphaToInt);
            int ch1 = base64toInt(a[inCursor++], alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missingBytesInLastGroup == 1) {
                int ch2 = base64toInt(a[inCursor++], alphaToInt);
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        // assert inCursor == s.length()-missingBytesInLastGroup;
        // assert outCursor == result.length;
        return result;
    }

    /**
     * Translates the specified character, which is assumed to be in the
     * "Base 64 Alphabet" into its equivalent 6-bit positive integer.
     *
     * @throw IllegalArgumentException or ArrayOutOfBoundsException if c is not
     * in the Base64 Alphabet.
     */
    private static int base64toInt(char c, byte[] alphaToInt) {
        int result = alphaToInt[c];
        if (result < 0) {
            throw new IllegalArgumentException("Illegal character " + c);
        }
        return result;
    }

    /**
     * This array is a lookup table that translates unicode characters drawn
     * from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045) into
     * their 6-bit positive integer equivalents. Characters that are not in the
     * Base64 alphabet but fall within the bounds of the array are translated to
     * -1.
     */
    private static final byte[] BASE64_TO_INT = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1,
            -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
            43, 44, 45, 46, 47, 48, 49, 50, 51};


    /**
     * This array is the analogue of base64ToInt, but for the nonstandard
     * variant that avoids the use of uppercase alphabetic characters.
     */
    private static final byte[] ALT_BASE64_TO_INT = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7,
            8, -1, 62, 9, 10, 11, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 12, 13, 14, -1, 15, 63, 16, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 17, -1, 18, 19, 21, 20, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
            43, 44, 45, 46, 47, 48, 49, 50, 51, 22, 23, 24, 25};

}
