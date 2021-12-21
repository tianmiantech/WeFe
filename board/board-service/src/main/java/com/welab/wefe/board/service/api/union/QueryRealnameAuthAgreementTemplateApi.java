package com.welab.wefe.board.service.api.union;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Api(path = "union/realname/auth/agreement/template/query", name = "realname auth agreement template query")
public class QueryRealnameAuthAgreementTemplateApi extends AbstractApi<AbstractApiInput, Object> {
    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<Object> handle(AbstractApiInput input) throws StatusCodeWithException, IOException {
        JSONObject result = unionService.realnameAuthAgreementTemplateQuery();
        return unionApiResultToBoardApiResult(result);
    }
}
