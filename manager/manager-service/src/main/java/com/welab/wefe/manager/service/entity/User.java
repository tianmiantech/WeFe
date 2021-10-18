package com.welab.wefe.manager.service.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractUniqueIDEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
@Entity
@Table(name = "user")
public class User extends AbstractUniqueIDEntity {

    private String account;
    private String password;
    private String nickname;
    private String email;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
