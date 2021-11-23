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
                @click="authorize = true; authorizeName = '';"
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
            <el-table-column label="名称" width="200">
                <template v-slot="scope">
                    {{ scope.row.type_name }}
                </template>
            </el-table-column>
            <!-- <el-table-column label="状态" width="100">
                <template v-slot="scope">
                    <el-tag :type="scope.row.status ? 'success' : 'danger'">
                        {{ scope.row.status ? '已启用' : '已禁用' }}
                    </el-tag>
                </template>
            </el-table-column> -->
            <el-table-column
                label="操作"
                fixed="right"
                min-width="240"
            >
                <template v-slot="scope">
                    <!-- <el-button
                        v-if="scope.row.status"
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
                    </el-button> -->
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
            title="企业认证类型"
            v-model="authorize"
            width="300px"
        >
            <el-form class="flex-form">
                <el-form-item label="名称:">
                    <el-input v-model.trim="authorizeName" />
                </el-form-item>
            </el-form>
            <el-button
                type="primary"
                :disabled="!authorizeName"
                style="width:120px; margin-left:80px;"
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
                authorizeId:   '',
                authorizeName: '',
                search:        {
                    name:   '',
                    status: '',
                },
                watchRoute:    true,
                defaultSearch: true,
                requestMethod: 'post',
                getListApi:    '/member/authtype/query',
                authorize:     false,
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            _getUrlParams() {
                const { query } = this.$route;
                const params = ['status'];

                this.unUseParams = [];

                for (const $key in this.search) {
                    this.search[$key] = '';
                }
                params.forEach(key => {
                    const val = query[key];

                    if(val) {
                        this.search[key] = val === 'true';
                    } else {
                        this.search[key] = false;
                        this.unUseParams.push(key);
                    }
                });
            },
            async changeStatus(event, row, status) {
                const params = {
                    typeId: row.type_id,
                };

                switch (status) {
                case 'disable':
                    params.freezed = true;
                    break;
                case 'enable':
                    params.freezed = false;
                    break;
                }

                const { code } = await this.$http.post({
                    url:      '/member/update',
                    data:     params,
                    btnState: {
                        target: event,
                    },
                });

                if(code === 0) {
                    this.refresh();
                }
            },
            update(event, row) {
                this.authorizeId = row.type_id;
                this.authorizeName = row.type_name;
                this.authorize = true;
            },
            remove(event, row) {
                this.$confirm('是否继续 将移除该类型?', '警告', {
                    type: 'warning',
                })
                    .then(async () => {
                        const { code } = await this.$http.post({
                            url:  '/member/authtype/delete',
                            data: {
                                typeId: row.type_id,
                            },
                            btnState: {
                                target: event,
                            },
                        });

                        if(code === 0) {
                            this.refresh();
                            this.$message.success('处理成功!');
                        }
                    });
            },
            async confirm(event) {
                const params = { typeName: this.authorizeName };

                if(this.authorizeId) {
                    params.typeId = this.authorizeId;
                }

                const { code } = await this.$http.post({
                    url:      this.authorizeId ? '/member/authtype/update' : '/member/authtype/add',
                    data:     params,
                    btnState: {
                        target: event,
                    },
                });

                if(code === 0) {
                    this.refresh();
                    this.$message.success('处理成功!');
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
        :deep(.nickname){font-size:40px;}
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
