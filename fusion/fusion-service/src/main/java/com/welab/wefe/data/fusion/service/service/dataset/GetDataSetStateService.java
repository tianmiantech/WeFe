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

package com.welab.wefe.data.fusion.service.service.dataset;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.api.dataset.GetDataSetStateApi;
import com.welab.wefe.data.fusion.service.api.dataset.GetDataSetStateApi.Output;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * @author jacky.jiang
 */
@Service
public class GetDataSetStateService extends AbstractService {
    @Autowired
    protected DataSetRepository dataSetRepository;

    public Output getStatue(GetDataSetStateApi.Input input) throws StatusCodeWithException, IOException {
        DataSetMySqlModel model = dataSetRepository.findOne("id", input.getDataSetId(), DataSetMySqlModel.class);

        Output output = new Output();

        int processCount = 0;
        try {
            processCount = DataSetStorageHelper.countDataSetRows(model);
        } catch (Exception e) {
            LOG.error("此id无数据");
            throw new StatusCodeWithException("此id无数据", StatusCode.DATA_NOT_FOUND);
        }

        int rowsCount = model.getRowCount();
        model.setProcessCount(processCount);
        DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
        dataSetRepository.updateById(model.getId(), "processCount", processCount, DataSetMySqlModel.class);
        if (processCount < rowsCount) {
            dataSetRepository.updateById(model.getId(), "process", Progress.Running, DataSetMySqlModel.class);
            output.setProgress(Progress.Running);
        } else if (processCount >= rowsCount) {
            dataSetRepository.updateById(model.getId(), "process", Progress.Success, DataSetMySqlModel.class);
            output.setProgress(Progress.Success);
        }

        output.setDataSetId(input.getDataSetId());
        output.setRowCount(model.getRowCount());
        output.setProcessCount(model.getProcessCount());

        return output;

    }
}
