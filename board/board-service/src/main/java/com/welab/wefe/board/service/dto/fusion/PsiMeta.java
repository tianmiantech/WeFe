package com.welab.wefe.board.service.dto.fusion;

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


import com.welab.wefe.common.util.Base64Util;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

/**
 * @author hunter.zhao
 */
public class PsiMeta {
    List<String> bs;

    public List<String> getBs() {
        return bs;
    }

    public void setBs(List<String> bs) {
        this.bs = bs;
    }

    public static PsiMeta of(byte[][] bs) {
        PsiMeta psiMeta = new PsiMeta();
        List<String> bitStr = Lists.newArrayList();
        for (int i = 0; i < bs.length; i++) {
            bitStr.add(Base64Util.encode(bs[i]));
        }
        psiMeta.bs = bitStr;
        return psiMeta;
    }
}
