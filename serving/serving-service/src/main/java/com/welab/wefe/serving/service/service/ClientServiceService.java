package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.utils.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ivenn.zheng
 */
@Service
public class ClientServiceService {



    @Autowired
    private ClientServiceRepository clientServiceRepository;

    public void save(SaveApi.Input input) {

        ClientServiceMysqlModel model = clientServiceRepository.findOne("id", input.getId(), ClientServiceMysqlModel.class);

        if (null == model) {
            model = new ClientServiceMysqlModel();
        }

        model.setServiceId(input.getServiceId());
        model.setClientId(input.getClientId());
        model.setStatus(input.getStatus());

        clientServiceRepository.save(model);

    }

    /**
     * Paging query
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {

        Specification<ClientServiceMysqlModel> where = Where
                .create()
                .contains("id", input.getId())
//                .contains("")
                .build(ClientServiceMysqlModel.class);

        PagingOutput<ClientServiceMysqlModel> page = clientServiceRepository.paging(where, input);

        List<QueryApi.Output> list = page
                .getList()
                .stream()
                // .filter(x -> member.contains(x.getModelId()))
                .map(x -> ModelMapper.map(x, QueryApi.Output.class))
                .collect(Collectors.toList());

        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }


}
