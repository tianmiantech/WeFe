package com.welab.wefe.manager.service.api.dataset;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.dataset.DataSetOutput;
import com.welab.wefe.manager.service.entity.DataSet;
import com.welab.wefe.manager.service.service.DataSetMemberPermissionContractService;
import com.welab.wefe.manager.service.service.DatasetContractService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
@Api(path = "data_set/put", name = "更新数据集")
public class PutApi extends AbstractApi<PutApi.Input, DataSetOutput> {
    @Autowired
    protected DatasetContractService mDataSetContractService;


    @Autowired
    private DataSetMemberPermissionContractService mDataSetMemberPermissionContractService;

    @Override
    protected ApiResult<DataSetOutput> handle(Input input) throws StatusCodeWithException {

        DataSet dataSet = new DataSet();
        BeanUtils.copyProperties(input, dataSet);
        dataSet.setContainsY(input.containsY ? 1 : 0);

        String publicMemberList = input.publicMemberList;

        if (DataSetPublicLevel.OnlyMyself.name().equals(input.publicLevel)) {
            // 不对外开放
            mDataSetMemberPermissionContractService.deleteByDataSetId(dataSet.getId());
            dataSet.setPublicLevel(input.publicLevel);

        } else if (DataSetPublicLevel.Public.name().equals(input.publicLevel)) {
            // 对所有成员开放
            mDataSetMemberPermissionContractService.deleteByDataSetId(dataSet.getId());
            dataSet.setPublicLevel(input.publicLevel);

        } else if (DataSetPublicLevel.PublicWithMemberList.name().equals(input.publicLevel)) {
            // 对部分成员开放
            mDataSetMemberPermissionContractService.save(dataSet.getId(), publicMemberList);
            dataSet.setPublicLevel(input.publicLevel);

        } else {
            throw new StatusCodeWithException("无效的publicLevel", StatusCode.SYSTEM_ERROR);
        }

        mDataSetContractService.upsert(dataSet);

        return success();
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String id;
        @Check(require = true)
        private String name;
        @Check(require = true)
        private String memberId;
        private Boolean containsY;
        private Long rowCount;
        private Integer columnCount;
        private String columnNameList;
        private Integer featureCount;
        private String featureNameList;
        @Check(require = true)
        private String publicLevel;
        private String publicMemberList;
        private int usageCountInJob;
        private int usageCountInFlow;
        private int usageCountInProject;
        private String description;
        @Check(require = true)
        private String tags;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }

        public Long getRowCount() {
            return rowCount;
        }

        public void setRowCount(Long rowCount) {
            this.rowCount = rowCount;
        }

        public Integer getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(Integer columnCount) {
            this.columnCount = columnCount;
        }

        public String getColumnNameList() {
            return columnNameList;
        }

        public void setColumnNameList(String columnNameList) {
            this.columnNameList = columnNameList;
        }

        public Integer getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(Integer featureCount) {
            this.featureCount = featureCount;
        }

        public String getFeatureNameList() {
            return featureNameList;
        }

        public void setFeatureNameList(String featureNameList) {
            this.featureNameList = featureNameList;
        }

        public String getPublicLevel() {
            return publicLevel;
        }

        public void setPublicLevel(String publicLevel) {
            this.publicLevel = publicLevel;
        }

        public String getPublicMemberList() {
            return publicMemberList;
        }

        public void setPublicMemberList(String publicMemberList) {
            this.publicMemberList = publicMemberList;
        }

        public int getUsageCountInJob() {
            return usageCountInJob;
        }

        public void setUsageCountInJob(int usageCountInJob) {
            this.usageCountInJob = usageCountInJob;
        }

        public int getUsageCountInFlow() {
            return usageCountInFlow;
        }

        public void setUsageCountInFlow(int usageCountInFlow) {
            this.usageCountInFlow = usageCountInFlow;
        }

        public int getUsageCountInProject() {
            return usageCountInProject;
        }

        public void setUsageCountInProject(int usageCountInProject) {
            this.usageCountInProject = usageCountInProject;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }
    }
}
