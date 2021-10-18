package com.welab.wefe.manager.service.api.default_tag;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.service.DataSetDefaultTagService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 删除数据集默认标签
 */
@Api(path = "default_tag/delete", name = "删除数据集默认标签")
public class DeleteApi extends AbstractApi<DeleteApi.Input, JObject> {

    @Autowired
    private DataSetDefaultTagService dataSetDefaultTagService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException {
        dataSetDefaultTagService.deleteById(input.id);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }
}
