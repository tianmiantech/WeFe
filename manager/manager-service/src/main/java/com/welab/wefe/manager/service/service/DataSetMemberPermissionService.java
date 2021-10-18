package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.manager.service.entity.DataSetMemberPermission;
import com.welab.wefe.manager.service.repository.DataSetMemberPermissionRepository;
import com.welab.wefe.manager.service.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Jervis
 * @Date 2020-05-29
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class DataSetMemberPermissionService {

    @Autowired
    private DataSetMemberPermissionRepository mRepository;
    @Autowired
    private MemberRepository mMemberRepository;

    public void save(String dataSetId, String publicMemberList) throws StatusCodeWithException {

        String[] memberIds = publicMemberList.split(",");

        validateMembersLocal(memberIds);
        validateMembersRemote(memberIds);

        List<DataSetMemberPermission> list = Arrays.stream(memberIds)
                .map(memberId -> new DataSetMemberPermission(dataSetId, memberId))
                .collect(Collectors.toList());

        // 如果有新的权限列表，那就删除之前的权限
        deleteByDataSetId(dataSetId);

        mRepository.saveAll(list);

    }

    public void deleteByDataSetId(String dataSetId) {
        mRepository.deleteByDataSetId(dataSetId);
    }

    private void validateMembersLocal(String[] memberIds) throws StatusCodeWithException {

        boolean hasEmptyMemberId = Arrays.stream(memberIds).anyMatch(String::isEmpty);

        if (hasEmptyMemberId) {
            throw new StatusCodeWithException("存在空的成员ID", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    private void validateMembersRemote(String[] memberIds) throws StatusCodeWithException {

        for (String memberId : memberIds) {
            if (!mMemberRepository.existsById(memberId)) {
                throw new StatusCodeWithException("存在错误的成员ID : " + memberId, StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }
}
