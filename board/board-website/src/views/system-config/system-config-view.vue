<template>
    <div class="page">
        <el-card>
            <el-form
                :disabled="!userInfo.admin_role"
                @submit.prevent
                label-width="160px"
                :inline="true"
            >
                <el-row :gutter="30">
                    <el-col :span="12">
                        <fieldset>
                            <legend>Board</legend>
                            <el-form-item label="后台内网地址：">
                                <el-input
                                    placeholder="http(s)://ip:port/board-service"
                                    v-model="config.wefe_board.intranet_base_uri"
                                />
                            </el-form-item>
                            <el-form-item label="账号是否需要审核：">
                                <el-radio
                                    v-model="config.wefe_board.account_need_audit_when_register"
                                    :label="true"
                                >
                                    需要审核
                                </el-radio>
                                <el-radio
                                    v-model="config.wefe_board.account_need_audit_when_register"
                                    :label="false"
                                >
                                    不需要审核
                                </el-radio>
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>Gateway</legend>
                            <el-form-item label="内网地址：">
                                <el-input
                                    placeholder="ip:port"
                                    v-model="config.wefe_gateway.intranet_base_uri"
                                />
                            </el-form-item>
                            <el-form-item style="position: relative;">
                                <template v-slot:label>
                                    网关 IP 地址白名单：
                                </template>

                                <el-input
                                    v-model="config.wefe_gateway.ip_white_list"
                                    type="textarea"
                                    clearable
                                    rows="8"
                                />
                                <el-tooltip placement="right" effect="light">
                                    <template #content>
                                        <div class="rule-guide f12">
                                            <strong>tips：</strong>
                                            <br />
                                            所有需要访问 gateway
                                            的本地服务均需要将其 IP
                                            地址添加到白名单，<br />
                                            目前有 board-service 和 flow
                                            两个服务需要访问
                                            gateway，白名单支持通配符和注释。
                                            <br />
                                            <br />
                                            # 允许所有 IP 地址<br />
                                            *<br />
                                            <br />
                                            # 允许指定网段<br />
                                            10.90.*<br />
                                            172.29.26.*<br />
                                            <br />
                                            # 精确指定<br />
                                            183.3.218.18
                                        </div>
                                    </template>
                                    <el-icon class="el-icon-opportunity">
                                        <elicon-opportunity />
                                    </el-icon>
                                </el-tooltip>
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>Flow</legend>
                            <el-form-item label="后台内网地址：">
                                <el-input
                                    placeholder="http(s)://ip:port"
                                    v-model="config.wefe_flow.intranet_base_uri"
                                />
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>Serving</legend>
                            <el-form-item label="后台内网地址：">
                                <el-input
                                    placeholder="http(s)://ip:port/serving-service"
                                    v-model="config.wefe_serving.intranet_base_uri"
                                />
                            </el-form-item>
                            <el-form-item>
                                <el-button type="success"
                                           @click="showDialog"
                                >
                                    初始化
                                </el-button>
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>数据集存储</legend>
                            <el-form-item label="类型：">
                                <el-radio v-model="config.storage_config.storage_type" label="CLICKHOUSE">
                                    Clickhouse
                                </el-radio>
                                <el-radio v-model="config.storage_config.storage_type" disabled label="HDFS">
                                    <el-tooltip class="item" effect="dark" content="coming soon" placement="top-start">
                                        HDFS
                                    </el-tooltip>
                                </el-radio>
                            </el-form-item>
                            <el-form-item label="host：">
                                <el-input v-model="config.clickhouse_storage_config.host" />
                            </el-form-item>
                            <el-form-item label="http port：">
                                <el-input v-model="config.clickhouse_storage_config.http_port" />
                            </el-form-item>
                            <el-form-item label="tcp port：">
                                <el-input v-model="config.clickhouse_storage_config.tcp_port" />
                            </el-form-item>
                            <el-form-item label="username：">
                                <el-input v-model="config.clickhouse_storage_config.username" />
                            </el-form-item>
                            <el-form-item label="password：">
                                <el-input
                                    v-model="config.clickhouse_storage_config.password"
                                    type="password"
                                    placeholder="请输入密码"
                                    autocomplete="new-password"
                                    @contextmenu.prevent
                                    @change="dataStoragePwdChange"
                                    clearable
                                />
                            </el-form-item>
                        </fieldset>

                        <el-dialog title="初始化" v-model="dialogVisibleInfo" width="40%" custom-class="unset-dialog-height">
                            <el-form :model="form">
                                <el-form-item label="账号" :label-width="formLabelWidth">
                                    <el-input v-model="form.phone_number" type="text" clearable
                                              placeholder="请输入Serving管理员账号(手机号)"></el-input>
                                </el-form-item>
                                <el-form-item label="密码" :label-width="formLabelWidth">
                                    <el-input v-model="form.password" type="password" clearable
                                              placeholder="请输入密码"></el-input>
                                </el-form-item>
                            </el-form>
                            <div class="dialog-footer">
                                <el-button @click="dialogVisibleInfo = false">取 消</el-button>
                                <el-button type="primary" @click="init">确 定</el-button>
                            </div>
                        </el-dialog>
                    </el-col>
                    <el-col :span="12">
                        <fieldset>
                            <legend>提醒</legend>
                            <el-form-item label="任务失败邮件提醒：">
                                <el-radio v-model="config.alert_config.email_alert_on_job_error" :label="true">
                                    开启
                                </el-radio>
                                <el-radio v-model="config.alert_config.email_alert_on_job_error" :label="false">
                                    关闭
                                </el-radio>
                            </el-form-item>
                            <el-form-item label="找回密码验证码通道：">
                                <el-radio v-model="config.alert_config.retrieve_password_captcha_channel" label="email">
                                    邮件
                                </el-radio>
                                <el-radio v-model="config.alert_config.retrieve_password_captcha_channel" label="sms">
                                    短信
                                </el-radio>
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>邮件服务器</legend>
                            <el-form-item label="邮件服务器地址：">
                                <el-input v-model="config.mail_server.mail_host" />
                            </el-form-item>
                            <el-form-item label="邮件服务器端口：">
                                <el-input v-model="config.mail_server.mail_port" />
                            </el-form-item>
                            <el-form-item label="邮件用户名：">
                                <el-input v-model="config.mail_server.mail_username" />
                            </el-form-item>
                            <el-form-item label="邮件密码：">
                                <el-input
                                    v-model="config.mail_server.mail_password"
                                    type="password"
                                    placeholder="请输入密码"
                                    autocomplete="new-password"
                                    @contextmenu.prevent
                                    @change="mailPasswordChange"
                                    clearable
                                />
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>阿里云短信通道</legend>
                            <el-form-item label="AccessKeyId：">
                                <el-input v-model="config.aliyun_sms_channel.access_key_id" />
                            </el-form-item>
                            <el-form-item label="AccessKeySecret：">
                                <el-input
                                    v-model="config.aliyun_sms_channel.access_key_secret"
                                    type="password"
                                    placeholder="请输入密码"
                                    autocomplete="new-password"
                                    @contextmenu.prevent
                                    @change="accessKeySecretChange"
                                    clearable
                                />
                            </el-form-item>
                            <el-form-item label="找回密码短信模板码：">
                                <el-input v-model="config.aliyun_sms_channel.retrieve_password_template_code" />
                            </el-form-item>
                            <el-form-item label="短信签名：">
                                <el-input v-model="config.aliyun_sms_channel.sign_name" />
                            </el-form-item>

                        </fieldset>
                    </el-col>
                </el-row>


                <el-divider/>

                <el-button
                    v-loading="loading"
                    class="save-btn mt10"
                    type="primary"
                    @click="update"
                >
                    提交
                </el-button>
            </el-form>
        </el-card>

    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
    import md5 from 'js-md5';
    import Rsa from '@/utils/rsa.js';

    export default {
        data() {
            return {
                loading: false,
                // model
                config:  {
                    wefe_board:                {},
                    wefe_gateway:              {},
                    wefe_flow:                 {},
                    wefe_serving:              {},
                    alert_config:              {},
                    mail_server:               {},
                    storage_config:            {},
                    clickhouse_storage_config: {},
                    aliyun_sms_channel:        {},
                },
                form: {
                    phone_number: '',
                    password:     '',
                },
                visible:           true,
                dialogVisibleInfo: false,
                formLabelWidth:    '20px',
            // initDialogVisible: false,
                publicKey:                  '',
                isChangeMailpwd:            false,
                isChangeAccessKeySecretPwd: false,
                isChangeDataStoragePwd:     false,
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.getData();
        },
        methods: {
            showDialog() {
                this.dialogVisibleInfo = true;
            },

            async init() {

                const password = [
                    this.form.phone_number,
                    this.form.password,
                    this.form.phone_number,
                    this.form.phone_number.substr(0, 3),
                    this.form.password.substr(this.form.password.length - 3),
                ].join('');

                const { code, message } = await this.$http.post({
                    url:  '/member/sync_to_serving',
                    data: {
                        phone_number: this.form.phone_number,
                        password:     md5(password),
                    },
                });

                if (code === 0) {
                    this.$message.success('初始化成功');
                    this.dialogVisibleInfo = false;
                } else {
                    this.$message.error('初始化失败：', message);
                    this.dialogVisibleInfo = false;

                }


            },


            async getGenerate_rsa_key_pair() {
                const { code, data } = await this.$http.get('/crypto/generate_rsa_key_pair');

                if (code === 0 && data && data.public_key) {
                    const { public_key } = data;

                    this.publicKey = public_key;
                }
            },
            dataStoragePwdChange() {
                this.isChangeDataStoragePwd = true;
            },
            mailPasswordChange(val) {
                this.isChangeMailpwd = true;
            },
            accessKeySecretChange() {
                this.isChangeAccessKeySecretPwd = true;
            },
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/global_config/get',
                    data: {
                        groups: [
                            'wefe_board',
                            'wefe_gateway',
                            'alert_config',
                            'mail_server',
                            'wefe_flow',
                            'wefe_serving',
                            'storage_config',
                            'clickhouse_storage_config',
                            'aliyun_sms_channel',
                        ] },
                });

                if (code === 0) {
                    this.config = data;
                }
                this.loading = false;
            },
            async update() {
                // 检查配置的密码部分是否有修改
                // 1. 如果 数据集存储密码、邮件密码、AccessKeySecret密码 都没有被修改，则三个密码置空
                if (!this.isChangeDataStoragePwd && !this.isChangeMailpwd && !this.isChangeAccessKeySecretPwd) {
                    this.config.clickhouse_storage_config.password = null;
                    this.config.mail_server.mail_password = null;
                    this.config.aliyun_sms_channel.access_key_secret = null;
                }

                // 2. 如果 数据集存储密码、邮件密码、AccessKeySecret密码 三个中有其中一个被修改，调用接口获取public_key
                if (this.isChangeDataStoragePwd || this.isChangeMailpwd || this.isChangeAccessKeySecretPwd) {
                    await this.getGenerate_rsa_key_pair();
                    // 一个true，两个false
                    if (this.isChangeDataStoragePwd && !this.isChangeMailpwd && !this.isChangeAccessKeySecretPwd) {
                        this.config.clickhouse_storage_config.password = Rsa.encrypt(this.publicKey, this.config.clickhouse_storage_config.password);
                        this.config.mail_server.mail_password = null;
                        this.config.aliyun_sms_channel.access_key_secret = null;
                    }
                    if (this.isChangeMailpwd && !this.isChangeDataStoragePwd && !this.isChangeAccessKeySecretPwd) {
                        this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                        this.config.clickhouse_storage_config.password = null;
                        this.config.aliyun_sms_channel.access_key_secret = null;
                    }
                    if (this.isChangeAccessKeySecretPwd && !this.isChangeMailpwd && !this.isChangeDataStoragePwd) {
                        this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);
                        this.config.clickhouse_storage_config.password = null;
                        this.config.mail_server.mail_password = null;
                    }
                    // 三个true
                    if (this.isChangeDataStoragePwd && this.isChangeMailpwd && this.isChangeAccessKeySecretPwd) {
                        this.config.clickhouse_storage_config.password = Rsa.encrypt(this.publicKey, this.config.clickhouse_storage_config.password);
                        this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                        this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);
                    }

                    // 两个true，一个false
                    if (this.isChangeDataStoragePwd && this.isChangeMailpwd && !this.isChangeAccessKeySecretPwd) {
                        this.config.clickhouse_storage_config.password = Rsa.encrypt(this.publicKey, this.config.clickhouse_storage_config.password);
                        this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                        this.config.aliyun_sms_channel.access_key_secret = null;
                    }
                    if (!this.isChangeDataStoragePwd && this.isChangeMailpwd && this.isChangeAccessKeySecretPwd) {
                        this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                        this.config.clickhouse_storage_config.password = null;
                        this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);

                    }
                    if (this.isChangeDataStoragePwd && !this.isChangeMailpwd && this.isChangeAccessKeySecretPwd) {
                        this.config.clickhouse_storage_config.password = Rsa.encrypt(this.publicKey, this.config.clickhouse_storage_config.password);
                        this.config.mail_server.mail_password = null;
                        this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);
                    }
                }


                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/global_config/update',
                    data: { groups: this.config },
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.$router.push({ name: 'system-config-view' });
                    this.getData();
                }
                this.isChangeMailpwd = false;
                this.isChangeAccessKeySecretPwd = false;
                this.isChangeDataStoragePwd = false;
                this.loading = false;
            },
            async preconditions() {
                // 未编辑，都置空 null
                if (!this.isChangeDataStoragePwd && !this.isChangeMailpwd && !this.isChangeAccessKeySecretPwd) {
                    this.config.clickhouse_storage_config.password = null;
                    this.config.mail_server.mail_password = null;
                    this.config.aliyun_sms_channel.access_key_secret = null;
                }
                // 有被编辑
                if (this.isChangeDataStoragePwd || this.isChangeMailpwd || this.isChangeAccessKeySecretPwd) {
                    await this.getGenerate_rsa_key_pair();
                    if (this.isChangeDataStoragePwd) {
                        this.config.clickhouse_storage_config.password = Rsa.encrypt(this.publicKey, this.config.clickhouse_storage_config.password);
                        if (!this.isChangeMailpwd && !this.isChangeAccessKeySecretPwd) {
                            this.config.mail_server.mail_password = null;
                            this.config.aliyun_sms_channel.access_key_secret = null;
                        }
                        if (this.isChangeMailpwd && this.isChangeAccessKeySecretPwd) {
                            this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                            this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);
                        }
                        if (this.isChangeMailpwd && !this.isChangeAccessKeySecretPwd) {
                            this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                            this.config.aliyun_sms_channel.access_key_secret = null;
                        }
                        if (!this.isChangeMailpwd && this.isChangeAccessKeySecretPwd) {
                            this.config.mail_server.mail_password = null;
                            this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);
                        }
                    }
                    if (this.isChangeMailpwd) {
                        this.config.mail_server.mail_password = Rsa.encrypt(this.publicKey, this.config.mail_server.mail_password);
                        if (!this.isChangeDataStoragePwd && !this.isChangeAccessKeySecretPwd) {
                            this.config.clickhouse_storage_config.password = null;
                            this.config.aliyun_sms_channel.access_key_secret = null;
                        }
                        if (this.isChangeDataStoragePwd && this.isChangeAccessKeySecretPwd) {
                            this.config.clickhouse_storage_config.password = Rsa.encrypt(this.publicKey, this.config.clickhouse_storage_config.password);
                            this.config.aliyun_sms_channel.access_key_secret = Rsa.encrypt(this.publicKey, this.config.aliyun_sms_channel.access_key_secret);
                        }
                        // ...
                    }
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{
        width: 100%;
    }
    .el-icon-opportunity {
        font-size: 16px;
        color: $--color-warning;
        position: absolute;
        right: 10px;
        top: 10px;
    }
    .rule-guide {
        color: #6c757d;
        line-height: 20px;
        border-radius: 4px;
        padding: 5px 10px;
    }
    .save-btn {
        width: 100px;
    }
</style>
