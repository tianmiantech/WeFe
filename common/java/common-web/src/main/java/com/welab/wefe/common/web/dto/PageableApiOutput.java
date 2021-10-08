/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Jervis
 **/
public class PageableApiOutput<T extends AbstractApiOutput> extends AbstractApiOutput {

    private int totalPage;
    private long total;
    private List<T> list;

    public PageableApiOutput(Page page) {
        setTotalPage(page.getTotalPages());
        setTotal(page.getTotalElements());
    }

    public PageableApiOutput(long total, int pageSize, List<T> list) {
        setTotal(total);
        setTotalPage((int) ((total + pageSize - 1) / pageSize));
        setList(list);
    }


    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

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
}
