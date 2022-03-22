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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zane
 * @date 2021/12/28
 */
public class FileType {

    /**
     * 缓存文件头信息-文件头信息
     */
    private static final HashMap<String, String> FILE_TYPE_HEADER_MAP = new HashMap<>();

    static {
        // images
        FILE_TYPE_HEADER_MAP.put("FFD8FF", "jpg");
        FILE_TYPE_HEADER_MAP.put("89504E47", "png");
        FILE_TYPE_HEADER_MAP.put("47494638", "gif");
        FILE_TYPE_HEADER_MAP.put("49492A00", "tif");
        FILE_TYPE_HEADER_MAP.put("424D", "bmp");

        // CAD
        FILE_TYPE_HEADER_MAP.put("41433130", "dwg");
        FILE_TYPE_HEADER_MAP.put("38425053", "psd");
        // 日记本
        FILE_TYPE_HEADER_MAP.put("7B5C727466", "rtf");
        FILE_TYPE_HEADER_MAP.put("3C3F786D6C", "xml");
        FILE_TYPE_HEADER_MAP.put("68746D6C3E", "html");
        // 邮件
        FILE_TYPE_HEADER_MAP.put("44656C69766572792D646174653A", "eml");
        FILE_TYPE_HEADER_MAP.put("D0CF11E0", "doc");
        FILE_TYPE_HEADER_MAP.put("5374616E64617264204A", "mdb");
        FILE_TYPE_HEADER_MAP.put("252150532D41646F6265", "ps");
        FILE_TYPE_HEADER_MAP.put("255044462D312E", "pdf");
        FILE_TYPE_HEADER_MAP.put("504B0304", "docx");
        FILE_TYPE_HEADER_MAP.put("52617221", "rar");
        FILE_TYPE_HEADER_MAP.put("57415645", "wav");
        FILE_TYPE_HEADER_MAP.put("41564920", "avi");
        FILE_TYPE_HEADER_MAP.put("2E524D46", "rm");
        FILE_TYPE_HEADER_MAP.put("000001BA", "mpg");
        FILE_TYPE_HEADER_MAP.put("000001B3", "mpg");
        FILE_TYPE_HEADER_MAP.put("6D6F6F76", "mov");
        FILE_TYPE_HEADER_MAP.put("3026B2758E66CF11", "asf");
        FILE_TYPE_HEADER_MAP.put("4D546864", "mid");
        FILE_TYPE_HEADER_MAP.put("1F8B08", "gz");

    }

    public static boolean isImage(byte[] bytes) {
        String header = getFileHeader(bytes);
        String fileType = getFileType(header);
        if (StringUtil.isEmpty(fileType)) {
            return false;
        }
        switch (fileType.toLowerCase()) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "tif":
            case "bmp":
                return true;
            default:
                return false;
        }
    }


    /**
     * 根据文件路径获取文件头信息
     */
    public static String getFileType(String header) {
        if (StringUtil.isEmpty(header)) {
            return null;
        }

        for (Map.Entry<String, String> entry : FILE_TYPE_HEADER_MAP.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(header) || header.startsWith(key)) {
                return entry.getValue();
            }
        }

        return null;
    }


    public static String getFileHeader(byte[] bytes) {
        return getFileHeader(new ByteArrayInputStream(bytes));
    }

    public static String getFileHeader(File file) {
        try {
            return getFileHeader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据文件路径获取文件头信息
     */
    public static String getFileHeader(InputStream is) {

        String value = null;
        try {
            byte[] b = new byte[4];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 将要读取文件头信息的文件的byte数组转换成string类型表示
     *
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     * 下面这段代码就是用来对文件类型作验证的方法，
     * 第一个参数是文件的字节数组，第二个就是定义的可通过类型。代码很简单，         主要是注意中间的一处，将字节数组的前四位转换成16进制字符串，并且转换的时候，要先和0xFF做一次与运算。这是因为，整个文件流的字节数组中，有很多是负数，进行了与运算后，可以将前面的符号位都去掉，这样转换成的16进制字符串最多保留两位，如果是正数又小于10，那么转换后只有一位，需要在前面补0，这样做的目的是方便比较，取完前四位这个循环就可以终止了。
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

}
