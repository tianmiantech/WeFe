package com.welab.wefe.serving.service.api.clientservice;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author ivenn.zheng
 */
@Api(path = "clientservice/query", name = "get models " ,login = false)
public class QueryApi extends AbstractApi<QueryApi.Input, ClientServiceOutputModel> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<ClientServiceOutputModel> handle(Input input) throws StatusCodeWithException, IOException {
        return success(clientServiceService.queryOne(input));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }



}
