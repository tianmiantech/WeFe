package com.welab.wefe.serving.service.api.clientservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.service.ClientServiceService;

@Api(path = "clientservice/update_client_service_info", name = "update client service info")
public class UpdateClientServiceInfoApi extends AbstractNoneInputApi<UpdateClientServiceInfoApi.Output> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        List<ClientServiceMysqlModel> all = clientServiceService.getAll();
        for (ClientServiceMysqlModel model : all) {
            // find client
            model.setPublicKey(null);
            model.setCode(null);
            model.setIpAdd(null);
            if (model.getType() == null || model.getType() == 0) {// 开通的服务
                model.setType(ServiceClientTypeEnum.OPEN.getValue());
            }
            
        }
        return null;
    }

    public static class Output extends AbstractApiOutput {

    }

}
