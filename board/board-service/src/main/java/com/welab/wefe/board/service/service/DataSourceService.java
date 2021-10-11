/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.datasource.AddApi;
import com.welab.wefe.board.service.api.datasource.DeleteApi;
import com.welab.wefe.board.service.api.datasource.QueryApi;
import com.welab.wefe.board.service.api.datasource.TestDBConnectApi;
import com.welab.wefe.board.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.board.service.database.repository.DataSourceRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Connection;

/**
 * @author Johnny.lin
 */
@Service
public class DataSourceService extends AbstractService {
    @Autowired
    DataSourceRepository dataSourceRepo;

    public AddApi.DataSourceAddOutput add(AddApi.DataSourceAddInput input) throws StatusCodeWithException {

        if (dataSourceRepo.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException("此数据源名称已存在，请换一个数据源名称", StatusCode.PARAMETER_VALUE_INVALID);
        }

        // Test if the connection is available
        testdbconnect(input);

        DataSourceMySqlModel model = ModelMapper.map(input, DataSourceMySqlModel.class);
        model.setCreatedBy(CurrentAccount.id());
//        model.setPassword(Md5.of(model.getPassword()));
        dataSourceRepo.save(model);

        AddApi.DataSourceAddOutput output = new AddApi.DataSourceAddOutput();
        output.setId(model.getId());
        return output;
    }

    /**
     * Delete data sources
     */
    public void delete(DeleteApi.Input input) {
        DataSourceMySqlModel model = dataSourceRepo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        dataSourceRepo.deleteById(input.getId());
    }

    /**
     * Query data source by pagination
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {
        Specification<DataSourceMySqlModel> where = Where.create()
                .equal("name", input.getName())
                .build(DataSourceMySqlModel.class);

        return dataSourceRepo.paging(where, input, QueryApi.Output.class);
    }

    /**
     * Test database connection
     */
    public TestDBConnectApi.Output testdbconnect(AddApi.DataSourceAddInput input) throws StatusCodeWithException {

        Connection conn = JdbcManager.getConnection(input.getDatabaseType(), input.getHost(), input.getPort(), input.getUserName(), input.getPassword(), input.getDatabaseName());
        if (conn != null) {
            boolean success = JdbcManager.testQuery(conn);
            if (!success) {
                throw new StatusCodeWithException(StatusCode.DATABASE_LOST, "数据库连接失败");
            }
        }

        TestDBConnectApi.Output output = new TestDBConnectApi.Output();
        output.setResult(true);
        return output;
    }

}
