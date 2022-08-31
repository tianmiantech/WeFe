<template>
    <div class="page">
        <el-card>
            <el-form
                :model="form"
                @submit.prevent
            >
                <el-row :gutter="30">
                    <el-col :span="12">
                        <fieldset>
                            <legend>基本配置</legend>

                            <el-form-item label="系统ID：">
                                {{ form.identity_info.member_id }}

                                <el-popover
                                    v-if="mode === 1"
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
                                    />
                                </el-popover>

                                <el-popover
                                    v-if="mode === 0"
                                    placement="top-start"
                                    width="100"
                                    trigger="hover"
                                    content="未加入联邦！如需加入联邦，请部署wefe-board系统，并将同步密钥到serving（全局设置->系统设置->同步）"
                                >
                                    <el-button
                                        slot="reference"
                                        size="medium"
                                        icon="el-icon-question"
                                        type="text"
                                    />
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
                                    v-model="form.service_cache_config.redis_host"
                                    placeholder=""
                                    :disabled="form.service_cache_config.type=='mem'"
                                />
                            </el-form-item>

                            <el-form-item
                                label="redis端口："
                            >
                                <el-input
                                    v-model="form.service_cache_config.redis_port"
                                    placeholder=""
                                    :disabled="form.service_cache_config.type=='mem'"
                                />
                            </el-form-item>

                            <el-form-item
                                label="密码："
                            >
                                <el-input
                                    v-model="form.service_cache_config.redis_password"
                                    type="password"
                                    autocomplete="new-password"
                                    @contextmenu.prevent
                                    @change="redisPwdChange"
                                    clearable
                                    :disabled="form.service_cache_config.type=='mem'"
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
                                    label="email"
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
                                    v-model="form.mail_server.password"
                                    type="password"
                                    autocomplete="new-password"
                                    @contextmenu.prevent
                                    @change="mailPwdChange"
                                    clearable
                                />
                            </el-form-item>
                        </fieldset>

                        <fieldset>
                            <legend>短信配置</legend>
                            <el-form-item label="AccessKeyId：">
                                <el-input
                                    v-model="form.sms_config.access_key_id"
                                />
                            </el-form-item>
                            <el-form-item label="AccessKeySecret：">
                                <!-- <el-input
                                    v-model="form.sms_config.access_key_secret"
                                /> -->
                                <el-input
                                    v-model="form.sms_config.access_key_secret"
                                    type="password"
                                    autocomplete="new-password"
                                    @contextmenu.prevent
                                    @change="accessKeySecretChange"
                                    clearable
                                />
                            </el-form-item>
                            <el-form-item label="找回密码短信模板码：">
                                <el-input
                                    v-model="form.sms_config.forget_password_template_code"
                                />
                            </el-form-item>
                            <el-form-item label="短信签名">
                                <el-input
                                    v-model="form.sms_config.sign_name"
                                />
                            </el-form-item>
                        </fieldset>
                    </el-col>
                </el-row>


                <el-divider />
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
        <el-card
            v-if="userInfo.super_admin_role"
            class="mt20"
        >
            <el-alert
                title="重置密钥："
                description="Serving 系统的秘钥对，用于系统对外的加签验证"
                style="max-width:600px;"
                type="info"
                close-text=" "
                show-icon
            />
            <br>
            <el-button
                v-loading="is_reset"
                type="danger"
                @click="resetKey"
            >
                重置密钥
            </el-button>
        </el-card>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';
import Rsa from '@/utils/rsa.js';

