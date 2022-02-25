<template>
    <div class="page register-wrapper">
        <div class="sign-box">
            <div class="logo">
                <img src="@assets/images/x-logo.png">
            </div>
            <h4 class="sign-title">注册新账号</h4>
            <h6 class="f14 mt20">
                已有账号?
                <router-link
                    :to="{
                        name: 'login',
                        query: { redirect: $route.query.redirect }
                    }"
                >
                    立即登录
                </router-link>
            </h6>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.prevent
            >
                <el-form-item
                    prop="account"
                    :rules="accountRules"
                >
                    <el-input
                        v-model.trim="form.account"
                        placeholder="用户名"
                        maxlength="32"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    prop="realname"
                    :rules="realnameRules"
                >
                    <el-input
                        v-model.trim="form.realname"
                        placeholder="姓名"
                        maxlength="32"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    prop="email"
                    :rules="emailRules"
                >
                    <el-input
                        v-model.trim="form.email"
                        placeholder="邮箱"
                        maxlength="32"
                        type="text"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    prop="password"
                    :rules="passwordRules"
                >
                    <el-input
                        v-model="form.password"
                        placeholder="密码"
                        type="password"
                        maxlength="30"
                        clearable
                    />
                </el-form-item>
                <el-form-item
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
                    />
                </el-form-item>
                <!-- <el-form-item
                    prop="code"
                    :rules="codeRules"
                >
                    <el-input
                        v-model="form.code"
                        placeholder="验证码"
                        class="form-code"
                        maxlength="10"
                        clearable
                    >
                        <template v-slot:append>
                            <div
                                class="code-img"
                                @click="getImgCode"
                            >
                                <img
                                    v-show="imgCode"
                                    class="code-img"
                                    :src="imgCode"
                                >
                            </div>
                        </template>
                    </el-input>
                </el-form-item> -->
                <div class="terms">
                    <el-checkbox v-model="form.terms">注册即代表同意我们的</el-checkbox>
                    《<span
                        class="el-link el-link--primary"
                        @click="termsDialog=true"
                    >隐私权限</span>》
                </div>
                <el-divider />
                <el-button
                    v-loading="submitting"
                    size="medium"
                    type="primary"
                    native-type="submit"
                    class="btn-submit ml10"
                    round
                    @click="submit"
                >
                    立即注册
                </el-button>
            </el-form>
        </div>
    </div>
</template>

<script>
    import md5 from 'js-md5';
    import { EMAILREG, PASSWORDREG } from '@js/const/reg';

    export default {
        data() {
            return {
                submitting: false,
                form:       {
                    terms:         false,
                    email:         '',
                    account:       '',
                    realname:      '',
                    password:      '',
                    passwordAgain: '',
                    code:          '',
                    key:           '',
                },
                imgCode:      '',
                termsDialog:  false,
                accountRules: [
                    {
                        required: true,
                        message:  '请输入用户名',
                    },
                ],
                emailRules: [
                    {
                        required: true,
                        message:  '请输入你的邮箱',
                        trigger:  'blur',
                    },
                    {
                        validator: this.emailFormat,
                        message:   '请输入正确的邮箱',
                        trigger:   'blur',
                    },
                ],
                realnameRules: [
                    {
                        required: true,
                        message:  '请输入你的姓名',
                        trigger:  'blur',
                    },
                    {
                        min:     2,
                        message: '您的姓名太短了',
                        trigger: 'blur',
                    },
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
                codeRules: [{ required: true, message: '请输入验证码' }],
            };
        },
        created() {
            // this.getImgCode();
        },
        methods: {
            async getImgCode() {
                const { code, data } = await this.$http.get('/user/captcha');

                if (code === 0) {
                    this.imgCode = data.image;
                    this.form.key = data.key;
                    this.form.code = '';
                }
            },
            emailFormat(rule, value, callback) {
                if (EMAILREG.test(value)) {
                    callback();
                } else {
                    callback(false);
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
                if (value === this.form.password) {
                    callback();
                } else {
                    callback(false);
                }
            },
            submit() {
                if (this.submitting) return;

                this.submitting = true;
                this.$refs['sign-form'].validate(async valid => {
                    if (valid) {
                        if (!this.form.terms) return this.$message.error('请先勾选隐私权限');
                        const { code } = await this.$http.post({
                            url:  '/user/register',
                            data: {
                                email:    this.form.email,
                                account:  this.form.account,
                                realname: this.form.realname,
                                password: md5(this.form.password),
                                key:      this.form.key,
                                code:     this.form.code,
                            },
                        });

                        if (code === 0) {
                            this.$router.replace({
                                name: 'login',
                            });
                            this.$message.success('恭喜, 注册成功. 请登录!');
                        } else {
                            // this.getImgCode();
                        }
                    } else {
                        // this.getImgCode();
                    }
                });
                this.submitting = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    @import './sign.scss';

    .register-wrapper{
        overflow: hidden;
        min-height: 100vh;
        padding-bottom: 40px;
        background: linear-gradient(90deg,#434343 0,#000);
    }
    .sign-box{
        padding-top: 0;
        margin: 170px auto 100px;
        background: #fff;
        border-radius: 3px;
        padding: 20px 50px;
    }
    .terms{
        color: #6C757D;
        padding-top: 10px;
    }
    .btn-submit{
        display: block;
        margin: 0 auto;
    }
</style>
