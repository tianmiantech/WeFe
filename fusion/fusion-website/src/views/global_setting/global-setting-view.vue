<template>
    <div class="page">
        <el-card
            class="mb20"
        >

            <el-form :model="form" :disabled="!userInfo.super_admin_role && !userInfo.admin_role">

                <el-row :gutter="10">
                    <el-col :span="24">
                        <fieldset>
                            <legend>基本信息</legend>
                            <el-form-item
                                label="Member Id："
                            >
                                <el-input
                                    :disabled="true"
                                    v-model="form.member_info.member_id"
                                />
                            </el-form-item>

                            <el-form-item
                                :rules="[{required: true, message: '名称必填!'}]"
                                label="成员名称："
                            >
                                <el-input
                                    v-model="form.member_info.member_name"
                                    placeholder="仅支持中文名称"
                                />
                            </el-form-item>

                            <el-form-item
                                label="联系方式："
                            >
                                <el-input
                                    v-model="form.member_info.member_mobile"
                                    autosize
                                />
                            </el-form-item>

                            <el-form-item
                                label="邮箱："
                            >
                                <el-input
                                    v-model="form.member_info.member_email"
                                    autosize
                                />
                            </el-form-item>
                        </fieldset>

                        <fieldset>
                            <legend>算法开放端口</legend>
                            <el-form-item
                                label="端口："
                            >
                                <el-input
                                    v-model="form.wefe_fusion.open_socket_port"
                                    autosize
                                />
                            </el-form-item>
                        </fieldset>


                    </el-col>
                </el-row>


                <el-button
                    v-loading="loading"
                    class="save-btn mt10"
                    type="primary"
                    @click="update"

                >
                    更新
                </el-button>
            </el-form>
        </el-card>


        <el-card class="mb20" v-if="userInfo.super_admin_role">
            <el-alert
                title="重置密钥："
                description="重置成员在 union 中的密钥，当您的密钥泄露时可通过此操作让旧密钥失效。"
                style="max-width:500px;"
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
import {mapGetters} from 'vuex';

    export default {
        data() {
            return {
                loading:    false,
                resetKeyLoading: false,
                is_update:  true,
                is_display: false,
                // model
                form:       {
                    member_info: {
                        member_id:      '',
                        member_name:    '',
                        member_email: '',
                        member_mobile:  '',
                        // 待添加字段
                        member_logo:  '',
                    },
                    wefe_fusion: {
                        open_socket_port: ''
                    },

                },

            };
        },
        created() {
            this.getData();
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url: '/system/global_config/detail',
                    data: {
                        groups: ["member_info","wefe_fusion"]
                    }
                });

                if (code === 0) {
                    this.form = data;
                }
                this.loading = false;
            },
            async update() {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  'global_config/update',
                    data: {
                        groups: this.form
                    },
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                }
                this.loading = false;
            },

            async resetRsaKey() {
                this.resetKeyLoading = true;
                const { code } = await this.$http.post({
                    url:  'system/reset_rsa_key',
                });

                if (code === 0) {
                    this.$message.success('重置成功!');
                }
                this.resetKeyLoading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form {width: 500px;}
    .save-btn{ width: 100px;}
</style>
