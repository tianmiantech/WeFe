<template>
    <div class="page">
        <el-card>
            <el-form
                :disabled="!userInfo.admin_role"
                @submit.prevent
            >
                <el-row :gutter="30">


                    <el-col :span="12">
                        <fieldset>
                            <legend>Board</legend>
                            <el-form-item label="后台内网地址（board-service）：">
                                <el-input
                                    placeholder="http(s)://ip:port/board-service"
                                    v-model="config.wefe_board.intranet_base_uri"
                                />
                            </el-form-item>
                            <el-form-item label="新注册的账号是否需要管理员审核：">
                                <el-radio
                                    v-model="config.wefe_board.account_need_audit_when_register"
                                    :label="'true'"
                                >
                                    需要审核
                                </el-radio>
                                <el-radio
                                    v-model="config.wefe_board.account_need_audit_when_register"
                                    :label="'false'"
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
                                            <br/>
                                            所有需要访问 gateway
                                            的本地服务均需要将其 IP
                                            地址添加到白名单，<br/>
                                            目前有 board-service 和 flow
                                            两个服务需要访问
                                            gateway，白名单支持通配符和注释。
                                            <br/>
                                            <br/>
                                            # 允许所有 IP 地址<br/>
                                            *<br/>
                                            <br/>
                                            # 允许指定网段<br/>
                                            10.90.*<br/>
                                            172.29.26.*<br/>
                                            <br/>
                                            # 精确指定<br/>
                                            183.3.218.18
                                        </div>
                                    </template>
                                    <el-icon class="el-icon-opportunity">
                                        <elicon-opportunity/>
                                    </el-icon>
                                </el-tooltip>
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>Flow</legend>
                            <el-form-item label="内网地址：">
                                <el-input
                                    placeholder="http(s)://ip:port"
                                    v-model="config.wefe_flow.intranet_base_uri"
                                />
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>Serving</legend>
                            <el-form-item label="内网地址：">
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


                        <el-dialog title="Serving系统初始化" v-model="dialogVisibleInfo" width="40%">
                            <el-form :model="form">
                                <el-form-item label="账号" :label-width="formLabelWidth">
                                    <el-input v-model="form.phone_number" type="text" clearable
                                              placeholder="请输入手机号"></el-input>
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

                        <el-dialog
                            title="成功"
                            v-model="initDialogVisible"
                            width="30%"
                            center
                            custom-class="init-success"
                        >
                            <div class="serving-init-div">Serving 系统已初始化成功！</div>
                            <div style="text-align: right">
                                <el-button type="primary" @click="initDialogVisible = false">确 定</el-button>
                            </div>
                        </el-dialog>
                    </el-col>
                    <el-col :span="12">
                        <fieldset>
                            <legend>提醒</legend>
                            <el-form-item label="是否开启任务失败邮件提醒功能：">
                                <el-radio v-model="config.alert_config.email_alert_on_job_error" :label="'true'">
                                    开启
                                </el-radio>
                                <el-radio v-model="config.alert_config.email_alert_on_job_error" :label="'false'">
                                    关闭
                                </el-radio>
                            </el-form-item>
                        </fieldset>
                        <fieldset>
                            <legend>邮件服务器</legend>
                            <el-form-item label="邮件服务器地址：">
                                <el-input v-model="config.mail_server.mail_host"/>
                            </el-form-item>
                            <el-form-item label="邮件服务器端口：">
                                <el-input v-model="config.mail_server.mail_port"/>
                            </el-form-item>
                            <el-form-item label="邮件用户名：">
                                <el-input v-model="config.mail_server.mail_username"/>
                            </el-form-item>
                            <el-form-item label="邮件密码：">
                                <el-input
                                    v-model="config.mail_server.mail_password"
                                    type="password"
                                    placeholder="请输入密码"
                                    autocomplete="new-password"
                                    @paste.prevent
                                    @copy.prevent
                                    @contextmenu.prevent
                                />
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

    export default {
        data() {
            return {
                loading: false,
                // model
                config:  {
                    wefe_board:   {},
                    wefe_gateway: {},
                    wefe_flow:    {},
                    wefe_serving: {},
                    alert_config: {},
                    mail_server:  {},
                },
                form: {
                    phone_number: '',
                    password:     '',
                },
                visible:           true,
                dialogVisibleInfo: false,
                formLabelWidth:    '20px',
                initDialogVisible: false,
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

                const { code, data, message } = await this.$http.post({
                    url:  '/member/sync_to_serving',
                    data: {
                        phone_number: this.form.phone_number,
                        password:     md5(password),
                    },
                });

                if (code === 0) {
                    console.log(data);
                    console.log(message);
                    this.initDialogVisible = true;
                } else {
                    console.log(data);
                    console.log(message);
                }

            },


            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/global_config/get',
                    data: { groups: ['wefe_board', 'wefe_gateway', 'alert_config', 'mail_server', 'wefe_flow', 'wefe_serving'] },
                });

                if (code === 0) {
                    this.config = data;
                }
                this.loading = false;
            },
            async update() {
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
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
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

.serving-init-div {
    padding: 5px;
    margin-bottom: 10px;
}

.init-success {
    .el-dialog__body {
        padding: 20px;
    }
}
</style>
<!--<style lang="scss">

</style>-->
