package com.welab.wefe.manager.service.dto.tag;

/**
 * @author yuxin.zhang
 */
public class TagsDTO {
    private String tagName;
    private Long count;

    @Override
    public String toString() {
        return "{" +
                "tagName='" + tagName + '\'' +
                ", count=" + count +
                '}';
    }

    public TagsDTO(String tagName, Long count) {
        this.tagName = tagName;
        this.count = count;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
