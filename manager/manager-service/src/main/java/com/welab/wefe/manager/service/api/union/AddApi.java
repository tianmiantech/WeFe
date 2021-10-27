package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataSetDefaultTag;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberOutput;
import com.welab.wefe.manager.service.dto.tag.DatSetDefaultTagAddInput;
import com.welab.wefe.manager.service.service.DatSetDefaultTagContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "union/node/add", name = "union_node_add",login = false)
public class AddApi extends AbstractApi<DatSetDefaultTagAddInput, AbstractApiOutput> {

    @Autowired
    private DatSetDefaultTagContractService datSetDefaultTagContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(DatSetDefaultTagAddInput input) throws StatusCodeWithException {
        LOG.info("AddApi handle..");
        try {
            DataSetDefaultTag dataSetDefaultTag = new DataSetDefaultTag();
            dataSetDefaultTag.setTagName(input.getTagName());
            dataSetDefaultTag.setExtJson(input.getExtJson());
            datSetDefaultTagContractService.add(dataSetDefaultTag);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}
