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
package com.welab.wefe.common.fastjson;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 自定义序列化
 *
 * @author zane
 * @date 2021/11/30
 */
public class FileSystemResourceSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        FileSystemResource file = (FileSystemResource) object;

        if (file == null) {
            out.writeNull();
            return;
        }

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

        out.writeString(value);
    }
}
