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
package com.welab.wefe.board.service.service.dataset;

import com.welab.wefe.board.service.database.entity.data_set.AbstractDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.base.RepositoryManager;
import com.welab.wefe.board.service.dto.vo.data_set.AbstractDataSetUpdateInputModel;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/10
 */
@Service
public abstract class AbstractDataSetService extends AbstractService {
    @Autowired
    protected UnionService unionService;

    public abstract AbstractDataSetMysqlModel findOneById(String dataSetId);

    protected abstract void beforeUpdate(AbstractDataSetMysqlModel model, AbstractDataSetUpdateInputModel input);

    /**
     * update data set info
     */
    public void update(AbstractDataSetUpdateInputModel input) throws StatusCodeWithException {
        AbstractDataSetMysqlModel model = findOneById(input.getDataSetId());
        if (model == null) {
            return;
        }

        model.setUpdatedBy(input);
        model.setName(input.getName());
        model.setDescription(input.getDescription());
        model.setPublicMemberList(input.getPublicMemberList());
        model.setPublicLevel(input.getPublicLevel());
        model.setTags(standardizeTags(input.getTags()));
        handlePublicMemberList(model);

        beforeUpdate(model, input);
        RepositoryManager.get(model.getClass()).save(model);

        if (model.getClass() == ImageDataSetMysqlModel.class) {
            unionService.uploadImageDataSet((ImageDataSetMysqlModel) model);
            CacheObjects.refreshImageDataSetTags();
        } else if (model.getClass() == DataSetMysqlModel.class) {
            unionService.uploadTableDataSet((DataSetMysqlModel) model);
            CacheObjects.refreshTableDataSetTags();
        }

    }

    /**
     * Standardize the tag list
     */
    public String standardizeTags(List<String> tags) {
        if (tags == null) {
            return "";
        }

        tags = tags.stream()
                // Remove comma(,，)
                .map(x -> x.replace(",", "").replace("，", ""))
                // Remove empty elements
                .filter(x -> !StringUtil.isEmpty(x))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Concatenate into a string, add a comma before and after it to facilitate like query.
        return "," + StringUtil.join(tags, ',') + ",";

    }

    /**
     * Process the list of visible members
     * <p>
     * When the scene is visible to the specified members, automatically add itself is also visible.
     */
    public void handlePublicMemberList(AbstractDataSetMysqlModel model) {

        // When the PublicLevel is PublicWithMemberList, if list contains yourself,
        // you will be removed, and union will handle the data that you must be visible.
        if (model.getPublicLevel() == DataSetPublicLevel.PublicWithMemberList) {
            String memberId = CacheObjects.getMemberId();


            if (model.getPublicMemberList().contains(memberId)) {
                String list = model.getPublicMemberList()
                        .replace(memberId, "")
                        .replace(",,", ",");

                model.setPublicMemberList(list);
            }
        }

    }
}
