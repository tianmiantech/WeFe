package com.welab.wefe.data.fusion.service.api.system;


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.dto.entity.TaskOverviewOutput;
import com.welab.wefe.data.fusion.service.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author hunter.zhao
 */
@Api(path = "task/overview", name = "task overview", desc = "task overview")
public class TaskOverviewApi extends AbstractApi<TaskOverviewApi.Input, TaskOverviewOutput> {


    @Autowired
    TaskService taskService;


    @Override
    protected ApiResult<TaskOverviewOutput> handle(Input input) throws StatusCodeWithException, IOException {
        return success(taskService.overview());
    }


    public static class Input extends AbstractApiInput {
    }

}
