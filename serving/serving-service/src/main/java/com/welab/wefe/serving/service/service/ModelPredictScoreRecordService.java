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

package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.database.repository.ModelPredictScoreStatisticsRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hunter.zhao
 */
@Service
public class ModelPredictScoreRecordService {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private TableModelRepository modelRepository;

    @Autowired
    private ModelPredictScoreStatisticsRepository statisticsRepository;

}
