/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.dto.vo.data_set.image_data_set;

import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/12
 */
public class LabelInfo extends AbstractCheckModel {
    @Check(name = "图片中标记的对象列表")
    public List<Item> objects = new ArrayList<>();

    public List<String> labelList() {
        List<String> list = new ArrayList<>();
        if (objects == null || objects.isEmpty()) {
            return list;
        }

        list = objects
                .stream()
                .filter(x -> StringUtil.isNotEmpty(x.label))
                .map(x -> x.label)
                .collect(Collectors.toList());

        return list;
    }

    /**
     * 是否包含标注信息
     */
    public boolean isLabeled() {
        if (objects == null || objects.isEmpty()) {
            return false;
        }
        return objects.stream().anyMatch(x -> StringUtil.isNotEmpty(x.label));
    }

    public static class Item extends AbstractCheckModel {

        public String label;
        /**
         * 是否：难以识别的物体
         */
        public boolean difficult = false;
        /**
         * 是否：遮挡超过15-20％
         */
        public boolean truncated = false;
        public List<Point> points;

        public Item() {
        }

        public Item(String label, int minX, int minY, int maxX, int maxY) {
            this.label = label;
            this.points = new ArrayList<>();
            this.points.add(new Point(minX, minY));
            this.points.add(new Point(maxX, maxY));
        }

        public Object toLabelObject() {
            Object object = new Object();
            LabelInfo.Point point1 = points.get(0);
            LabelInfo.Point point2 = points.get(1);
            object.bndbox = new Bndbox(point1.x, point1.y, point2.x, point2.y);
            object.name = label;
            object.difficult = difficult ? 1 : 0;
            object.truncated = truncated ? 1 : 0;
            return object;
        }
    }

    public static class Point extends AbstractCheckModel {
        public int x;
        public int y;

        public Point() {
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
