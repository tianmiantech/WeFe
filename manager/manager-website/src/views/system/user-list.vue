<template>
    <el-card class="page">
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item label="姓名">
                <el-input v-model="search.realname" />
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
            <el-table-column label="用户名" prop="account" width="140" />
            <el-table-column label="姓名" prop="realname" width="130" />
            <el-table-column label="邮箱" prop="email" />
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
            <el-table-column
                label="操作"
                fixed="right"
                min-width="300"
            >
                <template v-slot="scope">
                    <el-button
                        v-if="userInfo.super_admin_role && scope.row.user_id !== userInfo.user_id"
                        type="danger"
                        @click="changeRole($event, scope.row)"
                    >
                        设为{{ scope.row.admin_role ? '普通用户' : '管理员' }}
                    </el-button>
                    <el-button
                        v-if="userInfo.admin_role"
                        type="primary"
                        @click="resetPassword($event, scope.row)"
                    >
                        重置用户密码
                    </el-button>
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
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table';

    export default {
        inject: ['refresh'],
        mixins: [table],
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
                getListApi:    '/user/query',
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
                        url:  '/user/role/change',
                        data: {
                            userId:    row.user_id,
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
                this.$confirm('确定要将该用户密码重置吗?', '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/user/reset/password',
                        data: {
                            userId: row.user_id,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        this.$message.success('操作成功! 该用户密码已重置为 wefe123456');
                        setTimeout(() => {
                            this.refresh();
                        }, 300);
                    }
                });
            },
            changeUserInfo() {
                this.form.realname = this.userInfo.realname;
                this.form.email = this.userInfo.email;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .card-list{min-height: calc(100vh - 250px);}
</style>
