package com.welab.wefe.manager.service.api.member;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.PageableApiOutput;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.member.MemberQueryOutput;
import com.welab.wefe.manager.service.entity.Member;
import com.welab.wefe.manager.service.mapper.MemberMapper;
import com.welab.wefe.manager.service.service.MemberService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
@Api(path = "member/query", name = "查询成员")
public class QueryApi extends AbstractApi<QueryApi.Input, PageableApiOutput<MemberQueryOutput>> {
    @Autowired
    protected MemberService mMemberService;

    protected MemberMapper mMapper = Mappers.getMapper(MemberMapper.class);

    @Override
    protected ApiResult<PageableApiOutput<MemberQueryOutput>> handle(Input input) throws StatusCodeWithException {
        try {
            Page<Member> page = mMemberService.query(
                    input.getPageIndex(),
                    input.getPageSize(),
                    input.getId(),
                    input.getName(),
                    input.isHidden(),
                    input.isFreezed(),
                    input.isLostContact()
            );

            PageableApiOutput<MemberQueryOutput> output = new PageableApiOutput<>(page);

            List<MemberQueryOutput> list = page.getContent().stream()
                    .map(mMapper::transfer)
                    .collect(Collectors.toList());

            output.setList(list);

            return success(output);
        } catch (Exception e) {
            LOG.error("分页查询成员信息失败:", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "分页查询成员信息失败");
        }

    }

    public static class Input extends BaseInput {
        private String id;
        private String name;

        private boolean lostContact;
        private boolean hidden;
        private boolean freezed;

        private int pageIndex;
        private int pageSize;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public boolean isLostContact() {
            return lostContact;
        }

        public void setLostContact(boolean lostContact) {
            this.lostContact = lostContact;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public boolean isFreezed() {
            return freezed;
        }

        public void setFreezed(boolean freezed) {
            this.freezed = freezed;
        }


        //endregion
    }

}
