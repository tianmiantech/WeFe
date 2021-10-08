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

package com.welab.wefe.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zane
 */
public class ListUtil {

    /**
     * Moves the position of an element in a List
     */
    public static <T> void moveElement(List<T> list, int from, int to) {
        if (list == null || list.isEmpty()) {
            return;
        }

        if (from == to) {
            return;
        }

        T element = list.get(from);
        list.remove(from);
        list.add(to, element);
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));

        moveElement(list, 3, 1);
        System.out.println(list);
        moveElement(list, 2, 0);
        System.out.println(list);
    }
}
