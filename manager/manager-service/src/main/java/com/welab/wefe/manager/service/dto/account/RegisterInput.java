package com.welab.wefe.manager.service.dto.account;

import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
public class RegisterInput extends AbstractApiInput {
    private String userId;
    private String account;
    private String password;
    private String nickname;
    private String email;

}
