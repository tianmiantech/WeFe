package com.welab.wefe.data.fusion.service.api.datasource;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.api.system.TaskOverviewApi;
import com.welab.wefe.data.fusion.service.dto.entity.DataSourceOverviewOutput;
import com.welab.wefe.data.fusion.service.dto.entity.TaskOverviewOutput;
import com.welab.wefe.data.fusion.service.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author hunter.zhao
 */
@Api(path = "data_source/overview", name = "data source overview", desc = "data source overview")
public class OverviewApi extends AbstractApi<TaskOverviewApi.Input, DataSourceOverviewOutput> {

    @Autowired
    DataSourceService dataSourceService;

    @Override
    protected ApiResult<DataSourceOverviewOutput> handle(TaskOverviewApi.Input input) throws StatusCodeWithException, IOException {
        return success(dataSourceService.overview());
    }
}
