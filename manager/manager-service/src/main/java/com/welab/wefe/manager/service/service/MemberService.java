package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.utils.PageUtils;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.manager.service.entity.Member;
import com.welab.wefe.manager.service.repository.MemberRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class MemberService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private MemberRepository mRepository;

    public void add(Member member) throws StatusCodeWithException {

        Optional<Member> opt = mRepository.findById(member.getId());
        if (opt.isPresent()) {
            throw new StatusCodeWithException(StatusCode.DATA_EXISTED, "该数据已存在");
        }

        mRepository.save(member);
    }

    public void upsert(Member member) throws StatusCodeWithException {

        Optional<Member> opt = mRepository.findById(member.getId());
        if (!opt.isPresent()) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "数据不存在");
        }

        Member dbEntity = opt.get();
        member.setPublicKey(dbEntity.getPublicKey());
        member.setCreatedTime(dbEntity.getCreatedTime());

        mRepository.save(member);
    }

    public Page<Member> findAll(int pageIndex, int pageSize, String id, String name) {

        Page<Member> page;
        Pageable pageable = mRepository.getPageable(pageIndex, pageSize);

        if (StringUtil.isNotEmpty(id)) {

            page = PageUtils.getPage(mRepository.findById(id));

        } else if (StringUtil.isNotEmpty(name)) {
            // 通过名称模糊查询
            page = mRepository.findAllByNameLike(pageable, mRepository.getLikedString(name));
        } else {
            // 全量查询
            page = mRepository.findAll(pageable);
        }

        return page;
    }


    /**
     * 翻页查询
     */
    public Page<Member> query(int pageIndex, int pageSize, String id, String name, boolean hidden, boolean freezed, boolean lostContact) {
        String paramHidden = String.valueOf(hidden ? 1 : 0);
        String paramFreezed = String.valueOf(freezed ? 1 : 0);
        String paramLostContact = String.valueOf(lostContact ? 1 : 0);

        Pageable pageable = mRepository.getPageable(pageIndex, pageSize);

        return mRepository.paging(pageable, id, name,paramHidden,paramFreezed,paramLostContact);
    }


    /**
     * 查询所有
     *
     * @param id 主键ID，如果为空，则查询所有
     * @return
     */
    public List<Member> findAll(String id) {
        long startTime = System.currentTimeMillis();
        List<Member> resultList = new ArrayList<>();
        if (StringUtil.isEmpty(id)) {
            List<Member> dbList = mRepository.findAll();
            if (CollectionUtils.isNotEmpty(dbList)) {
                resultList.addAll(dbList);
            }
        } else {
            Member member = mRepository.findById(id).orElse(null);
            if (null != member) {
                resultList.add(member);
            }
        }
        long endTime = System.currentTimeMillis();
        LOG.info("MemberService findAll spend:" + (endTime - startTime) + " ms");
        return resultList;
    }

}
