package com.welab.wefe.serving.service.api.clientservice;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
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
@Api(path = "clientservice/query-list", name = "query list", login = false)
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<ClientServiceOutputModel>> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<PagingOutput<ClientServiceOutputModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(clientServiceService.queryList(input));
    }

    public static class Input extends PagingInput {
        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "客户名称")
        private String clientName;

        @Check(name = "启用状态")
        private Integer status;

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
}
