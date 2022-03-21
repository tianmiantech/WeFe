<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item
                label="手机号："
                label-width="80px"
            >
                <el-input
                    v-model="search.phone_number"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="姓名："
                label-width="80px"
            >
                <el-input
                    v-model="search.nickname"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="审核状态："
                label-width="100px"
            >
                <el-select
                    v-model="search.audit_status"
                    style="width: 176px;"
                    clearable
                >
                    <el-option
                        label="待审核"
                        value="auditing"
                    />
                    <el-option
                        label="已通过"
                        value="agree"
                    />
                    <el-option
                        label="已拒绝"
                        value="disagree"
                    />
                </el-select>
            </el-form-item>
            <el-button
                type="primary"
                class="inline-block"
                native-type="submit"
                @click="getList({ to: true, resetPagination: true })"
            >
                查询
            </el-button>
            <div class="mb20">
                <el-button
                    v-if="userInfo.super_admin_role"
                    type="danger"
                    @click="transformSuperUserDialog.visible=true"
                >
                    超级管理员转移
                </el-button>
            </div>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column
                label="姓名"
                prop="nickname"
                min-width="100"
            />
            <el-table-column
                v-if="userInfo.admin_role"
                prop="phone_number"
                min-width="100"
                label="手机号"
            />
            <el-table-column
                v-if="userInfo.admin_role"
                min-width="200"
                label="email"
                prop="email"
            />
            <el-table-column
                v-if="userInfo.admin_role"
                label="管理员"
                align="center"
                width="70"
            >
                <template v-slot="scope">
                    <span
                        v-if="scope.row.admin_role"
                        class="super_admin_role"
                    >
                        <el-icon>
                            <elicon-check />
                        </el-icon>
                    </span>
                    <span
                        v-else
                        class="not_super_admin_role"
                    >
                        <el-icon>
                            <elicon-close />
                        </el-icon>
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                label="已注销"
                align="center"
                width="70"
            >
                <template v-slot="scope">
                    <span
                        v-if="scope.row.cancelled"
                    >
                        <el-icon>
                            <elicon-check />
                        </el-icon>
                    </span>
                    <span
                        v-else
                    >
                        <el-icon>
                            <elicon-close />
                        </el-icon>
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                v-if="userInfo.super_admin_role"
                label="超级管理员"
                align="center"
                width="100"
            >
                <template v-slot="scope">
                    <span
                        v-if="scope.row.super_admin_role"
                        class="super_admin_role"
                    >
                        <el-icon>
                            <elicon-check />
                        </el-icon>
                    </span>
                    <span
                        v-else
                        class="not_super_admin_role"
                    >
                        <el-icon>
                            <elicon-close />
                        </el-icon>
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                label="是否已注销"
                min-width="140"
                align="center"
            >
                <template v-slot="scope">
                    {{ scope.row.cancelled ? '是' : '否' }}
                </template>
            </el-table-column>
            <el-table-column
                label="注册时间"
                min-width="140"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.created_time) }}
                </template>
            </el-table-column>
            <el-table-column
                min-width="340"
                label="操作"
            >
                <template v-slot="scope">
                    <template v-if="userInfo.admin_role && userInfo.id !== scope.row.id">
                        <el-button
                            v-if="scope.row.audit_status === 'auditing'"
                            type="primary"
                            @click="showAuditPanel(scope.row)"
                        >
                            审核
                        </el-button>
                        <template v-else>
                            <template v-if="userInfo.super_admin_role">
                                <el-button
                                    v-if="!scope.row.admin_role"
                                    type="primary"
                                    @click="changeUserRole(scope.row)"
                                >
                                    设为管理员
                                </el-button>
                                <el-button
                                    v-if="scope.row.admin_role && !scope.row.super_admin_role"
                                    type="primary"
                                    @click="changeUserRole(scope.row)"
                                >
                                    设为普通用户
                                </el-button>
                            </template>
                            <el-button @click="resetPassword(scope.row)">
                                重置密码
                            </el-button>
                            <el-button
                                v-if="scope.row.enable"
                                type="danger"
                                @click="disableUser(scope.row)"
                            >
                                禁用
                            </el-button>
                            <el-button
                                v-else
                                type="danger"
                                @click="disableUser(scope.row)"
                            >
                                取消禁用
                            </el-button>
                        </template>
                    </template>
                </template>
            </el-table-column>
        </el-table>

        <div
            v-if="pagination.total"
            class="mt20 text-r"
        >
            <el-pagination
                :total="pagination.total"
                :page-sizes="[10, 20, 30, 40, 50]"
                :page-size="pagination.page_size"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
        </div>

        <el-dialog
            title="用户审核"
            v-model="dialogAuditAccountVisible"
            destroy-on-close
            width="500px"
        >
            <el-form :model="form" inline>
                <el-form-item
                    label="审核意见："
                    :label-width="formLabelWidth"
                >
                    <el-input v-model="form.audit_comment" />
                </el-form-item>
                <el-form-item
                    label="审核结果："
                    :label-width="formLabelWidth"
                >
                    <el-radio
                        v-model="form.audit_status"
                        label="agree"
                    >
                        同意
                    </el-radio>
                    <el-radio
                        v-model="form.audit_status"
                        label="disagree"
                    >
                        拒绝
                    </el-radio>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button
                    type="primary"
                    @click="audit"
                >
                    确 定
                </el-button>
            </template>
        </el-dialog>

        <el-dialog
            width="340px"
            title="重置用户密码"
            v-model="resetPwDialog.visible"
            destroy-on-close
        >
            将重置 <strong class="primary-color">
                {{ resetPwDialog.nickname }}
            </strong> 的登录密码!
            <p class="mt10 mb10 color-danger">原密码将失效, 请谨慎操作</p>
            <span class="color-danger">*</span> 操作人登录密码:
            <el-input
                v-model="resetPwDialog.operatorPassword"
                style="width: 200px;"
                type="password"
                @paste.prevent
                @copy.prevent
                @contextmenu.prevent
            />
            <template #footer>
                <el-button
                    type="danger"
                    @click="confirmReset"
                >
                    确定
                </el-button>
                <el-button @click="resetPwDialog.visible = false">
                    取消
                </el-button>
            </template>
        </el-dialog>

        <el-dialog
            width="400px"
            title="更改用户权限"
            v-model="userRoleDialog.visible"
            destroy-on-close
        >
            是否将 <strong>{{ userRoleDialog.nickname }}</strong> 设置为 <strong class="primary-color">
                {{ userRoleDialog.admin_role ? '普通用户' : '管理员' }}
            </strong>?
            <p class="f12 mt10 color-danger">* 只有管理员能对“全局设置”中的配置项进行变更<br>* 只有超级管理员能对“成员信息”中的配置项进行变更</p>
            <template #footer>
                <el-button
                    type="danger"
                    @click="confirmChangeUserRole"
                >
                    是
                </el-button>
                <el-button @click="userRoleDialog.visible = false">
                    否
                </el-button>
            </template>
        </el-dialog>

        <el-dialog
            width="340px"
            :title="disableUserDialog.enable ? '禁止用户登录' : '允许用户登录'"
            v-model="disableUserDialog.visible"
            destroy-on-close
        >
            将{{ disableUserDialog.enable ? '禁止' : '允许' }} <strong>{{ disableUserDialog.nickname }}</strong> 的
            <strong class="primary-color">
                登录权限
            </strong>
            <template #footer>
                <el-button
                    type="danger"
                    @click="confirmDisableUser"
                >
                    是
                </el-button>
                <el-button @click="disableUserDialog.visible=false">
                    否
                </el-button>
            </template>
        </el-dialog>

        <el-dialog
            width="440px"
            title="超级管理员转移"
            v-model="transformSuperUserDialog.visible"
            destroy-on-close
        >
            <el-alert
                type="error"
                title="超级管理员角色转让以后你将变成【普通角色】, 并【失去】所有超级管理员权限! 请谨慎操作"
                :closable="false"
            />
            <el-form
                label-width="120px"
                class="flex-form mt30"
            >
                <el-form-item
                    label="选择目标用户"
                    class="is-required"
                >
                    <el-autocomplete
                        v-model="transformSuperUserDialog.user"
                        placeholder="输入姓名或者11位手机号搜索"
                        :fetch-suggestions="getUsers"
                        @select="selectUser"
                        style="width: 260px;"
                        clearable
                        @clear="clearSuggestions"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button
                    type="primary"
                    :disabled="!transformSuperUserDialog.id || transformSuperUserDialog.id === userInfo.id"
                    @click="transformSuperUser"
                >
                    确定
                </el-button>
                <el-button @click="transformSuperUserDialog.visible=false">
                    取消
                </el-button>
            </template>
        </el-dialog>
    </el-card>
