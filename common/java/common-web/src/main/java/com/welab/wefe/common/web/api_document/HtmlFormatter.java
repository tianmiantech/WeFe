/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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
package com.welab.wefe.common.web.api_document;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api_document.model.ApiItem;
import com.welab.wefe.common.web.api_document.model.ApiParam;
import com.welab.wefe.common.web.api_document.model.ApiParamField;

/**
 * @author zane
 * @date 2021/12/3
 */
public class HtmlFormatter extends AbstractApiDocumentFormatter {
    StringBuilder str = new StringBuilder(2048);

    public HtmlFormatter() {
        setHeader();
        setToc();
    }

    private void setToc() {
        str.append("<ol>");
        for (ApiItem api : API_LIST) {
            str.append("<li>");
            str.append(
                    "<a href='#" + api.id + "'>" +
                            api.path +
                            (StringUtil.isEmpty(api.name) ? "" : "(" + api.name + ")") +
                            "</a>"
            );
            str.append("</li>");
        }
        str.append("</ol>");
    }

    @Override
    public String contentType() {
        return "text/html";
    }

    @Override
    protected void formatApiItem(ApiItem api) {
        str.append("<div class=\"api-item\" value='" + api.group() + "'>\n" +
                "<h3 id='" + api.id + "' class=\"api-name\">" +
                api.path +
                (StringUtil.isEmpty(api.name) ? "" : "(" + api.name + ")") +
                "</h3>\n");

        if (StringUtil.isNotEmpty(api.desc)) {
            str
                    .append("<p class=\"api-desc\">")
                    .append(api.desc)
                    .append("</p>\n");
        }

        str
                .append("<div class='api-params'>")
                .append(getParams("入参", api.input))
                .append(getParams("响应", api.output))
                .append("</div>")
                .append("</div>");
    }

    @Override
    protected void formatGroupItem(String name) {
        str.append("<h2 class=\"group-title\"  value='" + name + "'>").append(name).append("</h2>");
    }

    @Override
    protected String getOutput() {
        setFooter();
        return str.toString();
    }

    private String getParams(String title, ApiParam params) {
        if (params == null) {
            return "";
        }
        String output = "</br>" +
                "<table class=\"api-param-table\">\n" +
                "<caption>" +
                title +
                "</caption>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th style=\"width:200px\">name</th>\n" +
                "<th style=\"width:200px\">type</th>\n" +
                "<th style=\"width:50px\">require</th>\n" +
                "<th style=\"width:20%\">comment</th>\n" +
                "<th style=\"\">desc</th>\n" +
                "<th style=\"width:200px\">regex</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n";
        for (ApiParamField item : params.fields) {
            output += "<tr>\n" +
                    "<td>" + item.name + "</td>\n" +
                    "<td>" + item.typeName + "</td>\n" +
                    "<td style=\"text-align: center;\">" + item.require + "</td>\n" +
                    "<td>" + item.comment + "</td>\n" +
                    "<td>" + item.desc + "</td>\n" +
                    "<td>" + item.regex + "</td>\n" +
                    "</tr>\n";
        }
        output += "</tbody>\n" +
                "</table>\n";
        return output;
    }

    private void setHeader() {
        str.append("<!DOCTYPE html>\n" +
                "<html lang=\"zh-cn\">\n" +
                "\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>API Documentation</title>\n" +
                "<style>\n" +
                "h1 {\n" +
                "text-align: center;\n" +
                "}\n" +
                "\n" +
                ".group-title {\n" +
                "border-bottom: 1px solid #999;\n" +
                "color: red;\n" +
                "margin-top: 50px;\n" +
                "cursor: pointer;\n" +
                "}\n" +
                "\n" +
                ".api-item {\n" +
                "border: 1px solid #000;\n" +
                "padding: 8px;\n" +
                "margin-top: 25px;\n" +
                "}\n" +
                "\n" +
                ".api-name {\n" +
                "color: black;\n" +
                "background-color: #ccc;\n" +
                "padding: 15px 15px;\n" +
                "margin: -8px -8px 0 -8px;\n" +
                "}\n" +
                "\n" +
                ".api-desc {\n" +
                "text-indent: 30px;\n" +
                "}\n" +
                "\n" +
                ".api-params {\n" +
                "width: 100%;\n" +
                "display: none;" +
                "}\n" +
                ".api-param-table {\n" +
                "width: 100%;\n" +
                "border-collapse: collapse;\n" +
                "}\n" +
                ".api-params caption{\n" +
                "font-weight: bold;\n" +
                "color: blue;\n" +
                "}\n" +
                "\n" +
                ".api-params th,\n" +
                ".api-params td {\n" +
                "border: 1px solid blue;\n" +
                "padding: 3px 8px;\n" +
                "}\n" +
                ".goto-top {\n" +
                "    position: fixed;\n" +
                "    right: 10px;\n" +
                "    bottom: 50px;\n" +
                "    border: 1px solid #333;\n" +
                "    padding: 3px;\n" +
                "    height: 40px;\n" +
                "    width: 40px;\n" +
                "    line-height: 40px;\n" +
                "    text-align: center;\n" +
                "    font-weight: bold;\n" +
                "text-decoration: none;" +
                "    z-index: 1024;" +
                "}" +
                ".goto-top:visited {\n" +
                "  color: black;\n" +
                "}" +
                "</style>\n" +
                "</head><body>\n" +
                "<h1>API Documentation</h1>");
    }

    private void setFooter() {
        str.append(
                "<a href='#' class=\"goto-top\">TOP</a>" +
                        "<script src='https://code.jquery.com/jquery-3.6.0.min.js'></script>" +
                        "<script>\n" +
                        "    $(\".group-title\").click(function(){\n" +
                        "        var group = $(this).attr(\"value\");\n" +
                        "        $(\".api-item[value=\" + group + \"]\").toggle();\n" +
                        "    });\n" +
                        "    $(\".api-name\").click(function(){\n" +
                        "        $(this).parent().find(\".api-params\").toggle();\n" +
                        "    });" +
                        "</script>" +
                        "</body>\n" +
                        "</html>"
        );
    }

}
