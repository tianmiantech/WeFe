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
package com.welab.wefe.common.wefe.checkpoint;

import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceAvailableCheckOutput;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceCheckPointOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 这里会自动扫描所有 AbstractCheckpoint 的子类
 * 并在 checkAll() 时对所有检查点进行检查。
 *
 * @author zane
 * @date 2021/12/21
 */
@Service
public class CheckpointManager {
    @Autowired
    private ApplicationContext applicationContext;

    private static final List<Class<?>> CHECKPOINT_LIST;

    static {
        // 通过反射扫描的方式获取所有检查点
        CHECKPOINT_LIST = ReflectionsUtil.getClassesExtending(AbstractCheckpoint.class);
    }

    public ServiceAvailableCheckOutput checkAll() {
        List<ServiceCheckPointOutput> list = new ArrayList<>();

        for (Class<?> clazz : CHECKPOINT_LIST) {
            AbstractCheckpoint checkpoint = (AbstractCheckpoint) applicationContext.getBean(clazz);
            ServiceCheckPointOutput result = checkpoint.check();
            list.add(result);
        }

        return new ServiceAvailableCheckOutput(list);
    }

}
