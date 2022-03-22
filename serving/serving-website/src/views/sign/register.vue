<template>
    <div class="page register-wrapper">
        <div class="sign-box">
            <div class="logo">
                <img src="../../assets/images/logo.png">
            </div>
            <h4 class="sign-title">注册新账号</h4>
            <h6 class="to-regist mt20">
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
                @submit.native.prevent
            >
                <el-form-item
                    prop="phone"
                    :rules="phoneRules"
                >
                    <el-input
                        v-model.trim="form.phone"
                        placeholder="手机号"
                        maxlength="11"
                        type="tel"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    prop="nickname"
                    :rules="nicknameRules"
                >
                    <el-input
                        v-model.trim="form.nickname"
                        placeholder="姓名"
                        maxlength="40"
                        type="text"
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
                        maxlength="60"
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
                        @paste.native.prevent
                        @copy.native.prevent
                        @contextmenu.native.prevent
                    />
                    <PasswordStrength
                        ref="password-strength"
                        :password="form.password"
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
                        @paste.native.prevent
                        @copy.native.prevent
                        @contextmenu.native.prevent
                    />
                </el-form-item>
                <el-form-item
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
                        <template slot="append">
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
                </el-form-item>
                <div class="terms">
                    <el-checkbox v-model="form.terms">注册即代表同意我们的</el-checkbox>
                    《<span
                        class="el-link el-link--primary"
                        @click="termsDialog=true"
                    >隐私政策</span>》
                </div>
                <el-divider />
                <el-button
                    round
                    type="primary"
                    size="middle"
                    native-type="submit"
                    class="btn-submit ml10"
                    @click="submit"
                >
                    立即注册
                </el-button>
            </el-form>
        </div>
        <el-dialog
            :visible.sync="termsDialog"
            title="隐私政策"
            append-to-body
            destroy-on-close
            width="60%"
            top="7vh"
            :show-close="false"
            :close-on-click-modal="false"
            class="terms_dialog"
        >
            <div class="terms_text">
                <p><span>我们致力于</span>保护您在使用我们网站时所提供的私隐、私人资料以及个人的资料(统称“个人资料”)，使我们在收集、使用、储存和传送个人资料方面符合 (与个人资料私隐有关的法律法规)及消费者保护方面的最高标准。 为确保您对我们在处理个人资料上有充分信心，您切要详细阅读及理解隐私政策的条文。</p>
                <p><span>特别是您一旦使用我们的网站，将被视为接受、同意、承诺和确认；您在自愿下连同所需的同意向我们披露个人资料；您会遵守本隐私政策的全部条款和限制；您在我们的网站上作登记、资料会被收集；您同意日后我们对隐私政策的任何修改；您同意我们的分公司、附属公司、雇员、就您可能会感兴趣的产品和服务与您联络(除非您已经表示不想收到该等讯息)。被收集的个人资料的种类经您的同意，我们会收集、管理和监控个人资料。</span></p>
                <p><span>为了向您提供我们的各项服务，您需要提供个人资料信息，</span>其中包括个人资料和不具名的资料，包括但不限于：<span>个人资料（您的电话号码、电子邮箱地址）。</span></p>
                <p><span>收集个人资料及不具名的资料的目的及用途如下:</span></p>
                <ol>
                    <li>通过我们的网站向您提供我们的各项服务；</li>
                    <li>当您使用我们的网站时，能辨认以及确认您的身份；</li>
                    <li>让您使用我们的网站时得到为您而设的服务；</li>
                    <li>我们的顾客服务人员有需要时可以与您联系；</li>
                    <li>统计我们网站使用量的数据；</li>
                    <li>让您在使用我们网站时更方便；</li>
                    <li>为改进我们的产品、服务及网站内容而进行市场研究调查；</li>
                    <li>为我们搞的活动、市场销售和推广计划收集资料；</li>
                    <li>遵守法律、政府和监管机关的规定，包括但不限于对个人资料披露及通知的规定；</li>
                    <li>就我们提供的各项服务、分析、核对或审查您的信用、付款或地位；</li>
                    <li>处理在您要求下的任何付款指示，直接扣帐或信用安排；</li>
                    <li>使您能运作您的账户以及使我们能从账户支取尚欠的服务费；</li>
                </ol>
                <p>您提供给我们的个人资料及不具名资料，只保留到搜集的目的已达到的时候，除非应适用的法律法规之规定而继续保留。 <span>个人资料的拥有权及披露在我们网站上所搜集的一切资料都由我们所拥有，不会出租或出售给任何无关的第三方。</span></p>
                <p>您有权： 查询我们是否持有您的任何个人资料；接达我们所持有的您的个人资料；要求我们更正任何不正确的个人资料；不时地征询有关我们所持有的<span>个人资料的性质，政策和执行方法</span>；然而在法律允许的极端有限的情况下，<span>我们可以不允许您接达您的个人资料</span>，例如：如您接达及得到您个人资料可能会对您有危险；当您的个人资料可能会影响一项正在进行的调查；当您的个人资料涉及到法庭程序，并且可能受到发现的限制；当您的个人资料涉及一项商业上敏感的决策过程；当另外一個人的个人资料也包含在同一份记录中；若您欲接达或更正个人资料，或索取有关个人资料的政策、执行方法和被持有的个人资料的种类，应致函到我们的下列的地址；要求接达或更正资料可能要付合理的处理费用；安全保管您的密码，除了我们致力确保您的个人资料存放和处理的安全外，<span>您不应向任何人披露您的登录密码或帐户资料，以保护您的个人资料</span>。</p>
                <p>每当您登录我们网站时，尤其是当您使用他人的电脑或者是公共的互联网终端机时，请记着操作完毕后一定要点击<span>退出</span>。</p>
                <p>您的努力和协助对于我们保护您的个人资料绝对有帮助。</p>
                <p>隐私政策的修改：本隐私政策可以不时(无需事先向您通知)被修改。任何对隐私政策的修改都会刊登在我们网站上。</p>
            </div>
            <template
                #footer
                class="dialog-footer"
            >
                <el-button
                    type="primary"
                    @click="termsDialog = false"
                >
                    我知道了
                </el-button>
            </template>
        </el-dialog>
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
                phone:         '',
                nickname:      '',
                password:      '',
                passwordAgain: '',
                code:          '',
                key:           '',
            },
            imgCode:     '',
            termsDialog: false,
            phoneRules:  [
                {
                    required: true,
                    message:  '请输入你的手机号',
                },
                {
                    validator: (rule, value, callback) => {
                        if (/^1[3-9]\d{9}/.test(value)) {
                            callback();
                        } else {
                            callback(false);
                        }
                    },
                    message: '请输入正确的手机号',
                    trigger: 'blur',
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
            nicknameRules: [
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
        this.getImgCode();
    },
    methods: {
        async getImgCode() {
            const { code, data } = await this.$http.get('/account/captcha');

            if (code === 0) {
                this.imgCode = data.image;
                this.form.key = data.key;
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
                    if(this.$refs['password-strength'].pwStrength < 3) {
                        return this.$message.error('密码强度太弱');
                    }
                    const password = [
                        this.form.phone,
                        this.form.password,
                        this.form.phone,
                        this.form.phone.substr(0, 3),
                        this.form.password.substr(this.form.password.length - 3),
                    ].join('');
                    const { code } = await this.$http.post({
                        url:  '/account/register',
                        data: {
                            email:        this.form.email,
                            phone_number: this.form.phone,
                            nickname:     this.form.nickname,
                            password:     md5(password),
                            key:          this.form.key,
                            code:         this.form.code,
                        },
                    });

                    if (code === 0) {
                        this.$router.replace({
                            name: 'login',
                        });
                        this.$message.success('恭喜, 注册成功! 请重新登录');
                    } else {
                        this.getImgCode();
                    }
                } else {
                    this.getImgCode();
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
    .terms_text {
        color: #333;
        p {
            font-family: Microsoft YaHei,SimHei,SimSun;
            font-size: 12px;
            text-indent: 20px;
            line-height: 24px;
            text-align: justify;
            span {
                font-weight: bold;
                color: #000;
            }
        }
        ol {
            margin-left: 33px;
            li {
                font-family: Microsoft YaHei,SimHei,SimSun;
                font-size: 12px;
                font-weight: bold;
                color: #000;
                line-height: 24px;
            }
        }
    }
</style>
<style lang="scss">
.terms_dialog {
    .el-dialog__header {
        background: #F5F7FA;
    }
}
</style>
