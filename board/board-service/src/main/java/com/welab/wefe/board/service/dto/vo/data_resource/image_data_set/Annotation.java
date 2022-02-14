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
package com.welab.wefe.board.service.dto.vo.data_resource.image_data_set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.welab.wefe.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/PaddlePaddle/PaddleDetection/blob/release/2.3/docs/tutorials/PrepareDataSet.md
 *
 * @author zane
 * @date 2021/11/8
 */
@XStreamAlias("annotation")
public class Annotation {
    public String folder;
    public String filename;
    public String path;
    public Source source;
    public Size size;
    /**
     * 暂时没用到，先使用默认值。
     */
    public int segmented = 0;
    @XStreamImplicit
    public List<Object> objectList;

    public LabelInfo toLabelInfo() {
        LabelInfo labelInfo = new LabelInfo();

        if (objectList != null) {
            for (Object object : objectList) {
                LabelInfo.Item item = new LabelInfo.Item(
                        object.name,
                        object.bndbox.xmin,
                        object.bndbox.ymin,
                        object.bndbox.xmax,
                        object.bndbox.ymax
                );

                labelInfo.objects.add(item);
            }

        }

        return labelInfo;
    }

    public List<String> getLabelList() {
        List<String> list = new ArrayList<>();
        if (objectList == null) {
            return list;
        }

        for (Object object : objectList) {
            if (StringUtil.isNotEmpty(object.name)) {
                list.add(object.name);
            }
        }
        return list;
    }
}
