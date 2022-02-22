<template>
    <div class="page">
        <el-card shadow="never">
            <el-form
                class="mb20"
                :model="form"
            >
                <el-row>
                    <el-col
                        :span="10"
                        style="max-width:320px;"
                    >
                        <el-form-item label="成员头像(点击上传)：">
                            <MemberAvatar
                                :uploader="userInfo.super_admin_role"
                                :img="member_logo"
                                @beforeUpload="beforeUpload"
                            />
                        </el-form-item>
                        <el-form-item label="成员 Id：">
                            {{ form.member_id }}
                        </el-form-item>
                        <el-form-item
                            :rules="[{required: true, message: '名称必填!'}]"
                            label="成员名称："
                        >
                            <el-input
                                v-model="form.member_name"
                                placeholder="仅支持中文名称"
                                :disabled="!userInfo.super_admin_role"
                            />
                        </el-form-item>
                        <el-form-item label="在联邦中隐身：">
                            <el-radio
                                v-model="form.member_hidden"
                                :label="true"
                                :disabled="!userInfo.super_admin_role"
                            >
                                是
                            </el-radio>
                            <el-radio
                                v-model="form.member_hidden"
                                :label="false"
                                :disabled="!userInfo.super_admin_role"
                            >
                                否
                            </el-radio>
                            <p class="tips-alert" v-if="form.member_hidden"> ※ 隐身后其他成员从联邦中不能看到关于您的所有信息</p>
                        </el-form-item>
                        <el-form-item label="是否允许对外公开数据资源基础信息：">
                            <el-radio
                                v-model="form.member_allow_public_data_set"
                                :label="true"
                                :disabled="!userInfo.super_admin_role"
                            >
                                是
                            </el-radio>
                            <el-radio
                                v-model="form.member_allow_public_data_set"
                                :label="false"
                                :disabled="!userInfo.super_admin_role"
                            >
                                否
                            </el-radio>
                            <p class="tips-alert" v-if="!form.member_allow_public_data_set"> ※ 其他成员目前不能查看到您的任何数据资源</p>
                        </el-form-item>
                        <el-form-item label="邮箱：">
                            <el-input
                                v-model="form.member_email"
                                placeholder="hello@world.com"
                                :disabled="!userInfo.super_admin_role"
                            />
                        </el-form-item>
                        <el-form-item label="联系电话：">
                            <el-input
                                v-model="form.member_mobile"
                                :disabled="!userInfo.super_admin_role"
                            />
                        </el-form-item>
                        <el-form-item label="Gateway Uri（对外通信的公网可访问地址）：">
                            <el-input
                                v-model="form.member_gateway_uri"
                                :disabled="!userInfo.super_admin_role"
                            >
                                <template v-slot:append>
                                    <el-button
                                        type="primary"
                                        @click="check"
                                    >
                                        有效性检查
                                    </el-button>
                                </template>
                            </el-input>
                        </el-form-item>
                        <!-- <el-form-item label="Board Uri：">
                            <el-input v-model.trim="form.board_uri" />
                        </el-form-item> -->
                        <el-form-item label="成员最后活动时间：">
                            {{ dateFormat(form.last_activity_time) }}
                        </el-form-item>
                    </el-col>
                    <el-col
                        :span="10"
                        style="padding-left: 100px;"
                    >
                        <p class="mb10">名片预览：</p>
                        <MemberCard />

                        <div v-if="enterpriseAuth !== ''" class="mt40">
                            <el-form-item label="企业实名认证：">
                                <p
                                    v-if="enterpriseAuth === -1"
                                    class="color-danger"
                                >
                                    <el-icon class="mr5">
                                        <elicon-circle-check />
                                    </el-icon>
                                    已拒绝 (原因: {{ audit_comment }})
                                </p>
                                <span
                                    v-if="enterpriseAuth === 0"
                                    class="el-link el-link--danger"
                                    style="white-space: nowrap;"
                                >
                                    <el-icon class="mr5">
                                        <elicon-circle-check />
                                    </el-icon>
                                    未认证
                                    <p class="ml10 f12">(超级管理员可申请实名认证)</p>
                                </span>

                                <span
                                    v-if="enterpriseAuth === 1"
                                    class="el-link el-link--danger"
                                >
                                    <el-icon class="mr5">
                                        <elicon-circle-check />
                                    </el-icon>
                                    审核中
                                </span>
                                <span
                                    v-if="enterpriseAuth === 2"
                                    class="el-link el-link--success"
                                >
                                    <el-icon class="mr5">
                                        <elicon-circle-check />
                                    </el-icon>
                                    已认证
                                </span>
                                <router-link
                                    v-if="userInfo.super_admin_role && enterpriseAuth !== 1"
                                    :to="{ name: 'enterprise-certification' }"
                                    class="f12 ml20"
                                >
                                    {{ enterpriseAuth === 0 ? '去认证' : '重新认证' }}
                                </router-link>
                            </el-form-item>
                        </div>
                    </el-col>
                </el-row>
            </el-form>
            <el-button
                v-if="userInfo.super_admin_role"
                v-loading="loading"
                class="save-btn"
                type="primary"
                @click="update"
            >
                更新
            </el-button>
        </el-card>

        <el-card
            class="mt20"
            shadow="never"
        >
            <el-alert
                title="未雨绸缪总是好的："
                description="当 union 服务出现意外导致您的数据丢失时，可以使用此功能将数据同步到 union。"
                style="max-width:550px;"
                close-text=" "
                type="info"
                show-icon
            />
            <br>
            <el-button
                size="small"
                @click="syncToUnion"
            >
                同步数据到 Union
            </el-button>
        </el-card>
        <el-card v-if="userInfo.super_admin_role" class="mt20">
            <el-alert
                title="重置密钥："
                description="重置成员在 union 中的密钥，当您的密钥泄露时可通过此操作让旧密钥失效。"
                style="max-width:600px;"
                type="info"
                close-text=" "
                show-icon
            />
            <br />
            <el-button type="danger" @click="resetRsaKey">
                重置密钥
            </el-button>
        </el-card>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        inject: ['refresh'],
        data() {
            return {
                loading:     false,
                // model
                member_logo: '',
                form:        {
                    member_name:                  '',
                    member_logo:                  '',
                    member_email:                 '',
                    member_mobile:                '',
                    member_hidden:                false,
                    member_allow_public_data_set: true,
                    member_gateway_uri:           '',
                    last_activity_time:           0,
                },
                enterpriseAuth: '',
                audit_comment:  '',
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.getAuthStatus();
            this.getMemberDetail();
        },
        methods: {
            async getMemberDetail() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url: '/member/detail',
                });

                if (code === 0) {
                    this.form = { ...data };
                    this.form.member_logo = '';

                    this.member_logo = data.member_logo;

                    const info = Object.assign({
                        ...this.userInfo,
                    }, this.form);

                    this.$store.commit('UPDATE_USERINFO', info);

                    const res = await this.$http.post({
                        url:  '/union/member/query',
                        data: {
                            id: data.member_id,
                        },
                    });

                    this.loading = false;

                    if (res.code === 0) {
                        this.form.last_activity_time = res.data.list[0].last_activity_time;
                    }
                }

                this.loading = false;
            },

            async getAuthStatus() {
                const { code, data } = await this.$http.get('/union/member/realname/authInfo/query');

                if(code === 0) {
                    this.enterpriseAuth = data.real_name_auth_status;
                    this.audit_comment = data.audit_comment;
                }
            },

            // upload avatar
            async beforeUpload(file) {
                const { code } = await this.$http.post({
                    url:  '/member/update_logo',
                    data: {
                        memberLogo: file,
                    },
                });

                if(code === 0) {
                    this.member_logo = file;
                    this.userInfo.member_logo = this.member_logo;
                    this.$store.commit('UPDATE_USERINFO', this.userInfo);
                    this.$message.success('头像上传成功!');
                }
            },

            async syncToUnion(event) {
                const { code } = await this.$http.post({
                    url:      '/member/sync_to_union',
                    data:     this.form,
                    btnState: {
                        target: event,
                    },
                });

                if (code === 0) {
                    this.$message.success('同步成功!');
                    this.refresh();
                }
            },

            async update() {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/member/update',
                    data: this.form,
                });

                this.loading = false;

                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.userInfo.member_name = this.form.member_name;
                    this.userInfo.member_email = this.form.member_email;
                    this.userInfo.member_mobile = this.form.member_mobile;
                    this.$store.commit('UPDATE_USERINFO', this.userInfo);
                    this.refresh();
                }
            },

            async check(event) {
                const { code } = await this.$http.post({
                    url:  '/member/check_route_connect',
                    data: {
                        member_id:          this.form.member_id,
                        member_gateway_uri: this.form.member_gateway_uri,
                    },
                    btnState: {
                        target: event,
                    },
                });

                if (code === 0) {
                    this.$message.success('服务正常！');
                }
            },
            resetRsaKey() {
                this.$confirm('此操作将重置成员在 union 中的密钥, <strong class="color-danger">[请在密钥泄露时进行操作]</strong>, 是否继续?', '警告', {
                    type:                     'warning',
                    dangerouslyUseHTMLString: true,
                }).then(async action => {
                    if (action === 'confirm') {
                        const { code } = await this.$http.post({
                            url: '/member/reset_rsa_key',
                        });

                        if (code === 0) {
                            this.$message.success('重置密钥成功!');
                        }
                    }
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form{overflow-x: auto;}
    .el-form-item {
        :deep(.el-form-item__label) {
            color: $color-light;
            padding-bottom: 6px;
            line-height: 18px;
            text-align: left;
            font-size: 13px;
            display: block;
        }
    }
    .el-form-item--small.el-form-item:last-child{margin:0;}
    .save-btn {width: 100px;}

    @keyframes cardRotate {
        0%{transform: rotateZ(0deg) translateX(100%);}
        100%{transform: rotateZ(1440deg) translateX(0);}
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
</style>

<style lang="scss">
.flex-box {
    .el-form-item__content {
        display: flex;
        .el-input {
            width: 42%;
        }
    }
}
.tips-alert{
    font-size: 12px;
    color:red;
    line-height: 12px;
    padding-bottom: 20px;
}
</style>
