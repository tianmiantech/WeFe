<template>
    <div class="page">
        <div class="sign-box">
            <div class="logo">
                <img src="@assets/images/x-logo.png">
            </div>
            <h2 class="sign-title mt20">修改登录密码</h2>
            <el-divider />
            <el-form
                ref="form"
                :model="form"
                @submit.prevent
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
                <div class="sign-action mt20">
                    <el-button
                        type="primary"
                        style="width:120px;"
                        @click="submit"
                    >
                        提交
                    </el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script>
    import md5 from 'js-md5';
    import { mapGetters } from 'vuex';
    import { PASSWORDREG } from '@js/const/reg';

    export default {
        data() {
            return {
                form: {
                    account:         '',
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
        /* created() {
            const { account } = this.$route.query;

            this.form.account = account;
        }, */
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
                            this.$message.success('密码修改成功! 请重新登录');
                            this.$router.replace({
                                name: 'login',
                            });
                        }
                    }
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    @import './sign.scss';
</style>
