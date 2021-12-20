package com.welab.wefe.serving.service.api.clientservice;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientService;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.springframework.beans.factory.annotation.Autowired;


@Api(path = "clientservice/save", name = "save client service model", login = false)
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {


    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {

        clientServiceService.save(input);
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "id")
        private String id;

        @Check(name = "服务 id", require = true)
        private String serviceId;

        @Check(name = "客户 id", require = true)
        private String clientId;

        @Check(name = "use status")
        private Integer status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }




}
