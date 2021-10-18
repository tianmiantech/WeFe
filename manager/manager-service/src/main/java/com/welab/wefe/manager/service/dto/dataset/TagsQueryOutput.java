package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.web.dto.AbstractApiOutput;

import java.util.List;

/**
 * @Author Jervis
 * @Date 2020-06-01
 **/
public class TagsQueryOutput extends AbstractApiOutput {

    private List<TagsDTO> tagList;

    public List<TagsDTO> getTagList() {
        return tagList;
    }

    public void setTagList(List<TagsDTO> tagList) {
        this.tagList = tagList;
    }
}
