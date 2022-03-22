<template>
    <el-card class="page">
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item label="姓名">
                <el-input v-model="search.nickname" clearable />
            </el-form-item>
            <el-form-item label="是否为管理员">
                <el-select
                    v-model="search.adminRole"
                    style="width:100px;"
                    filterable
                    clearable
                >
                    <el-option value="true" label="是"></el-option>
                    <el-option value="false" label="否"></el-option>
                </el-select>
            </el-form-item>
            <el-button
                type="primary"
                native-type="submit"
                @click="getList({ to: true, resetPagination: true })"
            >
                搜索
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
            class="card-list"
            :data="list"
            border
            stripe
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column label="序号" type="index"></el-table-column>
            <el-table-column label="用户名" prop="nickname" width="140" />
            <el-table-column label="手机号" prop="phone_number" width="140" />
            <el-table-column label="邮箱" prop="email" width="180" />
            <el-table-column label="用户角色" width="140">
                <template v-slot="scope">
                    {{ scope.row.super_admin_role ? '超级管理员' : (scope.row.admin_role ? '普通管理员' : '普通用户') }}
                </template>
            </el-table-column>
            <el-table-column label="成员状态">
                <template v-slot="scope">
                    {{ scope.row.enable ? '可用' : '已禁用' }}
                </template>
            </el-table-column>
            <el-table-column label="审核状态" width="220">
                <template v-slot="scope">
                    <el-tag v-if="scope.row.audit_status === 'agree'" type="success">
                        通过
                    </el-tag>
                    <p v-if="scope.row.audit_status === 'disagree'">
                        <el-tag type="danger">
                            不通过
                        </el-tag> <span>({{scope.row.audit_comment}})</span>
                    </p>
                    <el-tag v-if="scope.row.audit_status === 'auditing'">
                        待审核
                    </el-tag>
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
                        <i class="el-icon-check"></i>
                    </span>
                    <span
                        v-else
                    >
                        <i class="el-icon-close"></i>
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                v-if="userInfo.admin_role"
                min-width="370"
                fixed="right"
                label="操作"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.account_id !== userInfo.account_id">
                        <template v-if="scope.row.audit_status === 'agree' && userInfo.admin_role">
                            <template v-if="userInfo.super_admin_role">
                                <el-button
                                    v-if="scope.row.admin_role"
                                    @click="changeRole($event, scope.row)"
                                >
                                    设为普通用户
                                </el-button>
                                <el-button
                                    v-else
                                    type="danger"
                                    @click="changeRole($event, scope.row)"
                                >
                                    设为管理员
                                </el-button>
                            </template>
                            <el-button
                                v-if="userInfo.admin_role"
                                type="primary"
                                @click="resetPassword($event, scope.row)"
                            >
                                重置用户密码
                            </el-button>
                            <el-button
                                v-if="!scope.row.super_admin_role"
                                :type="scope.row.enable ? 'danger' : 'success'"
                                plain
                                @click="changeStatus($event, scope.row)"
                            >
                                {{scope.row.enable ? '禁用' : '启用'}}
                            </el-button>
                        </template>
                        <template v-else>
                            <el-popconfirm
                                confirm-button-text="同意"
                                cancel-button-text="拒绝"
                                cancelButtonType="danger"
                                :hide-icon="true"
                                trigger="hover"
                                @confirm="memberAduit($event, scope.row, 'agree')"
                                @cancel="memberAduit($event, scope.row, 'disagree')"
                            >
                                <template #reference>
                                    <el-button plain>
                                        {{ scope.row.audit_status === 'auditing' ? '审核' : scope.row.audit_status === 'disagree' ? '重新审核' : '' }}
                                    </el-button>
                                </template>
                            </el-popconfirm>
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
            width="340px"
            title="重置用户密码"
            v-model="resetPwDialog.visible"
            destroy-on-close
        >
            <p class="mb10">将重置 <strong class="primary-color">
                {{ resetPwDialog.nickname }}
            </strong> 的登录密码!</p>
            <p class="mt10 mb10 color-danger">原密码将失效, 请谨慎操作</p>
            <span class="color-danger">*</span> 操作人密码:
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
                    :disabled="!transformSuperUserDialog.id || transformSuperUserDialog.id === userInfo.account_id"
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
    import table from '@src/mixins/table';
    import { baseLogout } from '@src/router/auth';

    export default {
        mixins: [table],
        inject: ['refresh'],
        data() {
            return {
                loading: false,
                search:  {
                    realname:  '',
                    adminRole: '',
                },
                watchRoute:    true,
                defaultSearch: true,
                requestMethod: 'post',
                getListApi:    '/account/query',
                resetPwDialog: {
                    visible:          false,
                    id:               '',
                    nickname:         '',
                    operatorPassword: '',
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
        methods: {
            changeRole(event, row) {
                this.$confirm(`确定要将该用户设为${row.admin_role ? '普通用户' : '管理员'}吗?`, '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/account/role/change',
                        data: {
                            accountId: row.account_id,
                            adminRole: !row.admin_role,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        this.$message.success('操作成功!');
                        setTimeout(() => {
                            this.refresh();
                        }, 300);
                    }
                });
            },
            resetPassword(event, row) {
                this.resetPwDialog.id = row.account_id;
                this.resetPwDialog.nickname = row.nickname;
                this.resetPwDialog.visible = true;
            },
            async confirmReset($event) {
                const { phone_number } = this.userInfo;
                const { operatorPassword } = this.resetPwDialog;

                if(!operatorPassword) {
                    return this.$message.error('请输入你的帐号密码');
                }

                const { code, data } = await this.$http.post({
                    url:  '/account/reset/password',
                    data: {
                        accountId:        this.resetPwDialog.id,
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
            changeUserInfo() {
                this.form.nickname = this.userInfo.nickname;
                this.form.email = this.userInfo.email;
            },
            changeStatus($event, row) {
                this.$confirm(`你确定要${ row.enable ? '禁用' : '启用' }该用户吗?`, '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                }).then(async _ => {
                    await this.$http.post({
                        url:  '/account/enable',
                        data: {
                            accountId: row.account_id,
                            enable:    !row.enable,
                        },
                        btnState: {
                            target: $event,
                        },
                    });
                    this.refresh();
                });
            },
            async memberAduit($event, row, flag) {
                const result = flag === 'agree' ? this.$confirm('确定同意当前成员审核吗？', '提示', {
                    type:              'warning',
                    confirmButtonText: '确定',
                    cancelButtonText:  '取消',
                }) : this.$prompt('拒绝当前成员审核？\n 原因:', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText:  '取消',
                    inputValidator(value) {
                        return value != null && value !== '';
                    },
                    inputErrorMessage: '原因不能为空',
                });

                result.then(async ({ action, value }) => {
                    if(flag || action === 'confirm') {
                        const { code } = await this.$http.post({
                            url:  '/account/audit',
                            data: {
                                accountId:    row.account_id,
                                auditStatus:  flag,
                                auditComment: value,
                            },
                            btnState: {
                                target: $event,
                            },
                        });

                        if(code === 0) {
                            this.$message.success('操作成功!');
                            this.refresh();
                        }
                    }
                });
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
                            id:    x.account_id,
                        };
                    });

                    cb(list);
                }
            },

            clearSuggestions() {
                this.transformSuperUserDialog.id = '';
            },

            selectUser(item) {
                if(item.id === this.userInfo.account_id) {
                    return this.$message.error('不能将超级管理员转移给自己!');
                }
                this.transformSuperUserDialog.id = item.id;
            },

            async transformSuperUser($event) {
                const { code } = await this.$http.post({
                    url:  '/super/admin/change',
                    data: {
                        accountId: this.transformSuperUserDialog.id,
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
    .card-list{min-height: calc(100vh - 250px);}
</style>
