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

package com.welab.wefe.board.service.api.data_output_info;

import com.welab.wefe.board.service.service.modelexport.ModelExportService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.wefe.enums.ModelExportLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author aaron.li
 **/
@Controller
public class ModelExportController {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ModelExportService modelExportService;


    @RequestMapping(value = "/data_output_info/model_export", produces = "application/json; charset=UTF-8")
    public void download(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        PrintWriter out = null;
        String jobId = null;
        String modelFlowNodeId = null;
        String role = null;
        String language = null;
        String token = null;
        try {
            jobId = httpServletRequest.getParameter("jobId");
            modelFlowNodeId = httpServletRequest.getParameter("modelFlowNodeId");
            role = httpServletRequest.getParameter("role");
            language = httpServletRequest.getParameter("language");
            token = httpServletRequest.getParameter("token");
            httpServletResponse.setCharacterEncoding("UTF-8");
            out = httpServletResponse.getWriter();
            CurrentAccount.Info info = CurrentAccount.get(token);
            if (null == info) {
                out.write(JObject.create().append("code", StatusCode.PARAMETER_VALUE_INVALID.getCode())
                        .append("message", "未登录").toString());
                out.flush();
                return;
            }
            httpServletResponse.setHeader("content-type", "text/html;charset=UTF-8");
            if (StringUtil.isEmpty(jobId) || StringUtil.isEmpty(modelFlowNodeId)) {
                out.write(JObject.create().append("code", StatusCode.PARAMETER_VALUE_INVALID.getCode())
                        .append("message", "参数jobId不能为空").toString());
                out.flush();
                return;
            }
            if (StringUtil.isEmpty(modelFlowNodeId)) {
                out.write(JObject.create().append("code", StatusCode.PARAMETER_VALUE_INVALID.getCode())
                        .append("message", "参数modelFlowNodeId不能为空").toString());
                out.flush();
                return;
            }

            if (StringUtil.isEmpty(role)) {
                out.write(JObject.create().append("code", StatusCode.PARAMETER_VALUE_INVALID.getCode())
                        .append("message", "参数role不能为空").toString());
                out.flush();
                return;
            }

            if (StringUtil.isEmpty(language)) {
                out.write(JObject.create().append("code", StatusCode.PARAMETER_VALUE_INVALID.getCode())
                        .append("message", "参数language不能为空").toString());
                out.flush();
                return;
            }

            // export
            String exportResult = modelExportService.handle(jobId, modelFlowNodeId, role, language);

            httpServletResponse.setHeader("content-disposition", "attachment;filename=" + "model." + ModelExportLanguage.getLanguageSuffix(language));
            httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            out.write(exportResult);
            out.flush();
        } catch (Exception e) {
            LOG.error("Job id: " + jobId + " model export exception：", e);
            if (null != out) {
                out.write(JObject.create().append("code", StatusCode.SYSTEM_ERROR.getCode())
                        .append("message", "模型导出异常:" + e.getMessage()).toString());
                out.flush();
            }
        } finally {
            if (null != out) {
                out.close();
            }
        }

    }


}
