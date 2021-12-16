package com.welab.wefe.serving.service.api.client;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;

@Api(path = "client/delete", name = "delete client")
public class DeleteApi extends AbstractNoneOutputApi<DeleteApi.Input> {

    @Autowired
    private ClientService clientService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        clientService.detele(input.getId());
        return success();
    }

    public static class Input extends AbstractApiInput{
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
