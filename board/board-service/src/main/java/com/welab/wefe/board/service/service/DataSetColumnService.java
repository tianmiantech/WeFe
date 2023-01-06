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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.project.dataset.GetFeaturesApi;
import com.welab.wefe.board.service.database.entity.data_set.DataSetColumnMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.repository.DataSetColumnRepository;
import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnInputModel;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnOutputModel;
import com.welab.wefe.board.service.dto.vo.FeatureOutput;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane.luo
 */
@Service
public class DataSetColumnService extends AbstractService {
    @Autowired
    DataSetColumnRepository dataSetColumnRepository;
    @Autowired
    private ProjectDataSetService projectDataSetService;

    /**
     * 获取项目中数据集的特征列表
     * <p>
     * 由于特征数据类型没有上报到 union，所以在编辑训练时，看不到对方的特征数据类型，这个方法就是为了提供数据类型。
     */
    public List<FeatureOutput> listProjectDataSetFeatures(GetFeaturesApi.Input input) throws StatusCodeWithException {
        // 如果是取其它成员的特征列表，走gateway。
        if (!CacheObjects.getMemberId().equals(input.memberId)) {
            GetFeaturesApi.Output output = gatewayService.callOtherMemberBoard(
                    input.memberId,
                    GetFeaturesApi.class,
                    input,
                    GetFeaturesApi.Output.class
            );
            return output.list;
        }

        ProjectDataSetMySqlModel projectDataSet = projectDataSetService.findOne(input.projectId, input.dataSetId);
        if (projectDataSet == null || projectDataSet.getAuditStatus() != AuditStatus.agree) {
            return null;
        }

        Specification<DataSetColumnMysqlModel> where = Where
                .create()
                .equal("dataSetId", input.dataSetId)
                .orderBy("index", OrderBy.asc)
                .build(DataSetColumnMysqlModel.class);

        List<DataSetColumnMysqlModel> list = dataSetColumnRepository.findAll(where);

        return ModelMapper.maps(list, FeatureOutput.class);
    }

    public PagingOutput<DataSetColumnOutputModel> query(String dataSetId) {
        Specification<DataSetColumnMysqlModel> where = Where
                .create()
                .equal("dataSetId", dataSetId)
                .orderBy("index", OrderBy.asc)
                .build(DataSetColumnMysqlModel.class);

        // The front end does not do paging,
        // but considering that there may be a data set with a particularly large number of fields,
        // the paging method is used to query here.
        return dataSetColumnRepository.paging(
                where,
                new PagingInput(0, 10000),
                DataSetColumnOutputModel.class
        );
    }

    public void update(String dataSetId, List<DataSetColumnInputModel> list) {
        // clear data set columns
        dataSetColumnRepository.deleteByDataSetId(dataSetId);

        // save data set columns
        for (int i = 0; i < list.size(); i++) {
            DataSetColumnInputModel item = list.get(i);

            DataSetColumnMysqlModel column = ModelMapper.map(item, DataSetColumnMysqlModel.class);
            column.setDataSetId(dataSetId);
            column.setIndex(i);

            dataSetColumnRepository.save(column);
        }
    }
}
