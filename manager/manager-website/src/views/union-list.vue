<template>
    <el-card class="page">
        <!-- <el-form
            inline
            @submit.prevent
        >
            <el-form-item label="名称">
                <el-input v-model="search.name" />
            </el-form-item>
            <el-form-item>
                <el-checkbox label="已删除" v-model="search.status"></el-checkbox>
            </el-form-item>
            <el-button
                type="primary"
                native-type="submit"
                @click="getList({ to: true, resetPagination: true })"
            >
                搜索
            </el-button>
        </el-form> -->

        <div class="mb20">
            <el-button
                type="primary"
                @click="editDialog = true; editName = ''; editURL = '';"
            >
                添加
            </el-button>
        </div>

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
            <el-table-column label="名称" min-width="100">
                <template v-slot="scope">
                    {{ scope.row.organization_name }}
                    <!-- <p>节点id: {{ scope.row.union_node_id }}</p> -->
                </template>
            </el-table-column>
            <el-table-column label="链接" min-width="100">
                <template v-slot="scope">
                    {{ scope.row.union_base_url }}
                </template>
            </el-table-column>
            <!-- <el-table-column label="签名" min-width="100">
                <template v-slot="scope">
                    {{ scope.row.sign }}
                </template>
            </el-table-column> -->
            <el-table-column label="是否启用" width="100">
                <template v-slot="scope">
                    <el-tag :type="scope.row.enable ? 'success' : 'danger'">
                        {{ scope.row.enable ? '是' : '否' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                fixed="right"
                width="240"
            >
                <template v-slot="scope">
                    <el-button
                        v-if="scope.row.enable"
                        type="danger"
                        @click="changeStatus($event, scope.row, 'disable')"
                    >
                        禁用
                    </el-button>
                    <el-button
                        v-else
                        type="primary"
                        @click="changeStatus($event, scope.row, 'enable')"
                    >
                        启用
                    </el-button>
                    <el-button
                        type="primary"
                        @click="update($event, scope.row)"
                    >
                        更新
                    </el-button>
                    <el-button
                        type="danger"
                        @click="remove($event, scope.row)"
                    >
                        删除
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

        <el-dialog
            title="union节点"
            v-model="editDialog"
            width="400px"
        >
            <el-form class="flex-form pl20 pr20">
                <el-form-item label="名称:">
                    <el-input v-model.trim="editName" />
                </el-form-item>
                <el-form-item label="链接:">
                    <el-input v-model.trim="editURL" placeholder="https://www.example.com/board-service" />
                </el-form-item>
            </el-form>
            <el-button
                type="primary"
                :disabled="!editName || !editURL"
                style="width:120px; margin-left:120px;"
                @click="confirm"
            >
                提交
            </el-button>
        </el-dialog>
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
                editId:   '',
                editName: '',
                editURL:  '',
                search:   {
                    id:   '',
                    name: '',
                    // status: '',
                },
                watchRoute:    true,
                defaultSearch: true,
                requestMethod: 'post',
                getListApi:    '/union/node/query',
                editDialog:    false,
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            async changeStatus(event, row, status) {
                const params = {
                    unionNodeId: row.union_node_id,
                };

                switch (status) {
                case 'enable':
                    params.enable = true;
                    break;
                case 'disable':
                    params.enable = false;
                    break;
                }

                this.$confirm(params.enable ? '是否要启用该节点?' : '是否继续禁用该节点', '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                })
                    .then(async () => {
                        const { code } = await this.$http.post({
                            url:      '/union/node/enable',
                            data:     params,
                            btnState: {
                                target: event,
                            },
                        });

                        if(code === 0) {
                            this.$message.success('处理成功!');
                            setTimeout(() => {
                                this.refresh();
                            }, 500);
                        }
                    });
            },
            update(event, row) {
                this.editId = row.union_node_id;
                this.editName = row.organization_name;
                this.editURL = row.union_base_url;
                this.editDialog = true;
            },
            remove(event, row) {
                this.$confirm('是否继续删除该节点?', '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                })
                    .then(async () => {
                        const { code } = await this.$http.post({
                            url:  '/union/node/delete',
                            data: {
                                unionNodeId: row.union_node_id,
                            },
                            btnState: {
                                target: event,
                            },
                        });

                        if(code === 0) {
                            this.$message.success('处理成功!');
                            setTimeout(() => {
                                this.refresh();
                            }, 500);
                        }
                    });
            },
            async confirm(event) {
                const params = {
                    organizationName: this.editName,
                    unionBaseUrl:     this.editURL,
                };

                if(this.editId) {
                    params.unionNodeId = this.editId;
                }

                const { code } = await this.$http.post({
                    url:      this.editId ? '/union/node/update' : '/union/node/add',
                    data:     params,
                    btnState: {
                        target: event,
                    },
                });

                if(code === 0) {
                    this.$message.success('处理成功!');
                    setTimeout(() => {
                        this.refresh();
                    }, 500);
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .card-list{min-height: calc(100vh - 250px);}
    .member-cards{
        margin-left: 40px;
        margin-bottom: 40px;
        position: relative;
        display: inline-block;
        vertical-align: top;
        :deep(.realname){font-size:40px;}
    }
    .more-info{
        width: 100%;
        font-size:14px;
        padding-left: 40px;
        padding-right:20px;
        color: $color-light;
        text-align: right;
        position: absolute;
        bottom: 15px;
        right:0;
    }
    .link{color: #eee;}
    .el-icon-s-promotion{
        cursor: pointer;
        &:hover{color: $color-link-base;}
    }
</style>
