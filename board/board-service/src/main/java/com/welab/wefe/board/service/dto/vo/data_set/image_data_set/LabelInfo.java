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

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane
 * @date 2021/11/12
 */
public class LabelInfo {
    public List<Item> list = new ArrayList<>();

    public static class Item {
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
    }

    public static class Point {
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
