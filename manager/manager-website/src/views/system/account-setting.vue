<template>
    <el-card class="page">
        <el-row :gutter="60">
            <el-col :xs="12" :sm="12" :md="10" :lg="10" :xl="10">
                <h3 class="mb20">用户信息:</h3>
                <el-form style="max-width: 300px">
                    <el-form-item
                        label="姓名"
                        required
                    >
                        <el-input v-model.trim="accountInfo.realname" />
                    </el-form-item>
                    <el-form-item
                        label="邮箱"
                        required
                    >
                        <el-input v-model.trim="accountInfo.email" />
                    </el-form-item>
                    <el-button
                        type="primary"
                        :disabled="!accountInfo.realname || !accountInfo.email"
                        @click="updateUserInfo"
                    >
                        提交
                    </el-button>
                </el-form>
            </el-col>
            <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12">
                <h3 class="mb20">修改登录密码:</h3>
                <el-form
                    ref="form"
                    :model="form"
                    style="max-width: 300px"
                >
                    <el-form-item
                        label="旧密码"
                        prop="old_password"
                        :rules="old_password"
                    >
                        <el-input
                            v-model="form.old_password"
                            type="password"
                            @paste.prevent
                            @copy.prevent
                            @contextmenu.prevent
                        />
                    </el-form-item>
                    <el-form-item
                        label="新密码"
                        prop="new_password"
                        :rules="new_password"
                    >
                        <el-input
                            v-model="form.new_password"
                            type="password"
                            @paste.prevent
                            @copy.prevent
                            @contextmenu.prevent
                        />
                        <PasswordStrength
                            ref="password-strength"
                            :password="form.new_password"
                        />
                    </el-form-item>
                    <el-form-item
                        label="再次确认新密码"
                        prop="repeat_password"
                        :rules="repeat_password"
                    >
                        <el-input
                            v-model="form.repeat_password"
                            type="password"
                            @paste.prevent
                            @copy.prevent
                            @contextmenu.prevent
                        />
                    </el-form-item>
                    <el-button
                        type="primary"
                        :disabled="!form.old_password || !form.new_password || !form.repeat_password"
                        @click="submit"
                    >
                        提交
                    </el-button>
                </el-form>
            </el-col>
        </el-row>
    </el-card>
</template>

<script>
    import md5 from 'js-md5';
    import { mapGetters } from 'vuex';
    import { PASSWORDREG } from '@js/const/reg';
    import { baseLogout } from '@src/router/auth';

    export default {
        inject: ['refresh'],
        data() {
            return {
                accountInfo: {
                    realname: '',
                    email:    '',
                },
                form: {
                    old_password:    '',
                    new_password:    '',
                    repeat_password: '',
                },
                old_password: [
                    { required: true, message: '必填!' },
                ],
                new_password: [
                    { required: true, message: '必填!' },
                    {
                        validator: this.passwordType,
                        message:   '密码至少8位, 需包含数字,字母,特殊字符任意组合',
                        trigger:   'blur',
                    },
                ],
                repeat_password: [
                    { required: true, message: '请再次输入密码' },
                    {
                        min:     8,
                        message: '密码至少8位',
                        trigger: 'blur',
                    },
                    {
                        validator: this.passwordCheck,
                        message:   '两次密码不一致',
                        trigger:   'blur',
                    },
                ],
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.accountInfo.realname = this.userInfo.realname;
            this.accountInfo.email = this.userInfo.email;
        },
        methods: {
            async updateUserInfo(event) {
                if (this.accountInfo.realname === '') {
                    return this.$message.error('姓名不能为空!');
                } else if(this.accountInfo.email === '') {
                    return this.$message.error('邮箱不能为空!');
                }

                const { code } = await this.$http.post({
                    url:      '/user/update',
                    data:     this.accountInfo,
                    btnState: {
                        target: event,
                    },
                });

                if(code === 0) {
                    const user = {
                        ...this.userInfo,
                        ...this.accountInfo,
                    };

                    this.$store.commit('UPDATE_USERINFO', user);
                    this.$message.success('操作成功!');
                    this.refresh();
                }
            },
            passwordType(rule, value, callback) {
                if (PASSWORDREG.test(value)) {
                    callback();
                } else {
                    callback(false);
                }
            },
            passwordCheck(rule, value, callback) {
                if (value === this.form.new_password) {
                    callback();
                } else {
                    callback(false);
                }
            },
            submit() {
                this.$refs['form'].validate(async valid => {
                    if(valid) {
                        if(this.$refs['password-strength'].pwStrength < 3) {
                            return this.$message.error('密码强度太弱');
                        }

                        const { code } = await this.$http.post({
                            url:  '/user/change/password',
                            data: {
                                oldPassword: md5(this.form.old_password),
                                newPassword: md5(this.form.new_password),
                            },
                        });

                        if(code === 0) {
                            baseLogout({ redirect: false });
                            this.$message.success('密码修改成功! 请重新登录');
                        }
                    }
                });
            },
        },
    };
</script>
