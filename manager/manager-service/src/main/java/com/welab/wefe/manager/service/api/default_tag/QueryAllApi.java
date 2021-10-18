package com.welab.wefe.manager.service.api.default_tag;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.PageableApiOutput;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.manager.service.entity.DataSetDefaultTag;
import com.welab.wefe.manager.service.entity.QueryDataSet;
import com.welab.wefe.manager.service.service.DataSetDefaultTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询数据集默认标签
 */
@Api(path = "default_tag/query", name = "查询数据集默认标签")
public class QueryAllApi extends AbstractApi<QueryAllApi.Input, JObject> {
    @Autowired
    protected DataSetDefaultTagService dataSetDefaultTagService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException {
        List<DataSetDefaultTag> list = dataSetDefaultTagService.findAll();
        return success(JObject.create("list", JObject.toJSON(list)));
    }

    protected PageableApiOutput<DataSetQueryOutput> getOutput(Page<QueryDataSet> page) {
        PageableApiOutput<DataSetQueryOutput> output = new PageableApiOutput<>(page);
        List<DataSetQueryOutput> list = new ArrayList<>();
        page.getContent().forEach(
                x -> {
                    DataSetQueryOutput outPut = new DataSetQueryOutput();
                    try {
                        BeanUtils.copyProperties(x, outPut);
                        list.add(outPut);
                    } catch (Exception e) {
                    }
                }
        );

        output.setList(list);
        return output;
    }


    public static class Input extends BaseInput {
        private String id;
        private String memberId;
        private String memberName;
        private String name;
        private Boolean containsY;
        private String tag;
        private int pageIndex;
        private int pageSize;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
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
