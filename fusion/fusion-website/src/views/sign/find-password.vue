<template>
    <div class="full-page">
        <div class="sign-box">
            <div class="logo">
                <img src="@assets/images/x-logo.png">
            </div>
            <h2 class="sign-title mt30">找回密码</h2>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.native.prevent
            >
                <el-form-item
                    label="注册时的手机号"
                    prop="phone"
                    :rules="phoneRules"
                >
                    <el-input
                        v-model="form.phone"
                        maxlength="11"
                        type="tel"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    label="短信验证码"
                    :rules="codeRules"
                >
                    <el-input
                        v-model="form.smsCode"
                        class="form-code"
                        maxlength="6"
                        clearable
                    >
                        <template v-slot:append>
                            <el-button
                                type="primary"
                                class="smsCount text-c"
                                :disabled="form.phone.length !== 11 || smsCount < 121"
                                @click="getSmsCode"
                            >
                                {{ smsCount > 120 ? '发送验证码' : `${smsCount}秒后重发` }}
                            </el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item
                    label="新密码"
                    prop="password"
                    :rules="passwordRules"
                >
                    <el-input
                        v-model="form.password"
                        type="password"
                        maxlength="30"
                        clearable
                        @paste.native.prevent
                        @copy.native.prevent
                        @contextmenu.native.prevent
                    />
                </el-form-item>
                <el-form-item
                    label="确认新密码"
                    prop="passwordAgain"
                    :rules="passwordAgain"
                    clearable
                >
                    <el-input
                        v-model="form.passwordAgain"
                        placeholder="再次输入密码"
                        type="password"
                        maxlength="30"
                        clearable
                        @paste.native.prevent
                        @copy.native.prevent
                        @contextmenu.native.prevent
                    />
                </el-form-item>
                <el-divider />
                <div class="sign-action">
                    <router-link
                        class="float-left"
                        :to="{name: 'login'}"
                    >
                        立即登录
                    </router-link>
                    <el-button
                        v-loading="submitting"
                        style="width:80px;"
                        type="primary"
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
    import { PASSWORDREG } from '@js/const/reg';

    export default {
        data() {
            return {
                form: {
                    phone:         '',
                    smsCode:       '',
                    password:      '',
                    passwordAgain: '',
                },
                smsCount:   121,
                phoneRules: [
                    { required: true, message: '请输入你的手机号' },
                    {
                        validator: (rule, value, callback) => {
                            if (/^1[3-9]\d{9}/.test(value)) {
                                callback();
                            } else {
                                callback(new Error('请输入正确的手机号'));
                            }
                        },
                        trigger: 'blur',
                    },
                ],
                codeRules: [
                    { required: true, message: '请输入短信验证码' },
                ],
                passwordRules: [
                    {
                        required: true,
                        message:  '请输入你的密码',
                    },
                    {
                        validator: this.passwordType,
                        message:   '密码至少8位, 需包含数字,字母,特殊字符任意组合',
                        trigger:   'blur',
                    },
                ],
                passwordAgain: [
                    {
                        required: true,
                        message:  '请再次输入密码',
                        trigger:  'blur',
                    },
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
                submitting: false,
            };
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
                if (value === this.form.password) {
                    callback();
                } else {
                    callback(false);
                }
            },
            async getSmsCode(event) {
                const { code } = await this.$http.post({
                    url:  '/union/send_forget_password_sms_code',
                    data: {
                        phoneNumber: this.form.phone,
                    },
                });

                if(code === 0) {
                    this.smsCount--;
                    const timer = setInterval(() => {
                        this.smsCount--;
                        if (this.smsCount < 0) {
                            clearInterval(timer);
                            this.smsCount = 121;
                        }
                    }, 1000);
                }
            },
            submit() {
                if (this.submitting) return;

                this.submitting = true;
                this.$refs['sign-form'].validate(async valid => {
                    if(valid) {
                        const password = [
                            this.form.phone,
                            this.form.password,
                            this.form.phone,
                            this.form.phone.substr(0, 3),
                            this.form.password.substr(this.form.password.length - 3),
                        ].join('');
                        const { code } = await this.$http.post({
                            url:  '/account/forget_password',
                            data: {
                                phoneNumber:         this.form.phone,
                                smsVerificationCode: this.form.smsCode,
                                password:            md5(password),
                            },
                        });

                        if(code === 0) {
                            this.$message.success('密码更新成功! 请重新登录!');
                            this.$store.commit('UPDATE_USERINFO', null);

                            this.$router.replace({
                                name: 'login',
                            });
                        }
                    } else {
                        this.$message.error(valid.message);
                    }
                    this.submitting = false;
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    @import './sign.scss';

    .full-page {
        height:100vh;
        background: #fff;
    }
    .el-form-item{
        .smsCount{
            width: 106px;
            padding-left:27px;
            line-height: 28px;
            background-color: var(--el-color-primary);
            color:#fff;
            &.is-disabled{
                background: none;
                color: var(--el-button-disabled-font-color);
            }
        }
    }
</style>
