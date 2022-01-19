<template>
    <el-card class="page">
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
                />
            </el-form-item>
            <el-button
                type="primary"
                @click="submit"
            >
                提交
            </el-button>
        </el-form>
    </el-card>
</template>

<script>
    import md5 from 'js-md5';
    import { mapGetters } from 'vuex';
    import { PASSWORDREG } from '@js/const/reg';
    import { baseLogout } from '@src/router/auth';

    export default {
        data() {
            return {
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
        methods: {
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
                        const oldPassword = [
                            this.userInfo.phone_number,
                            this.form.old_password,
                            this.userInfo.phone_number,
                            this.userInfo.phone_number.substr(0, 3),
                            this.form.old_password.substr(this.form.old_password.length - 3),
                        ].join('');
                        const password = [
                            this.userInfo.phone_number,
                            this.form.new_password,
                            this.userInfo.phone_number,
                            this.userInfo.phone_number.substr(0, 3),
                            this.form.new_password.substr(this.form.new_password.length - 3),
                        ].join('');

                        const { code } = await this.$http.post({
                            url:  '/account/update_password',
                            data: {
                                oldPassword: md5(oldPassword),
                                newPassword: md5(password),
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
