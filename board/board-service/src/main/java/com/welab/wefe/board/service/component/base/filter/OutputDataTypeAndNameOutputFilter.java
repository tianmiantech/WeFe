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

package com.welab.wefe.board.service.component.base.filter;

import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.model.FlowGraphNode;

/**
 * 同时限定数据类型和名称
 *
 * @author zane.luo
 */
public class OutputDataTypeAndNameOutputFilter implements OutputItemFilterFunction {

    private final IODataType type;
    private final String name;

    public OutputDataTypeAndNameOutputFilter(IODataType type, String name) {
                this.type = type;
        this.name = name;
    }

    @Override
    public boolean apply(FlowGraphNode node, OutputItem outputItem) {
        return type.equals(outputItem.getDataType()) && name.equals(outputItem.getName());
    }

}
