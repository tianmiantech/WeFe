package com.welab.wefe.serving.service.api.partner;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.MemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.PartnerRepository;
import com.welab.wefe.serving.service.enums.ClientStatusEnum;

@Api(path = "partner/init", name = "init partner")
public class InitPartnerApi extends AbstractNoneInputApi<InitPartnerApi.Output> {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        List<MemberMySqlModel> members = memberRepository.findAll();
        List<ClientMysqlModel> clients = clientRepository.findAll();

        for (MemberMySqlModel m : members) {
            PartnerMysqlModel model = ModelMapper.map(m, PartnerMysqlModel.class);
            model.setServingBaseUrl(m.getApi());
            model.setPartnerId(m.getMemberId());
            model.setUnionMember(true);
            model.setStatus(ClientStatusEnum.NORMAL.getValue());
            partnerRepository.save(model);
        }
        
        for (ClientMysqlModel c : clients) {
            PartnerMysqlModel model = ModelMapper.map(c, PartnerMysqlModel.class);
            model.setStatus(c.getStatus());
            partnerRepository.save(model);
        }
        return null;
    }

    public static class Output {

    }

}