</template>

<script>
    import md5 from 'js-md5';
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table.js';
    import { baseLogout } from '@src/router/auth';

    export default {
        mixins: [table],
        inject: ['refresh'],
        data() {
            return {
                dialogAuditAccountVisible: false,
                formLabelWidth:            '100px',

                search: {
                    phone_number: '',
                    nickname:     '',
                    audit_status: '',
                },
                getListApi:     '/account/query',
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },

                form: {
                    account_id:    '',
                    audit_status:  'agree',
                    audit_comment: '',
                },
                resetPwDialog: {
                    visible:          false,
                    id:               '',
                    nickname:         '',
                    operatorPassword: '',
                },
                userRoleDialog: {
                    id:         '',
                    nickname:   '',
                    visible:    false,
                    admin_role: false,
                },
                disableUserDialog: {
                    visible:  false,
                    enable:   false,
                    nickname: '',
                    id:       '',
                },
                transformSuperUserDialog: {
                    visible: false,
                    user:    '',
                    id:      '',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.getList();
        },
        methods: {
            // show audit dialog
            showAuditPanel(row) {
                this.form.account_id = row.id;
                this.form.audit_status = 'agree';
                this.form.audit_comment = '';

                this.dialogAuditAccountVisible = true;
            },
            async audit($event) {
                const { code } = await this.$http.post({
                    url:      '/account/audit',
                    data:     this.form,
                    btnState: {
                        target: $event,
                    },
                });

                this.dialogAuditAccountVisible = false;

                if(code === 0){
                    this.getList();
                }
            },
            resetPassword(row) {
                this.resetPwDialog.id = row.id;
                this.resetPwDialog.nickname = row.nickname;
                this.resetPwDialog.visible = true;
            },
            async confirmReset($event) {
                const { operatorPassword } = this.resetPwDialog;
                const { phone_number } = this.userInfo;

                if(!operatorPassword) {
                    return this.$message.error('请输入你的帐号密码');
                }

                const { code, data } = await this.$http.post({
                    url:  '/account/reset/password',
                    data: {
                        id:               this.resetPwDialog.id,
                        operatorPassword: md5([
                            phone_number,
                            operatorPassword,
                            phone_number,
                            phone_number.substr(0, 3),
                            operatorPassword.substr(operatorPassword.length - 3),
                        ].join('')),
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.resetPwDialog.operatorPassword = '';
                    this.resetPwDialog.visible = false;
                    this.$alert(`该用户密码已重置为 <strong>${data}</strong> <p class="color-danger">此密码仅可查看一次, 请勿随意传播</p>`, '操作成功', {
                        type:                     'warning',
                        dangerouslyUseHTMLString: true,
                        confirmButtonText:        '确定',
                    });
                    setTimeout(() => {
                        this.refresh();
                    }, 300);
                }
            },
            changeUserRole(row) {
                this.userRoleDialog.visible = true;
                this.userRoleDialog.nickname = row.nickname;
                this.userRoleDialog.admin_role = row.admin_role;
                this.userRoleDialog.id = row.id;
            },
            async confirmChangeUserRole($event) {
                const { code } = await this.$http.post({
                    url:  '/account/update',
                    data: {
                        id:        this.userRoleDialog.id,
                        adminRole: !this.userRoleDialog.admin_role,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.getList();
                    this.userRoleDialog.visible = false;
                    this.$message.success('操作成功!');
                }
            },
            disableUser(row) {
                this.disableUserDialog.id = row.id;
                this.disableUserDialog.enable = row.enable;
                this.disableUserDialog.nickname = row.nickname;
                this.disableUserDialog.visible = true;
            },
            async confirmDisableUser($event) {
                const { code } = await this.$http.post({
                    url:  '/account/enable',
                    data: {
                        id:     this.disableUserDialog.id,
                        enable: !this.disableUserDialog.enable,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.getList();
                    this.disableUserDialog.visible = false;
                    this.$message.success('操作成功!');
                }
            },

            async getUsers(value, cb) {
                if(value === '') {
                    return cb([]);
                }

                const params = {};

                if(/^1[3-9]\d{9}/.test(value)) {
                    params.phone_number = value;
                } else {
                    params.nickname = value;
                }

                const { code, data } = await this.$http.get({
                    url: this.getListApi,
                    params,
                });

                if(code === 0) {
                    const list = data.list.map(x => {
                        return {
                            value: `${x.nickname} (${x.phone_number})`,
                            id:    x.id,
                        };
                    });

                    cb(list);
                }
            },

            clearSuggestions() {
                this.transformSuperUserDialog.id = '';
            },

            selectUser(item) {
                if(item.id === this.userInfo.id) {
                    return this.$message.error('不能将超级管理员转移给自己!');
                }
                this.transformSuperUserDialog.id = item.id;
            },

            async transformSuperUser($event) {
                const { code } = await this.$http.post({
                    url:  '/super/admin/change',
                    data: {
                        id: this.transformSuperUserDialog.id,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    baseLogout();
                    this.$message.success('操作成功, 请重新登录!');
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .primary-color {color:$color-link-base-hover;}
    .new_password{
        margin:10px 0;
        padding: 5px 10px;
        border-radius: 2px;
        border: 1px solid #e5e5e5;
        background: #f9f9f9;
    }
</style>
