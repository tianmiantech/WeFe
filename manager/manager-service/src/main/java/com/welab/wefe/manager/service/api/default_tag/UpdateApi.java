package com.welab.wefe.manager.service.api.default_tag;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.entity.DataSetDefaultTag;
import com.welab.wefe.manager.service.service.DataSetDefaultTagService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 修改数据集默认标签
 */
@Api(path = "default_tag/update", name = "修改数据集默认标签")
public class UpdateApi extends AbstractApi<UpdateApi.Input, JObject> {

    @Autowired
    private DataSetDefaultTagService dataSetDefaultTagService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException {
        DataSetDefaultTag dataSetDefaultTag = dataSetDefaultTagService.findById(input.id);
        if(dataSetDefaultTag == null){
            return fail("无效的id");
        }
        dataSetDefaultTag.setTagName(input.tagName);
        dataSetDefaultTagService.save(dataSetDefaultTag);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String id;
        @Check(require = true)
        private String tagName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }
    }
}
