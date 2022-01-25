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
package com.welab.wefe.board.service.fusion.manager;



import com.welab.wefe.board.service.database.entity.fusion.ExportProgressMySqlModel;
import com.welab.wefe.board.service.dto.fusion.FusionResultExportProgress;
import com.welab.wefe.board.service.service.fusion.ExportProgressService;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hunter.zhao
 */
public class ExportManager {
    public static final Logger LOG = LoggerFactory.getLogger(ActuatorManager.class);

    /**
     * businessId : JSON
     */
    private static final ConcurrentHashMap<String, FusionResultExportProgress> EXPORT_TASK = new ConcurrentHashMap<>();

    private static final ExportProgressService exportProgressService;

    static {
        exportProgressService = Launcher.CONTEXT.getBean(ExportProgressService.class);
    }

    public static FusionResultExportProgress get(String businessId) {

        if (EXPORT_TASK.get(businessId) == null) {
            //直接查表
            ExportProgressMySqlModel model = exportProgressService.findLastByBusinessId(businessId);
            return ModelMapper.map(model, FusionResultExportProgress.class);
        }

        FusionResultExportProgress progress = EXPORT_TASK.get(businessId);
        if (progress.getProgress() == 100) {
            //remove;
            romove(progress);
        }

        return progress;

//        return EXPORT_TASK.get(businessId);
    }

    public static void set(String businessId, FusionResultExportProgress progress) {

        if (EXPORT_TASK.containsKey(businessId)) {
            throw new RuntimeException(" There are fusion tasks being exported");
        }

        LOG.info("Set FusionResultExportProgress successfully, businessId is {}", businessId);
        EXPORT_TASK.put(businessId, progress);
    }

    public static void romove(FusionResultExportProgress progress) {
        exportProgressService.add(progress);

        EXPORT_TASK.remove(progress.getBusinessId());
    }
}
