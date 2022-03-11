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
package com.welab.wefe.common.http.download;

/**
 * @author zane
 * @date 2022/3/8
 */
public enum DownloadStatus {
    /**
     * 尚未开始
     */
    prepare,
    /**
     * 下载中
     */
    downloading,
    /**
     * 下载完成
     */
    completed,
    /**
     * 下载失败
     */
    failed,
    /**
     * 取消下载
     */
    canceled
}
