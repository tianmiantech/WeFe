package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.serving.service.api.client.QueryClientApi;
import com.welab.wefe.serving.service.api.client.QueryClientListApi;
import com.welab.wefe.serving.service.api.client.SaveClientApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ClientStatusEnum;
import com.welab.wefe.serving.service.utils.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {


    @Autowired
    private ClientRepository clientRepository;


    public void save(SaveClientApi.Input input) {

        ClientMysqlModel model = clientRepository.findOne("id", input.getId(), ClientMysqlModel.class);

        if (null == model) {
            model = new ClientMysqlModel();
        }

        model.setName(input.getName());
        model.setEmail(input.getEmail());
        model.setIpAdd(input.getIpAdd());
        model.setRemark(input.getRemark());
        model.setPubKey(input.getPubKey());
        clientRepository.save(model);
    }


    /**
     * Paging query
     */
    public PagingOutput<QueryClientListApi.Output> queryList(QueryClientListApi.Input input) {

        Specification<ClientMysqlModel> where = Where
                .create()
                .equal("createdBy", input.getCreatedBy())
                .betweenAndDate("createdTime", input.getStartTime().getTime(), input.getEndTime().getTime())
                .equal("status", 1)
                .build(ClientMysqlModel.class);

        PagingOutput<ClientMysqlModel> page = clientRepository.paging(where, input);

        List<QueryClientListApi.Output> list = page
                .getList()
                .stream()
                // .filter(x -> member.contains(x.getModelId()))
                .map(x -> ModelMapper.map(x, QueryClientListApi.Output.class))
                .collect(Collectors.toList());

        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }

    public QueryClientApi.Output queryById(String id) {

        ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
        return ModelMapper.map(model, QueryClientApi.Output.class);


    }

    public void detele(String id) {
        ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
        model.setStatus(ClientStatusEnum.DELETED.getValue());
        clientRepository.save(model);
    }

}
