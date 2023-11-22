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

package com.welab.wefe.common.data.mongodb.dto;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
public class PageOutput<T> {
    private Integer pageIndex;
    private Long total;
    private Integer pageSize;
    private Integer totalPage;
    private List<T> list;

    public PageOutput(Integer pageIndex, Long total, Integer pageSize, List<T> list) {
        this.pageIndex = pageIndex;
        this.total = total;
        this.pageSize = pageSize;
        this.totalPage = (int) ((total + pageSize - 1) / pageSize);
        this.list = list;
    }

    public PageOutput(Integer pageIndex, Long total, Integer pageSize, Integer totalPage, List<T> list) {
        this.pageIndex = pageIndex;
        this.total = total;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.list = list;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
