package com.welab.wefe.manager.service.api.dataset;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.dataset.TagsDTO;
import com.welab.wefe.manager.service.dto.dataset.TagsQueryOutput;
import com.welab.wefe.manager.service.service.DatasetContractService;
import com.welab.wefe.manager.service.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author Jervis
 * @Date 2020-06-01
 **/
@Api(path = "data_set/tags/query", name = "查询标签")
public class DataSetTagsApi extends AbstractApi<DataSetTagsApi.Input, TagsQueryOutput> {
    @Autowired
    protected DatasetService mDatasetService;
    @Autowired
    protected DatasetContractService mDatasetContractService;

    @Override
    protected ApiResult<TagsQueryOutput> handle(Input input) throws StatusCodeWithException {
        List<TagsDTO> tagList = mDatasetService.getTagList(input.getTagName());

        TagsQueryOutput output = new TagsQueryOutput();
        output.setTagList(tagList);

        return success(output);
    }


    public static class Input extends BaseInput {
        private String memberId;
        private String tagName;
        private int pageIndex;
        private int pageSize;

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }
}
