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

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataresource.DataResourceQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.DataResourcePublicLevel;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.api.dataresource.DataSetTagsApi;
import com.welab.wefe.union.service.api.dataresource.DeleteApi;
import com.welab.wefe.union.service.api.dataresource.HiddenApi;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceDetailInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryInput;
import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.TagsDTO;
import com.welab.wefe.union.service.service.contract.*;
import com.welab.wefe.union.service.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataResourceService {
    @Autowired
    protected DataResourceMongoReop dataResourceMongoReop;
    @Autowired
    private DataResourceContractService dataResourceContractService;
    @Autowired
    private ImageDataSetContractService imageDataSetContractService;
    @Autowired
    private TableDataSetContractService tableDataSetContractService;
    @Autowired
    private BloomFilterContractService bloomFilterContractService;

    /**
     * 分页查询
     */
    public PageOutput<ApiDataResourceQueryOutput> query(ApiDataResourceQueryInput input) {
        PageOutput<DataResourceQueryOutput> pageOutput = dataResourceMongoReop.findOneByDataResourceId(MapperUtil.transferDataResourceInput(input));
        List<ApiDataResourceQueryOutput> list = pageOutput.getList().stream()
                .map(x -> {
                    if (x.getDataResourceType().compareTo(DataResourceType.TableDataSet) == 0) {
                        return MapperUtil.transferDetailTableDataSet(x);
                    } else if (x.getDataResourceType().compareTo(DataResourceType.ImageDataSet) == 0) {
                        return MapperUtil.transferDetailImageDataSet(x);
                    } else if (x.getDataResourceType().compareTo(DataResourceType.BloomFilter) == 0) {
                        return MapperUtil.transferDetailBloomFilter(x);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageOutput<>(pageOutput.getPageIndex(), pageOutput.getTotal(), pageOutput.getPageSize(), pageOutput.getTotalPage(), list);
    }


    /**
     * 详情
     */
    public ApiDataResourceQueryOutput detail(ApiDataResourceDetailInput input) throws StatusCodeWithException {
        DataResourceQueryOutput dataResourceQueryOutput = null;
        String targetTableName = null;
        switch (input.getDataResourceType()) {
            case BloomFilter:
                targetTableName = MongodbTable.Union.BLOOM_FILTER;
                break;
            case TableDataSet:
                targetTableName = MongodbTable.Union.TABLE_DATASET;
                break;
            case ImageDataSet:
                targetTableName = MongodbTable.Union.IMAGE_DATASET;
                break;

            default:
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "dataResourceType");
        }
        dataResourceQueryOutput = dataResourceMongoReop.findOneByDataResourceId(input.getDataResourceId(), targetTableName);
        if (dataResourceQueryOutput == null) {
            return null;
        }
        switch (dataResourceQueryOutput.getDataResourceType()) {
            case TableDataSet:
                return MapperUtil.transferDetailTableDataSet(dataResourceQueryOutput);
            case ImageDataSet:
                return MapperUtil.transferDetailImageDataSet(dataResourceQueryOutput);
            case BloomFilter:
                return MapperUtil.transferDetailBloomFilter(dataResourceQueryOutput);
            default:
                return MapperUtil.transferDetailDefault(dataResourceQueryOutput);
        }
    }

    /**
     * 删除
     */
    public void delete(DeleteApi.Input input) throws StatusCodeWithException {
        DataResource dataResource = dataResourceMongoReop.findByDataResourceId(input.getDataResourceId());
        if (null == dataResource) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "资源不存在");
        }
        if (!dataResource.getMemberId().equals(input.curMemberId)) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
        }
        switch (dataResource.getDataResourceType()) {
            case BloomFilter:
                bloomFilterContractService.delete(input.getDataResourceId());
                break;
            case TableDataSet:
                tableDataSetContractService.delete(input.getDataResourceId());
                break;
            case ImageDataSet:
                imageDataSetContractService.delete(input.getDataResourceId());
                break;
            default:
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "无效的资源类型");
        }
        dataResourceContractService.delete(input.getDataResourceId());
    }

    /**
     * 隐藏
     */
    public void hidden(HiddenApi.Input input) throws StatusCodeWithException {
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.curMemberId);
        if (null == dataResource) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "资源不存在");
        }
        dataResource.setPublicLevel(DataResourcePublicLevel.OnlyMyself.name());
        dataResourceContractService.update(dataResource);
    }


    /**
     * 查询标签
     */
    public List<TagsDTO> queryTags(DataSetTagsApi.Input input) {
        List<String> tagsList = dataResourceMongoReop.findByDataResourceType(input.getDataResourceType());
        Map<String, Long> tagGroupMap = tagsList.stream()
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.groupingBy(String::trim, Collectors.counting()));

        return tagGroupMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(x -> new TagsDTO(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }
}
