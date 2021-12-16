package com.welab.wefe.serving.service.api.clientservice;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ClientServiceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author ivenn.zheng
 */
@Api(path = "clientservice/query", name = "get models ")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<QueryApi.Output>> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(clientServiceService.query(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "id")
        private String id;

        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "客户邮箱")
        private String clientName;

        @Check(name = "启用状态")
        private Integer status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    public static class Output extends AbstractApiOutput {

        @Check(name = "客户id")
        private String id;

        @Check(name = "客户名称")
        private String name;

        @Check(name = "客户邮箱")
        private String email;

        @Check(name = "ip 地址")
        private String ipAdd;

        @Check(name = "公钥")
        private String pubKey;

    }

}
