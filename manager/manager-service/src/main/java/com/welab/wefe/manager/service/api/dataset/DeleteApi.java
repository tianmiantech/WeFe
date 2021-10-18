package com.welab.wefe.manager.service.api.dataset;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.dataset.DataSetOutput;
import com.welab.wefe.manager.service.service.DataSetMemberPermissionContractService;
import com.welab.wefe.manager.service.service.DatasetContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * @Author Jervis
 * @Date 2020-05-29
 **/
@Api(path = "data_set/delete", name = "删除数据集")
public class DeleteApi extends AbstractApi<DeleteApi.Input, DataSetOutput> {
    @Autowired
    protected DatasetContractService datasetContractService;

    @Autowired
    private DataSetMemberPermissionContractService mDataSetMemberPermissionContractService;

    @Override
    protected ApiResult<DataSetOutput> handle(Input input) throws StatusCodeWithException {
        try {
            datasetContractService.deleteById(input.getId());
            mDataSetMemberPermissionContractService.deleteByDataSetId(input.getId());
        } catch (EmptyResultDataAccessException e) {
            LOG.warn("删除不存在的数据");
        }
        return success();
    }

    public static class Input extends BaseInput {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
