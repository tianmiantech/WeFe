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
package com.welab.wefe.common.fastjson;

import com.alibaba.fastjson.serializer.ValueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;

/**
 * 当对象序列化后输出到日志中时，使用此自定义序列化可以避免输出过长的日志。
 * <p>
 * 使用方法：
 * JSON.toJSONString(result, new LoggerValueFilter());
 *
 * @author zane
 * @date 2022/4/27
 */
public class LoggerValueFilter implements ValueFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerValueFilter.class);

    @Override
    public Object process(Object object, String name, Object value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof String) {
                return process(object, name, (String) value);
            } else if (value instanceof byte[]) {
                return process(object, name, (byte[]) value);
            } else if (value instanceof File) {
                return process(object, name, (File) value);
            } else if (value instanceof FileSystemResource) {
                return process(object, name, (FileSystemResource) value);
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return value;
        }


        return value;
    }

    public Object process(Object object, String name, FileSystemResource file) throws IOException {
        String value = "";
        if (file.exists()) {
            if (file.isFile()) {
                value = "file(" + file.contentLength() + "byte):" + file.getPath() + "/" + file.getFilename();
            } else {
                value = "dir:" + file.getPath() + "/" + file.getFilename();
            }
        } else {
            value = "file:" + file.getFilename();
        }
        return value;
    }

    public Object process(Object object, String name, File file) {
        String value = "";
        if (file.exists()) {
            if (file.isDirectory()) {
                value = "dir:" + file.getAbsolutePath();
            } else {
                value = "file(" + file.length() + "byte):" + file.getAbsolutePath();
            }
        } else {
            value = "file:" + file.getAbsolutePath();
        }
        return value;
    }

    public Object process(Object object, String name, byte[] value) {
        return "bytes(length " + value.length + ")";
    }

    public Object process(Object object, String name, String value) {
        // 对过长的文本进行截断处理
        int length = value.length();
        if (length > 1024) {
            return value.substring(0, 50) + "...(length:" + length + ")";
        }

        return value;
    }

}
