package com.welab.wefe.manager.service.api.default_tag;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataSetDefaultTag;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberOutput;
import com.welab.wefe.manager.service.dto.tag.DatSetDefaultTagAddInput;
import com.welab.wefe.manager.service.dto.tag.DatSetDefaultTagDeleteInput;
import com.welab.wefe.manager.service.service.DatSetDefaultTagContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "default_tag/delete", name = "default_tag_delete", login = false)
public class DeleteApi extends AbstractApi<DatSetDefaultTagDeleteInput, MemberOutput> {

    @Autowired
    private DatSetDefaultTagContractService datSetDefaultTagContractService;

    @Override
    protected ApiResult<MemberOutput> handle(DatSetDefaultTagDeleteInput input) throws StatusCodeWithException {
        LOG.info("AddApi handle..");
        try {

            datSetDefaultTagContractService.deleteByTagId(input.getTagId());
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}
