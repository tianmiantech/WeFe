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
package com.welab.wefe.board.service.model;

/**
 * 用于储存创建 job 过程中的中间变量
 *
 * @author zane
 * @date 2022/4/19
 */
public class JobBuilder {
    /**
     * 数据集的版本号，目前只用在图像数据集。
     * 版本号标记可以使 visual fl 服务根据版本号减少下载图像数据集的次数
     */
    public String dataSetVersion;
}
