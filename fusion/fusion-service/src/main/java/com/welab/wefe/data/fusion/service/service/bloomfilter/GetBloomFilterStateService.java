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

package com.welab.wefe.data.fusion.service.service.bloomfilter;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.api.bloomfilter.GetBloomFilterStateApi;
import com.welab.wefe.data.fusion.service.api.bloomfilter.GetBloomFilterStateApi.Output;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.database.repository.base.BloomFilterRepository;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * @author jacky.jiang
 */
@Service
public class GetBloomFilterStateService extends AbstractService {
    @Autowired
    protected BloomFilterRepository dataSetRepository;

    public Output getStatue(GetBloomFilterStateApi.Input input) throws StatusCodeWithException, IOException{
        BloomFilterMySqlModel model = dataSetRepository.findOne("id", input.getBloomFilterId(), BloomFilterMySqlModel.class);

        Output output = new Output();

        int processCount = model.getProcessCount();
        int rowsCount = model.getRowCount();
        DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);

        dataSetRepository.updateById(model.getId(), "processCount", processCount, DataSetMySqlModel.class);
        if (processCount == 0 || rowsCount == 0 || processCount < rowsCount){

            dataSetRepository.updateById(model.getId(), "process", Progress.Running, DataSetMySqlModel.class);
            output.setProgress(Progress.Running);
        }else if (processCount >= rowsCount){

            dataSetRepository.updateById(model.getId(), "process", Progress.Success, DataSetMySqlModel.class);
            output.setProgress(Progress.Success);
        }

        output.setBloomFilterId(input.getBloomFilterId());
        output.setRowCount(model.getRowCount());
        output.setProcessCount(model.getProcessCount());


        return output;
    }
}
