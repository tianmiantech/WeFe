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
package com.welab.wefe.board.service.service.fusion;



import com.welab.wefe.board.service.database.entity.fusion.ExportProgressMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.ExportProgressRepository;
import com.welab.wefe.board.service.dto.fusion.FusionResultExportProgress;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hunter.zhao
 */
@Service
public class ExportProgressService extends AbstractService {
    @Autowired
    ExportProgressRepository exportProgressRepository;

    public ExportProgressMySqlModel findByBusinessId(String businessId) {
        return exportProgressRepository.findOne("business_id", businessId, ExportProgressMySqlModel.class);
    }


    public ExportProgressMySqlModel findLastByBusinessId(String businessId) {
        return exportProgressRepository.findLastByBusinessId(businessId);
    }

    public void add(FusionResultExportProgress progress) {
        ExportProgressMySqlModel model = ModelMapper.map(progress, ExportProgressMySqlModel.class);
        exportProgressRepository.save(model);
    }
}
