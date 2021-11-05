package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
public class UserRoleChangeInput extends AbstractApiInput {
    private String userId;
    private boolean adminRole;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAdminRole() {
        return adminRole;
    }

    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }
}
