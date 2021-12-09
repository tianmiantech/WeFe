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


/**
 * @author hunter.zhao
 */
public class PsiMeta {
    byte[][] bytes;

    public byte[][] getBytes() {
        return bytes;
    }

    public void setBytes(byte[][] bytes) {
        this.bytes = bytes;
    }

    public static PsiMeta of(byte[][] bs) {
        PsiMeta psiMeta = new PsiMeta();
        psiMeta.bytes = bs;
        return psiMeta;
    }
}
