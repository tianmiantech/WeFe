package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.dto.dataset.TagsDTO;
import com.welab.wefe.manager.service.entity.DataSet;
import com.welab.wefe.manager.service.entity.DataSetForTag;
import com.welab.wefe.manager.service.entity.QueryDataSet;
import com.welab.wefe.manager.service.repository.DataSetForTagRepository;
import com.welab.wefe.manager.service.repository.DataSetRepository;
import com.welab.wefe.manager.service.repository.MemberRepository;
import com.welab.wefe.manager.service.repository.QueryDataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Jervis
 * @Date 2020-05-27
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class DatasetService {

    @Autowired
    private DataSetRepository mDataSetRepository;
    @Autowired
    private QueryDataSetRepository mQueryDataSetRepository;
    @Autowired
    private MemberRepository mMemberRepository;
    @Autowired
    private DataSetForTagRepository mDataSetForTagRepository;

    public void upsert(DataSet dataset) throws StatusCodeWithException {
        boolean memberExists = mMemberRepository.existsById(dataset.getMemberId());

        if (!memberExists) {
            throw new StatusCodeWithException("成员ID不存在", StatusCode.INVALID_USER);
        }

        Optional<DataSet> dbEntity = mDataSetRepository.findById(dataset.getId());

        if (dbEntity.isPresent()) {
            dataset.setCreatedTime(dbEntity.get().getCreatedTime());
            mDataSetRepository.deleteById(dataset.getId());
        }

        mDataSetRepository.save(dataset);

    }

    public Page<QueryDataSet> findList(int pageIndex, int pageSize, String name, String memberName, Boolean containsY, String tag, String curMemberId, String id, String memberId) {

        Page<QueryDataSet> page;
        Pageable pageable = mDataSetRepository.getPageableForAtQuery(pageIndex, pageSize);
        String containsYStr = null;
        if (containsY != null) {
            containsYStr = containsY ? "1" : "0";
        }
        page = mQueryDataSetRepository.find(pageable, name, memberName, containsYStr, tag, curMemberId, id, memberId);

        return page;
    }

    public Page<QueryDataSet> findListMgr(int pageIndex, int pageSize, String name, String memberName, Boolean containsY, String tag, String id, String memberId) {

        Page<QueryDataSet> page;

        Pageable pageable = mDataSetRepository.getPageableForAtQuery(pageIndex, pageSize);
        String containsYStr = null;
        if (containsY != null) {
            containsYStr = containsY ? "1" : "0";
        }
        page = mQueryDataSetRepository.findAll(pageable, name, memberName, containsYStr, tag, id, memberId);

        return page;
    }

    public void deleteById(String dataSetId) {
        mDataSetRepository.deleteById(dataSetId);
    }

    /**
     * 翻页查询
     */
    public List<TagsDTO> getTagList(String tagName) {

        List<DataSetForTag> tagList = mDataSetForTagRepository.findByTags(tagName);

        Map<String, Long> map = new HashMap<>();
        // 把数据库记录的tags字段进行拆分，去重，排序
        tagList
                .stream()
                .map(DataSetForTag::getTags)
                .flatMap(tag -> Arrays.stream(tag.split(",")))
                .filter(StringUtil::isNotEmpty)
                .filter(word -> {
                    if (StringUtil.isEmpty(tagName)) {
                        return true;
                    } else {
                        return word.contains(tagName);
                    }
                })
                .collect(Collectors.toList())
                .forEach(word -> map.put(word, map.getOrDefault(word, 0L) + 1));


        List<TagsDTO> list = new ArrayList<>();
        map.forEach((word, count) -> list.add(new TagsDTO(word, count)));

        return list;

    }


    public DataSet findByIdMgr(String id) {
        Optional<DataSet> optional = mDataSetRepository.findById(id);
        return optional.orElse(null);
    }

}
