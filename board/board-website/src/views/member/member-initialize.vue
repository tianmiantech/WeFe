<template>
    <div
        v-loading="loading"
        class="page register-wrapper"
    >
        <div class="sign-box">
            <div class="logo">
                <img src="@assets/images/x-logo.png">
            </div>
            <h4 class="sign-title">初始化系统 · 成为联邦成员</h4>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.prevent
            >
                <el-form-item
                    label="联邦成员名称："
                    required
                >
                    <el-input
                        v-model.trim="form.member_name"
                        placeholder="成员名称必须包含中文"
                        show-word-limit
                        maxlength="12"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="邮箱：">
                    <el-input
                        v-model.trim="form.member_email"
                        placeholder="hello@world.com"
                        maxlength="40"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="联系电话：">
                    <el-input
                        v-model.trim="form.member_mobile"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="是否允许对外公开数据集基础信息：">
                    <el-radio
                        v-model="form.member_allow_public_data_set"
                        :label="true"
                    >
                        是
                    </el-radio>
                    <el-radio
                        v-model="form.member_allow_public_data_set"
                        :label="false"
                    >
                        否
                    </el-radio>
                </el-form-item>
                <!-- <el-form-item
                    label="Board Service Address："
                    required
                >
                    <el-input
                        v-model.trim="form.board_uri"
                        :placeholder="baseUrl"
                        clearable
                    />
                </el-form-item> -->
                <!-- <el-form-item
                    label="Gateway Uri："
                    required
                >
                    <el-input
                        v-model.trim="form.gateway_uri"
                        placeholder="必填"
                        clearable
                    >
                        <template slot="append">
                            <el-button
                                type="primary"
                                :disabled="!form.gateway_uri"
                                @click="check"
                            >
                                有效性检查
                            </el-button>
                        </template>
                    </el-input>
                </el-form-item> -->
                <el-divider />
                <el-button
                    round
                    type="primary"
                    size="medium"
                    class="btn-submit ml10"
                    @click="submit"
                >
                    加入联邦 ！
                </el-button>
            </el-form>
        </div>

        <el-dialog
            width="500px"
            title="生成您的名片"
            destroy-on-close
            :custom-class="`member-card-wrap ${ memberCard.transition ? 'transition' : '' }`"
            v-model="memberCard.visible"
            :close-on-press-escape="false"
            :close-on-click-modal="false"
            :show-close="false"
        >
            <MemberCard :uploader="true" />

            <div class="text-c pt30">
                <el-button type="primary">提交</el-button>
            </div>
        </el-dialog>
        <section
            v-if="memberCard.visible"
            class="flakes"
        >
            <span class="flake">😁</span>
            <span class="flake">😆</span>
            <span class="flake">😍</span>
            <span class="flake">🤖</span>
            <span class="flake">👏</span>
            <span class="flake">✌️</span>
            <span class="flake">💃</span>
            <span class="flake">🤩</span>
            <span class="flake">🧪</span>
            <span class="flake">👩‍⚕️</span>
        </section>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
    import { EMAILREG } from '@js/const/reg';
    import { baseLogout } from '@src/router/auth';

    export default {
        data() {
            return {
                loading: false,
                baseUrl: '',
                form:    {
                    member_name:                  '',
                    member_email:                 '',
                    member_mobile:                '',
                    member_allow_public_data_set: true,
                    // gateway_uri:                  '',
                },
                memberCard: {
                    visible:    false,
                    transition: false,
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.systemStatusCheck();
        },
        methods: {
            emailFormat(rule, value, callback) {
                if (EMAILREG.test(value)) {
                    callback();
                } else {
                    callback(false);
                }
            },

            async systemStatusCheck() {
                if(this.loading) return;
                this.loading = true;

                const { code, data } = await this.$http.get({
                    url: '/member/is_initialized',
                });

                this.loading = false;
                if(code === 0) {
                    if(data.initialized) {
                        if(this.userInfo.member_id) {
                            this.$store.commit('SYSTEM_INITED', true); // system inited
                            this.$router.replace({
                                name: 'index',
                            });
                        } else {
                            this.$message.success('请重新登录');
                            baseLogout();
                        }
                    }
                } else if (code === 10006) {
                    baseLogout();
                }
            },

            async check(event) {
                const { code } = await this.$http.post({
                    url:  '/member/check_route_connect',
                    data: {
                        gateway_uri: this.form.gateway_uri,
                    },
                    btnState: {
                        target: event,
                    },
                });

                if (code === 0) {
                    this.$message.success('服务正常！');
                } else if (code === 10006) {
                    setTimeout(() => {
                        baseLogout();
                    });
                }
            },

            submit() {
                this.$refs['sign-form'].validate(async valid => {
                    if (valid) {
                        const { code } = await this.$http.post({
                            url:  '/member/initialize',
                            data: this.form,
                        });

                        if (code === 0) {
                            const res = await this.$http.get({
                                url: '/member/detail',
                            });

                            if(res.code === 0) {
                                const info = Object.assign(this.userInfo, res.data);

                                this.$store.commit('SYSTEM_INITED', true); // system initialized
                                this.$store.commit('UPDATE_USERINFO', info);
                                this.$message.success('欢迎来到 WeFe 联邦! ');
                                this.initMemberCard();
                            } else {
                                this.$message.success('请重新登录');
                                baseLogout();
                            }
                        }
                    }
                });
            },

            initMemberCard() {

                this.$router.replace({
                    name: 'index',
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
@import "../sign/sign.scss";
.register-wrapper {
    overflow: hidden;
    min-height: 100vh;
    padding-bottom: 40px;
    background: linear-gradient(90deg, #434343 0, #000);
}

.sign-box {
    padding-top: 0;
    margin-top: 170px;
    background: #fff;
    border-radius: 3px;
    padding: 20px;
}
.el-dialog__wrapper :deep(.member-card-wrap){
    animation: cardRotate 2s ease-in-out;
    .member-card{margin: 0 auto;}
}
.flake {
    position: fixed;
    top: -10%;
    user-select: none;
    animation-name: flakes-fall, flakes-shake-rotate;
    animation-timing-function: linear, ease-in-out;
    animation-iteration-count: infinite, infinite;
    animation-play-state: running, running;
    animation-duration: 10s, 3s;
}
@keyframes flakes-fall {
    0% {
        top: -10%;
    }
    100% {
        top: 100%;
    }
}
@keyframes flakes-shake-rotate {
    0% {
        transform: translateX(0px) rotate(0deg);
    }
    50% {
        transform: translateX(8vmin) rotate(180deg);
    }
    100% {
        transform: translateX(0px) rotate(360deg);
    }
}
.flake:nth-of-type(0) {
    left: 1%;
    animation-delay: 0s, 0s;
}
.flake:nth-of-type(1) {
    left: 10%;
    animation-delay: 1s, 1s;
}
.flake:nth-of-type(2) {
    left: 20%;
    animation-delay: 6s, 0.5s;
}
.flake:nth-of-type(3) {
    left: 30%;
    animation-delay: 4s, 2s;
}
.flake:nth-of-type(4) {
    left: 40%;
    animation-delay: 2s, 2s;
}
.flake:nth-of-type(5) {
    left: 50%;
    animation-delay: 8s, 3s;
}
.flake:nth-of-type(6) {
    left: 60%;
    animation-delay: 6s, 2s;
}
.flake:nth-of-type(7) {
    left: 70%;
    animation-delay: 2.5s, 1s;
}
.flake:nth-of-type(8) {
    left: 80%;
    animation-delay: 1s, 0s;
}
.flake:nth-of-type(9) {
    left: 90%;
    animation-delay: 3s, 1.5s;
}
.btn-submit {
    display: block;
    margin: 0 auto;
}
</style>
