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

package com.welab.wefe.board.service.api.member;

import com.welab.wefe.board.service.service.SystemInitializeService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileType;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author aaron.li
 **/
@Api(path = "member/update_logo", name = "update member logo")
public class UpdateMemberLogoApi extends AbstractNoneOutputApi<UpdateMemberLogoApi.Input> {
    @Autowired
    private SystemInitializeService systemInitializeService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        systemInitializeService.updateMemberLogo(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(
                name = "成员logo",
                require = true,
                blockReactionaryKeyword = false,
                blockSqlInjection = false,
                blockXss = false
        )
        private String memberLogo;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            String base64 = StringUtil.substringAfter(memberLogo, "base64,");
            byte[] bytes = Base64.decodeBase64(base64);
            if (!FileType.isImage(bytes)) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的图片格式");
            }

            // 对图片文件进行缩放，过滤掉内部可能包含的木马内容。
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                Thumbnails
                        .of(new ByteArrayInputStream(bytes))
                        .scale(1)
                        .toOutputStream(output);
            } catch (IOException e) {
                StatusCode
                        .PARAMETER_VALUE_INVALID
                        .throwException("图片已损坏：" + e.getMessage());
            }

            memberLogo = "data:image/jpeg;base64," + Base64.encodeBase64String(output.toByteArray());

        }


        public String getMemberLogo() {
            return memberLogo;
        }

        public void setMemberLogo(String memberLogo) {
            this.memberLogo = memberLogo;
        }
    }
}
