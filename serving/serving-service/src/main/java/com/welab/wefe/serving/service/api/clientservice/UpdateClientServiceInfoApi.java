package com.welab.wefe.serving.service.api.clientservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.service.ClientService;
import com.welab.wefe.serving.service.service.ClientServiceService;

@Api(path = "clientservice/update_client_service_info", name = "update client service info")
public class UpdateClientServiceInfoApi extends AbstractNoneInputApi<UpdateClientServiceInfoApi.Output> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        List<ClientServiceMysqlModel> all = clientServiceService.getAll();
        for (ClientServiceMysqlModel model : all) {
            // find client
            ClientMysqlModel client = clientService.queryByClientId(model.getClientId());
            if (client == null) {
                continue;
            }
            model.setPublicKey(client.getPubKey());
            model.setCode(client.getCode());
            if (model.getType() == null || model.getType() == 0) {// 开通的服务
                model.setType(ServiceClientTypeEnum.OPEN.getValue());
            } else { // 激活的服务不用动
                continue;
            }
            clientServiceRepository.save(model);
        }
        return success();
    }

    public static class Output extends AbstractApiOutput {

    }

}
