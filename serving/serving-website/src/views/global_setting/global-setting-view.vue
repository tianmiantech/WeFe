<template>
    <div class="page">
        <el-card>
            <el-form :model="form" @submit.prevent>
                <el-row :gutter="30">
                    <el-col :span="12">

                        <fieldset>
                            <legend>基本配置</legend>

                            <el-form-item label="系统ID：">
                                {{ form.identity_info.member_id }}

                                <el-popover v-if="mode === 1"
                                            placement="top-start"
                                            width="100"
                                            trigger="hover"
                                            content="已加入联邦"
                                >
                                    <el-button
                                        slot="reference"
                                        size="medium"
                                        icon="el-icon-question"
                                        type="text"
                                    ></el-button>
                                </el-popover>

                                <el-popover v-if="mode === 0"
                                            placement="top-start"
                                            width="100"
                                            trigger="hover"
                                            content="未加入联邦！如需加入联邦，请部署wefe-board系统，并将同步密钥到serving（全局设置->系统设置->同步）">
                                    <el-button
                                        slot="reference"
                                        size="medium"
                                        icon="el-icon-question"
                                        type="text"
                                    ></el-button>
                                </el-popover>

                            </el-form-item>
                            <el-form-item
                                :rules="[{required: true, message: '名称必填!'}]"
                                label="成员名称："
                            >
                                <el-input
                                    v-model="form.identity_info.member_name"
                                    placeholder="仅支持中文名称"
                                    :disabled="is_update"
                                />
                            </el-form-item>
                            <el-form-item
                                label="Serving服务地址："
                            >
                                <el-input
                                    v-model="form.identity_info.serving_base_url"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>

                            <el-form-item
                                label="Union服务地址："
                            >
                                <el-input
                                    v-model="form.wefe_union.intranet_base_uri"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>
                        </fieldset>

                    <fieldset>
                            <legend>服务缓存配置</legend>

                           <el-form-item label="缓存类型：">
                                <el-radio
                                    v-model="form.service_cache_config.type"
                                    label="mem"
                                    :disabled="is_update"
                                >
                                    内存
                                </el-radio>
                                <el-radio
                                    v-model="form.service_cache_config.type"
                                    label="redis"
                                    :disabled="is_update"
                                >
                                    redis
                                </el-radio>
                            </el-form-item>

                            <el-form-item
                                label="redis地址："
                            >
                                <el-input
                                    v-model="form.service_cache_config.host"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>

                            <el-form-item
                                label="redis端口："
                            >
                                <el-input
                                    v-model="form.service_cache_config.port"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>

                              <el-form-item
                                label="密码："
                            >
                                <el-input
                                    v-model="form.service_cache_config.password"
                                    type="password"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>
                        </fieldset>

                    </el-col>

                    <el-col :span="12">
                        <fieldset>
                            <legend>提醒</legend>
                            <el-form-item label="找回密码验证码通道：">
                                <el-radio
                                    v-model="form.captcha_send_channel.channel"
                                    label="sms"
                                    :disabled="is_update"
                                >
                                    短信
                                </el-radio>
                                <el-radio
                                    v-model="form.captcha_send_channel.channel"
                                    label="mail"
                                    :disabled="is_update"
                                >
                                    邮箱
                                </el-radio>
                            </el-form-item>
                        </fieldset>

                        <fieldset>
                            <legend>邮箱配置</legend>

                            <el-form-item
                                label="邮件服务器地址："
                            >
                                <el-input
                                    v-model="form.mail_server.host"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>
                            <el-form-item
                                label="邮件服务器端口："
                            >
                                <el-input
                                    v-model="form.mail_server.port"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>
                            <el-form-item
                                label="邮件用户名："
                            >
                                <el-input
                                    v-model="form.mail_server.username"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>
                            <el-form-item
                                label="邮件密码："
                            >
                                <el-input
                                    type="password"
                                    v-model="form.mail_server.password"
                                    placeholder=""
                                    :disabled="is_update"
                                />
                            </el-form-item>


                        </fieldset>

                        <fieldset v-if="form.sms_config">
                            <legend>短信配置</legend>
                            <el-form-item label="AccessKeyId：">
                                <el-input
                                    v-model="form.sms_config.access_key_id"
                                ></el-input>


                            </el-form-item>
                            <el-form-item label="AccessKeySecret：">
                                <el-input
                                    v-model="form.sms_config.access_key_secret"
                                ></el-input>


                            </el-form-item>
                            <el-form-item label="找回密码短信模板码：">
                                <el-input
                                    v-model="form.sms_config.forget_password_template_code"
                                ></el-input>


                            </el-form-item>
                            <el-form-item label="短信签名">
                                <el-input
                                    v-model="form.sms_config.sign_name"
                                ></el-input>


                            </el-form-item>

                        </fieldset>
                    </el-col>

                </el-row>


                <el-divider/>
                <el-button
                    v-loading="loading"
                    class="save-btn"
                    type="primary"
                    size="medium"
                    :disabled="is_update"
                    @click="update"
                >
                    更新
                </el-button>

            </el-form>
        </el-card>
        <el-card v-if="userInfo.super_admin_role" class="mt20">
            <el-alert
                title="重置密钥："
                description="Serving 系统的秘钥对，用于系统对外的加签验证"
                style="max-width:600px;"
                type="info"
                close-text=" "
                show-icon
            />
            <br/>
            <el-button type="danger" v-loading="is_reset" @click="resetKey">
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

            loading: false,
            is_update: false,
            is_display: false,
            is_reset: false,
            mode: '',
            form: {
                identity_info: {
                    member_id: '',
                    member_name: '',
                    serving_base_url: '',
                    mode: '',
                },
                wefe_union: {
                    intranet_base_uri: '',
                },
                mail_server: {
                    host: '',
                    username: '',
                    port: '',
                    password: '',
                },
                captcha_send_channel: {
                    channel: 'email'
                },
                sms_config: {
                    access_key_id: '',
                    access_key_secret: '',
                    sign_name: '',
                    forget_password_template_code: '',
                },
                service_cache_config: {
                    type: '',
                    redis_host: '',
                    redis_port: '',
                    reids_password: '',
                },
            },

        };
    },
    computed: {
        ...mapGetters(['userInfo']),
    },
    created() {
        this.getData();
    },
    methods: {
        async modeChange() {
            if (this.mode === 1) {
                this.form.identity_info.mode = 'union';
            } else {
                this.form.identity_info.mode = 'standalone';
            }
        },
        async getData() {
            // this.loading = true;
            const {code, data} = await this.$http.post({
                url: '/global_config/detail',
                data: {
                    "groups": [
                        'identity_info',
                        'wefe_union',
                        'mail_server',
                        'captcha_send_channel',
                        'sms_config',
                        'service_cache_config'
                    ]
                }
            });
            this.loading = false;
            this.is_update = !this.userInfo.admin_role;
            this.is_display = this.userInfo.admin_role;

            if (code === 0) {
                this.form = data;
                if (data.identity_info.mode === 'union') {
                    this.mode = 1;
                } else {
                    this.mode = 0;
                }
            }
            
        },
        async update() {
            this.loading = true;
            const {code} = await this.$http.post({
                url: '/global_config/update',
                data: {
                    "groups": this.form
                },
            });
            if (code === 0) {
                this.$message.success('保存成功!');
                this.$router.push({name: 'global-setting-view'});
            }
            this.loading = false;
        },
        async resetKey() {
            this.is_reset = true;
            this.$confirm('请注意，重置密钥后系统将退出联邦，无法再使用联邦服务, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then( async () => {
                const {code} = await this.$http.get({
                    url: '/system/reset_rsa_key',
                });

                if (code === 0) {
                    this.$message.success('重置成功！')
                }

            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消'
                });
            });

            this.is_reset = false;
        },
    },
};
</script>

<style lang="scss" scoped>

.save-btn {
    width: 100px;
}

</style>
