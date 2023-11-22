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
package com.welab.wefe.board.service.api.file.security;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.util.FileType;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author zane
 * @date 2022/3/14
 */
public class ImageSecurityChecker extends FileSecurityChecker {

    @Override
    protected void doCheck(File file) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());
        // 读取之后就可以删除文件了
        file.delete();

        // 判断是否是图片
        if (!FileType.isImage(bytes)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的图片格式");
        }

        // 对图片文件进行缩放重绘，过滤掉内部可能包含的木马内容。
        try {
            Thumbnails
                    .of(new ByteArrayInputStream(bytes))
                    .scale(1)
                    .toFile(file);
        } catch (IOException e) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("图片已损坏：" + e.getMessage());
        }

    }
}
