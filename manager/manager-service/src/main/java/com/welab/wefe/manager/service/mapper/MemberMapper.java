package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.manager.service.dto.member.MemberInput;
import com.welab.wefe.manager.service.dto.member.MemberQueryOutput;
import com.welab.wefe.manager.service.entity.Member;
import org.mapstruct.Mapper;

/**
 * @Author Jervis
 * @Date 2020-05-29
 **/
@Mapper
public interface MemberMapper {

    Member transfer(MemberInput input);

    MemberQueryOutput transfer(Member entity);
}
