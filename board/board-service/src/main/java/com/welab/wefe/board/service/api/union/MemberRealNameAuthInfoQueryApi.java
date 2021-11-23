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

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/3
 */
@Api(path = "union/member/realname/authInfo/query", name = "apply realname auth")
public class MemberRealNameAuthInfoQueryApi extends AbstractApi<AbstractApiInput, JSONObject> {
    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<JSONObject> handle(AbstractApiInput input) throws StatusCodeWithException, IOException {
        JSONObject result = unionService.realnameAuthInfoQuery();
        return unionApiResultToBoardApiResult(result);
    }
}
