package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.manager.service.dto.dataset.DataSetDetailOutput;
import com.welab.wefe.manager.service.dto.dataset.DataSetInput;
import com.welab.wefe.manager.service.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.manager.service.entity.DataSet;
import org.mapstruct.Mapper;

/**
 * @Author Jervis
 * @Date 2020-05-28
 **/
@Mapper
public interface DataSetMapper {

    DataSet transfer(DataSetInput input);

    DataSetQueryOutput transfer(DataSet entity);

    DataSetDetailOutput transferDetail(DataSet entity);

}
