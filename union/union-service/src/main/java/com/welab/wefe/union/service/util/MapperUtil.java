/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.union.service.util;

import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryInput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.member.MemberServiceQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.api.dataresource.dataset.image.PutApi;
import com.welab.wefe.union.service.api.dataresource.dataset.nomal.QueryApi;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.bloomfilter.ApiBloomFilterQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.dataset.image.ApiImageDataSetQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.ApiDataSetQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.ApiTableDataSetQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.DataSetDetailOutput;
import com.welab.wefe.union.service.dto.member.ApiMemberServiceQueryOutput;
import com.welab.wefe.union.service.dto.member.MemberQueryOutput;

import java.util.Date;

public class MapperUtil {
    public static ApiBloomFilterQueryOutput transferDetailBloomFilter(DataResourceQueryOutput dataResourceQueryOutput) {
        ApiBloomFilterQueryOutput out = ModelMapper.map(dataResourceQueryOutput, ApiBloomFilterQueryOutput.class);
        out.setExtraData(JObject.create(dataResourceQueryOutput.getBloomFilter()).toJavaObject(ApiBloomFilterQueryOutput.ExtraData.class));
        out.setCreatedTime(toDate(dataResourceQueryOutput.getCreatedTime()));
        out.setUpdatedTime(toDate(dataResourceQueryOutput.getUpdatedTime()));
        return out;
    }

    public static ApiImageDataSetQueryOutput transferDetailImageDataSet(DataResourceQueryOutput dataResourceQueryOutput) {
        ApiImageDataSetQueryOutput out = ModelMapper.map(dataResourceQueryOutput, ApiImageDataSetQueryOutput.class);
        out.setExtraData(JObject.create(dataResourceQueryOutput.getImageDataSet()).toJavaObject(ApiImageDataSetQueryOutput.ExtraData.class));
        out.setCreatedTime(toDate(dataResourceQueryOutput.getCreatedTime()));
        out.setUpdatedTime(toDate(dataResourceQueryOutput.getUpdatedTime()));
        return out;
    }

    public static ApiTableDataSetQueryOutput transferDetailTableDataSet(DataResourceQueryOutput dataResourceQueryOutput) {
        ApiTableDataSetQueryOutput out = ModelMapper.map(dataResourceQueryOutput, ApiTableDataSetQueryOutput.class);
        out.setExtraData(JObject.create(dataResourceQueryOutput.getTableDataSet()).toJavaObject(ApiTableDataSetQueryOutput.ExtraData.class));
        out.setCreatedTime(toDate(dataResourceQueryOutput.getCreatedTime()));
        out.setUpdatedTime(toDate(dataResourceQueryOutput.getUpdatedTime()));
        return out;
    }

    public static ApiDataResourceQueryOutput transferDetailDefault(DataResourceQueryOutput dataResourceQueryOutput) {
        ApiDataResourceQueryOutput out = ModelMapper.map(dataResourceQueryOutput, ApiDataResourceQueryOutput.class);
        out.setCreatedTime(toDate(dataResourceQueryOutput.getCreatedTime()));
        out.setUpdatedTime(toDate(dataResourceQueryOutput.getUpdatedTime()));
        return out;
    }

    public static DataResourceQueryInput transferDataResourceInput(ApiDataResourceQueryInput entity) {
        DataResourceQueryInput out = ModelMapper.map(entity, DataResourceQueryInput.class);
        return out;
    }

    public static MemberQueryOutput transferMember(Member member) {
        MemberQueryOutput out = ModelMapper.map(member, MemberQueryOutput.class);
        out.setId(member.getMemberId());
        out.setCreatedTime(toDate(member.getCreatedTime()));
        out.setUpdatedTime(toDate(member.getUpdatedTime()));
        return out;
    }

    public static DataSetDetailOutput transferDataSetDetail(DataSet dataSet) {
        DataSetDetailOutput out = ModelMapper.map(dataSet, DataSetDetailOutput.class);
        out.setId(dataSet.getDataSetId());
        out.setCreatedTime(toDate(dataSet.getCreatedTime()));
        out.setUpdatedTime(toDate(dataSet.getUpdatedTime()));
        return out;
    }

    public static ApiDataSetQueryOutput transferToDataSetOutput(DataSetQueryOutput entity) {
        ApiDataSetQueryOutput out = ModelMapper.map(entity, ApiDataSetQueryOutput.class);
        out.setId(entity.getDataSetId());
        out.setUsageCountInJob(StringUtil.isEmpty(entity.getUsageCountInJob()) ? 0
                : Integer.parseInt(entity.getUsageCountInJob()));
        out.setUsageCountInFlow(StringUtil.isEmpty(entity.getUsageCountInFlow()) ? 0
                : Integer.parseInt(entity.getUsageCountInFlow()));
        out.setUsageCountInProject(StringUtil.isEmpty(entity.getUsageCountInProject()) ? 0
                : Integer.parseInt(entity.getUsageCountInProject()));
        out.setCreatedTime(toDate(entity.getCreatedTime()));
        out.setUpdatedTime(toDate(entity.getUpdatedTime()));
        return out;
    }

    public static DataSetQueryInput transferToDataSetInput(QueryApi.Input entity) {
        DataSetQueryInput input = ModelMapper.map(entity, DataSetQueryInput.class);
        input.setDataSetId(entity.getId());
        input.setPageIndex(entity.getPageIndex() == null ? 0 : entity.getPageIndex());
        input.setPageSize(entity.getPageSize() == null ? 10 : entity.getPageSize());
        return input;
    }

    public static ApiMemberServiceQueryOutput transferToMemberServiceQueryOutput(MemberServiceQueryOutput memberServiceQueryOutput) {
        ApiMemberServiceQueryOutput out = ModelMapper.map(memberServiceQueryOutput, ApiMemberServiceQueryOutput.class);
        out.setCreatedTime(toDate(memberServiceQueryOutput.getCreatedTime()));
        out.setUpdatedTime(toDate(memberServiceQueryOutput.getUpdatedTime()));
        return out;
    }

    public static ImageDataSet transferPutInputToImageDataSet(PutApi.Input input) {
        ImageDataSet out = ModelMapper.map(input, ImageDataSet.class);
        out.setLabelCompleted(String.valueOf(input.isLabelCompleted() ? 1 : 0));
        out.setCreatedTime(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()));
        out.setUpdatedTime(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()));
        return out;
    }

    public static DataResource transferPutInputToDataResource(PutApi.Input input) {
        DataResource out = ModelMapper.map(input, DataResource.class);
        out.setMemberId(input.curMemberId);
        return out;
    }


    public static Date toDate(String dateStr) {
        if (StringUtil.isEmpty(dateStr)) {
            return null;
        }
        return DateUtil.fromString(dateStr, DateUtil.YYYY_MM_DD_HH_MM_SS2);
    }

    public static void main(String[] args) {
        String created_time = "2021-09-02 14:40:33";

        Date date = DateUtil.fromString(created_time, DateUtil.YYYY_MM_DD_HH_MM_SS2);

        String formatDate = DateUtil.toString(date, DateUtil.YYYY_MM_DD_HH_MM_SS2);
        System.out.println(formatDate);
    }
}
