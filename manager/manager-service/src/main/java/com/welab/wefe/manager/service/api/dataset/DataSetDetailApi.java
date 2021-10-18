package com.welab.wefe.manager.service.api.dataset;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.dataset.DataSetDetailOutput;
import com.welab.wefe.manager.service.entity.DataSet;
import com.welab.wefe.manager.service.mapper.DataSetMapper;
import com.welab.wefe.manager.service.service.DatasetContractService;
import com.welab.wefe.manager.service.service.DatasetService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author Jervis
 * @Date 2020-06-10
 **/
@Api(path = "data_set/detail", name = "获取数据集详情")
public class DataSetDetailApi extends AbstractApi<DataSetDetailApi.Input, DataSetDetailOutput> {

    @Autowired
    protected DatasetService mDatasetService;
    @Autowired
    protected DatasetContractService mDatasetContractService;

    protected DataSetMapper mDataSetMapper = Mappers.getMapper(DataSetMapper.class);

    @Override
    protected ApiResult<DataSetDetailOutput> handle(Input input) throws StatusCodeWithException {
        DataSet dataSet = mDatasetService.findByIdMgr(input.getId());
        return success(getOutput(dataSet));
    }

    protected DataSetDetailOutput getOutput(DataSet dataSet) {
        if (dataSet == null) {
            return null;
        }

        DataSetDetailOutput detail = mDataSetMapper.transferDetail(dataSet);
        return detail;
    }

    public static class Input extends BaseInput {
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
