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

package com.welab.wefe.board.service.dto.base;

import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.util.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
public class PagingOutput<T> extends AbstractApiOutput {
    private long total;
    private List<T> list;

    public static <T> PagingOutput<T> of(long total, List<T> list) {
        PagingOutput<T> output = new PagingOutput<>();
        output.total = total;
        output.list = list;
        return output;
    }

    /**
     * 创建 PagingOutput 的便捷方法
     * <p>
     * 该方法会将数据库实体自动映射为 DTO
     */
    public static <T> PagingOutput<T> of(long total, List<?> list, Class<T> clazz) {
        PagingOutput<T> output = new PagingOutput<>();
        output.total = total;

        if (list != null) {

            output.list = list
                    .parallelStream()
                    .map(x -> ModelMapper.map(x, clazz))
                    .collect(Collectors.toList());
        }

        return output;
    }

    //region getter/setter

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }


    //endregion
}
