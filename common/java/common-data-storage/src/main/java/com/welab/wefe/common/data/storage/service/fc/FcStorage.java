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
package com.welab.wefe.common.data.storage.service.fc;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.fc.aliyun.AliyunOssConfig;
import com.welab.wefe.common.data.storage.service.fc.aliyun.AliyunOssStorage;
import com.welab.wefe.common.data.storage.service.fc.tencent.TencentCosConfig;
import com.welab.wefe.common.data.storage.service.fc.tencent.TencentCosStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 函数计算存储（fc storage）
 * <p>
 * 为了提高函数计算的 IO 效率，在使用函数计算执行任务时会将数据集从持久化存储中取出到OSS进行计算，最后将结果再落回到持久化存储。
 *
 * @author zane
 * @date 2022/5/24
 */
public abstract class FcStorage {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static FcStorage storage;

    public abstract <K, V> void putAll(List<DataItemModel<K, V>> list, Map<String, Object> args) throws Exception;

    /**
     * 初始化对象
     * <p>
     * 当配置信息变化时，重新初始化即可刷新对象。
     */
    public synchronized static void initWithAliyun(AliyunOssConfig config) {
        storage = new AliyunOssStorage(config);
    }

    /**
     * 初始化对象
     * <p>
     * 当配置信息变化时，重新初始化即可刷新对象。
     */
    public synchronized static void initWithTencent(TencentCosConfig config) {
        storage = new TencentCosStorage(config);
    }

    public static FcStorage getInstance() {
        return storage;
    }

}
