<template>
    <el-container style="background:#fff;">
        <div class="carousel">
            <el-carousel height="100%">
                <el-carousel-item class="bg-sunny-morning">
                    <div class="content">
                        <h3>跨行业数据联合</h3>
                        <p>
                            天冕大数据实验室与某知名互联网信贷企业、某终端设备服务商、某细分电商合作共建信贷信用评估模型：某互联网信贷企业A拥有用户的金融数据，某终端设备服务商B拥有用户的手机使用行为数据，某细分电商C拥有用户的消费习惯数据。
                        </p>
                    </div>
                </el-carousel-item>
                <el-carousel-item class="bg-premium-dark">
                    <div class="content">
                        <h3>安全可靠</h3>
                        <p>
                            联邦学习要解决的是个人(2C)和企业(2B)间联合建模的问题：它能做到各个数据拥有者（个人/企业）自有数据不出本地，而后联邦系统可以通过加密机制下的参数交换方式，即在不违反数据隐私法规情况下，建立一个虚拟的共有模型。
                        </p>
                    </div>
                </el-carousel-item>
                <el-carousel-item class="bg-plum-plate">
                    <div class="content">
                        <h3>联邦迁移学习</h3>
                        <p>
                            各方数据保留在本地，避免数据泄露，满足用户隐私保护和数据安全的需求；多个参与者联合数据建立虚拟的共有模型，实现各自的使用目的、共同获益；在联邦学习的体系下，各个参与者的身份和地位相同；联邦学习的建模效果和传统机器学习算法的建模效果相差不大；
                        </p>
                    </div>
                </el-carousel-item>
            </el-carousel>
        </div>

        <el-main>
            <div class="sign-box">
                <div class="logo">
                    <img src="@assets/images/x-logo.png">
                </div>
                <div class="slogan text-c">
                    Manager
                </div>
                <el-divider />

                <div class="sign-form">
                    <h2 class="sign-title mt20 mb20">登录账号</h2>
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
                                v-model="form.account"
                                placeholder="用户名"
                                maxlength="32"
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
                                @paste.prevent
                                @copy.prevent
                                @contextmenu.prevent
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
                        </el-form-item>
                        <div class="sign-action">
                            <el-button
                                type="primary"
                                class="login-btn"
                                native-type="submit"
                                size="medium"
                                @click="submit"
                            >
                                立即登录
                            </el-button>
                        </div>
                        <h4 class="text-r f14 mt20">
                            <!-- <router-link
                                :to="{name: 'find-password'}"
                                class="float-left"
                            >
                                忘记密码
                            </router-link> -->
                            还没有账号?
                            <router-link :to="{ name: 'register', query: { redirect: $route.query.redirect } }">
                                立即注册
                            </router-link>
                        </h4>
                    </el-form>
                </div>
            </div>
            <p class="copyright text-c f12">@copyright 天冕信息技术有限公司 Version {{ version }}</p>
        </el-main>
    </el-container>
</template>

<script>
    import md5 from 'js-md5';

    export default {
        data() {
            return {
                version:    process.env.VERSION,
                submitting: false,
                form:       {
                    password: '',
                    account:  '',
                    code:     '',
                    key:      '',
                },
                imgCode:      '',
                accountRules: [
                    { required: true, message: '请输入用户名' },
                    {
                        /* validator: (rule, value, callback) => {
                            if (/^1[3-9]\d{9}/.test(value)) {
                                callback();
                            } else {
                                callback(new Error('请输入正确的手机号'));
                            }
                        }, */
                        trigger: 'blur',
                    },
                ],
                passwordRules: [{ required: true, message: '请输入你的密码' }],
                codeRules:     [{ required: true, message: '请输入验证码' }],
            };
        },
        created() {
            this.getImgCode();
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
            submit(event) {
                if (this.submitting) return;

                this.submitting = true;
                this.$refs['sign-form'].validate(async valid => {
                    if (valid) {
                        const { code, data } = await this.$http.post({
                            url:  '/user/login',
                            data: {
                                code:     this.form.code,
                                account:  this.form.account,
                                password: md5(this.form.password),
                                key:      this.form.key,
                            },
                            btnState: {
                                target: event,
                            },
                        });

                        if (code === 0) {
                            if(data.need_update_password) {
                                this.$message.error('密码强度不够需重新设置密码');
                                this.$router.replace({
                                    name: 'change-password',
                                });
                            } else {
                                this.$store.commit('UPDATE_USERINFO', data);
                                this.$router.replace({
                                    name: 'index',
                                });
                            }
                        } else {
                            this.getImgCode();
                        }
                    }
                    this.submitting = false;
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    @import "./sign.scss";

    .el-main{
        max-width: 1400px;
        padding-bottom: 60px;
        position: relative;
    }
    .copyright{
        position: absolute;
        bottom: 20px;
        width:100%;
    }
    .slogan{
        font-size: 15px;
        font-weight: bold;
        line-height: 1.4;
    }
    .carousel {
        width: 400px;
        line-height: 1.4;
        font-size: 14px;
        .el-carousel {
            height: 100%;
            position: fixed;
            top: 0;
            left:0;
            width: 300px;
        }

        .el-carousel__item {
            color: #fff;
            padding: 0 60px;
            text-align: center;
            background: #333;
            align-items: center;
            display: flex;
            h3{margin-bottom: 20px;}
        }

        .bg-plum-plate{background: linear-gradient(135deg,#667eea,#764ba2)}
        .bg-premium-dark{background: linear-gradient(90deg,#434343 0,#000)}
        .bg-sunny-morning{background: linear-gradient(120deg,#f6d365,#fda085);}
    }
    .login-btn{width:100%;}

    @media screen and (max-width:1440px) {
        .el-main{max-width: 1000px;}
        .carousel {width: 300px;}
    }

</style>
