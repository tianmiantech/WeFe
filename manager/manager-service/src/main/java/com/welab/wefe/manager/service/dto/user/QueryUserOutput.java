package com.welab.wefe.manager.service.dto.user;

import com.welab.wefe.common.web.dto.AbstractApiOutput;

/**
 * @author yuxin.zhang
 */
public class QueryUserOutput extends AbstractApiOutput {
    private String userId;
    private String account;
    private String realname;
    private String email;
    /**
     * 是否是超级管理员;超级管理员通常是第一个创建并初始化系统的那个人
     */
    private boolean superAdminRole;
    /**
     * 是否是管理员;管理员有更多权限，比如设置 member 是否对外可见。
     */
    private boolean adminRole;

    /**
     * 是否可用
     */
    private boolean enable = true;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSuperAdminRole() {
        return superAdminRole;
    }

    public void setSuperAdminRole(boolean superAdminRole) {
        this.superAdminRole = superAdminRole;
    }

    public boolean isAdminRole() {
        return adminRole;
    }

    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
