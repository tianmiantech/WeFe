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
package com.welab.wefe.board.service.service.dataset.image_data_set;

import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.ListUtil;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zane
 * @date 2021/11/26
 */
@Service
public class ClassifyImageDataSetParser extends AbstractImageDataSetParser {

    /**
     * e.g:
     * 0 pink primrose
     * 1 hard-leaved pocket orchid
     * 2 canterbury bells
     * 3 sweet pea
     */
    private static final String LABEL_LIST_FILE_NAME = "label_list.txt";
    private static final Pattern LABEL_LIST_PATTERN = Pattern.compile("^\\s*(?<index>\\d+)\\s+(?<label>.+)\\s*$");
    /**
     * e.g:
     * jpg/image_06630.jpg 0
     * jpg/image_06638.jpg 1
     * jpg/image_06647.jpg 1
     * jpg/image_06646.jpg 2
     */
    private static final String TEST_LIST_FILE_NAME = "test_list.txt";
    private static final String TRAIN_LIST_FILE_NAME = "train_list.txt";
    private static final String VAL_LIST_FILE_NAME = "val_list.txt";
    private static final Pattern SAMPLE_LABEL_PATTERN = Pattern.compile("^(?<sample>.+)\\s+(?<index>\\d+)\\s*$");

    @Override
    protected List<ImageDataSetSampleMysqlModel> parseFilesToSamples(ImageDataSetMysqlModel dataSet, Map<String, File> imageFiles, Map<String, File> xmlFiles, Map<String, File> txtFiles) throws Exception {

        // label 与 索引的映射表(index : label)
        Map<Integer, String> labelIndexMap = getLabelIndexMap(txtFiles);

        // 样本与 label index 的映射表(image : index)
        Map<String, Integer> sampleLabelIndexMap = getSampleLabelIndexMap(txtFiles);

        List<ImageDataSetSampleMysqlModel> result = new ArrayList<>();
        Exception error = ListUtil.parallelEach(
                imageFiles.keySet(),
                key -> {
                    File imageFile = imageFiles.get(key);
                    Integer index = sampleLabelIndexMap.get(key);
                    String label = null;
                    if (index != null) {
                        label = labelIndexMap.get(index);
                    }

                    ImageDataSetSampleMysqlModel sample = createSample(dataSet, imageFile, label);
                    result.add(sample);
                }
        );

        if (error != null) {
            throw error;
        }

        return result;
    }

    /**
     * 解析标注文件，生成样本与 label 需要的映射表。
     * e.g:
     * jpg/image_06741.jpg 0
     * jpg/image_06762.jpg 0
     * jpg/image_05145.jpg 1
     * jpg/image_05137.jpg 1
     * jpg/image_05142.jpg 1
     *
     * @return sample : index
     */
    private Map<String, Integer> getSampleLabelIndexMap(Map<String, File> txtFiles) throws IOException {
        String[] fileNames = {TRAIN_LIST_FILE_NAME, TEST_LIST_FILE_NAME, VAL_LIST_FILE_NAME};
        Map<String, Integer> sampleLabelIndexMap = new HashMap<>();

        for (String fileName : fileNames) {
            File file = txtFiles.get(fileName);
            if (file == null) {
                continue;
            }

            Files.lines(file.toPath())
                    .forEach(x -> {
                        Matcher matcher = SAMPLE_LABEL_PATTERN.matcher(x);
                        if (matcher.find()) {
                            String sample = matcher.group("sample").trim();
                            Integer index = Convert.toInt(matcher.group("index"));
                            sample = new File(sample).getName();
                            sampleLabelIndexMap.put(sample, index);
                        }
                    });
        }

        return sampleLabelIndexMap;
    }

    /**
     * 解析 label_list.txt 文件，生成 label 与 索引的映射表。
     * e.g:
     * 0 pink primrose
     * 1 hard-leaved pocket orchid
     * 2 canterbury bells
     * 3 sweet pea
     *
     * @return index : label
     */
    private Map<Integer, String> getLabelIndexMap(Map<String, File> txtFiles) throws IOException {
        File labelListFile = txtFiles.get(LABEL_LIST_FILE_NAME);

        Map<Integer, String> labelIndexMap = new HashMap<>();
        if (labelListFile == null) {
            return labelIndexMap;
        }

        Files.lines(labelListFile.toPath())
                .forEach(x -> {
                    Matcher matcher = LABEL_LIST_PATTERN.matcher(x);
                    if (matcher.find()) {
                        Integer index = Convert.toInt(matcher.group("index"));
                        String label = matcher.group("label").trim();
                        labelIndexMap.put(index, label);
                    }
                });
        return labelIndexMap;
    }

    private ImageDataSetSampleMysqlModel createSample(ImageDataSetMysqlModel dataSet, File imageFile, String label) throws StatusCodeWithException, IOException {
        ImageDataSetSampleMysqlModel sample = super.createSampleModel(dataSet, imageFile);
        sample.setLabelList(label);
        sample.setLabeled(StringUtil.isNotEmpty(label));
        return sample;
    }


}