export default {
    data() {
        return {

            loading:    false,
            is_update:  false,
            is_display: false,
            is_reset:   false,
            mode:       '',
            form:       {
                identity_info: {
                    member_id:        '',
                    member_name:      '',
                    serving_base_url: '',
                    mode:             '',
                },
                wefe_union: {
                    intranet_base_uri: '',
                },
                mail_server: {
                    host:     '',
                    username: '',
                    port:     '',
                    password: '',
                },
                captcha_send_channel: {
                    channel: 'email',
                },
                sms_config: {
                    access_key_id:                 '',
                    access_key_secret:             '',
                    sign_name:                     '',
                    forget_password_template_code: '',
                },
                service_cache_config: {
                    type:           '',
                    redis_host:     '',
                    redis_port:     '',
                    redis_password: '',
                },

                isChangeRedisPwd: false,
                isChangeMailPwd: false,
                isChangeAccessKeySecretPwd: false,
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
    
        redisPwdChange() {
            this.isChangeRedisPwd = true;
        },
        mailPwdChange() {
            this.isChangeMailPwd = true;
        },
        accessKeySecretChange() {
            this.isChangeAccessKeySecretPwd = true;
        },
        async getData() {
            this.loading = true;
            const { code, data } = await this.$http.post({
                url:  '/global_config/detail',
                data: {
                    'groups': [
                        'identity_info',
                        'wefe_union',
                        'mail_server',
                        'captcha_send_channel',
                        'sms_config',
                        'service_cache_config',
                    ],
                },
            });

            this.loading = false;
            this.is_update = !this.userInfo.admin_role;
            this.is_display = this.userInfo.admin_role;

            if (code === 0) {
                if (data.sms_config === null) data.sms_config = {};
                this.form = data;
                if (data.identity_info.mode === 'union') {
                    this.mode = 1;
                } else {
                    this.mode = 0;
                }
            }
            
             this.loading = false;
        },
        async update() {

            // 检查配置的密码部分是否有修改
                // 1. 如果 数据集存储密码、邮件密码、AccessKeySecret密码 都没有被修改，则三个密码置空
                if (!this.isChangeRedisPwd && !this.isChangeMailPwd && !this.isChangeAccessKeySecretPwd) {
                    this.form.service_cache_config.redis_password = null;
                    this.form.mail_server.password = null;
                    this.form.sms_config.access_key_secret = null;
                }

                // 2. 如果 数据集存储密码、邮件密码、AccessKeySecret密码 三个中有其中一个被修改，调用接口获取public_key
                if (this.isChangeRedisPwd || this.isChangeMailPwd || this.isChangeAccessKeySecretPwd) {
                    await this.getGenerate_rsa_key_pair();
                    // 一个true，两个false
                    if (this.isChangeMailPwd && !this.isChangeAccessKeySecretPwd && !this.isChangeRedisPwd) {
                        this.form.mail_server.password = Rsa.encrypt(this.publicKey, this.form.mail_server.password);
                        this.form.service_cache_config.redis_password = null;
                        this.form.sms_config.access_key_secret = null;
                    }
                    if (this.isChangeAccessKeySecretPwd && !this.isChangeMailPwd && !this.isChangeRedisPwd) {
                        this.form.sms_config.access_key_secret = Rsa.encrypt(this.publicKey, this.form.sms_config.access_key_secret);
                        this.form.service_cache_config.redis_password = null;
                        this.form.mail_server.password = null;
                    }
                     if (this.isChangeRedisPwd && !this.isChangeMailPwd && !this.isChangeAccessKeySecretPwd) {
                        this.form.sms_config.access_key_secret = null;
                        this.form.service_cache_config.redis_password = Rsa.encrypt(this.publicKey, this.form.service_cache_config.redis_password);
                        this.form.mail_server.password = null;
                    }

                    // 三个true
                    if (this.isChangeRedisPwd && this.isChangeMailPwd && this.isChangeAccessKeySecretPwd) {
                        this.form.service_cache_config.redis_password = Rsa.encrypt(this.publicKey, this.form.service_cache_config.redis_password);
                        this.form.mail_server.password = Rsa.encrypt(this.publicKey, this.form.mail_server.password);
                        this.form.sms_config.access_key_secret = Rsa.encrypt(this.publicKey, this.form.sms_config.access_key_secret);
                    }

                    // 两个true，一个false
                    if (this.isChangeRedisPwd && this.isChangeMailPwd && !this.isChangeAccessKeySecretPwd) {
                        this.form.service_cache_config.redis_password = Rsa.encrypt(this.publicKey, this.form.service_cache_config.redis_password);
                        this.form.mail_server.password = Rsa.encrypt(this.publicKey, this.form.mail_server.password);
                        this.form.sms_config.access_key_secret = null;
                    }
                    if (!this.isChangeRedisPwd && this.isChangeMailPwd && this.isChangeAccessKeySecretPwd) {
                        this.form.mail_server.password = Rsa.encrypt(this.publicKey, this.form.mail_server.password);
                        this.form.service_cache_config.redis_password = null;
                        this.form.sms_config.access_key_secret = Rsa.encrypt(this.publicKey, this.form.sms_config.access_key_secret);

                    }
                    if (this.isChangeRedisPwd && !this.isChangeMailPwd && this.isChangeAccessKeySecretPwd) {
                        this.form.service_cache_config.redis_password = Rsa.encrypt(this.publicKey, this.form.service_cache_config.redis_password);
                        this.form.mail_server.password = null;
                        this.form.sms_config.access_key_secret = Rsa.encrypt(this.publicKey, this.form.sms_config.access_key_secret);
                    }
                }

            this.loading = true;
            const { code } = await this.$http.post({
                url:  '/global_config/update',
                data: {
                    'groups': this.form,
                },
            });

            if (code === 0) {
                this.$message.success('保存成功!');
                this.$router.push({ name: 'global-setting-view' });
            }
            this.loading = false;
        },
        async getGenerate_rsa_key_pair() {
            const { code, data } = await this.$http.get('/crypto/generate_rsa_key_pair');

            if (code === 0 && data && data.public_key) {
                const { public_key } = data;

                this.publicKey = public_key;
            }
        },
        async resetKey() {
            this.is_reset = true;
            this.$confirm('请注意，重置密钥后系统将退出联邦，无法再使用联邦服务, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText:  '取消',
                type:              'warning',
            }).then( async () => {
                const { code } = await this.$http.get({
                    url: '/system/reset_rsa_key',
                });

                if (code === 0) {
                    this.$message.success('重置成功！');
                }

            }).catch(() => {
                this.$message({
                    type:    'info',
                    message: '已取消',
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
