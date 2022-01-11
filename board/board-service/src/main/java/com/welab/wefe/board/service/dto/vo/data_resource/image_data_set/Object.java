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
package com.welab.wefe.board.service.dto.vo.data_resource.image_data_set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author zane
 * @date 2021/11/8
 */
@XStreamAlias("object")
public class Object {
    public String name;
    /**
     * 关于目标物体姿态描述（非必须字段）
     */
    public String pose = "Unspecified";
    /**
     * 如果物体的遮挡超过15-20％并且位于边界框之外，请标记为truncated（非必须字段）
     */
    public int truncated;
    /**
     * 难以识别的物体标记为difficult（非必须字段）
     */
    public int difficult;
    public Bndbox bndbox;
}
