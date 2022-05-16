<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item label="合作者名称：">
                <el-input
                    v-model="search.partnerName"
                    clearable
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="getList({ to: true})"
                >
                    查询
                </el-button>
                <router-link
                    class="ml10"
                    :to="{name: 'partner-add',}"
                >
                    <el-button>
                        新增合作者
                    </el-button>
                </router-link>
            </el-form-item>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <div slot="empty">
                <TableEmptyData />
            </div>
            <el-table-column
                label="序号"
                width="50"
                type="index"
            />

            <el-table-column
                label="合作者名称"
                min-width="230"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.name }}</p>
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="合作者 code"
                min-width="120"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.code }}</p>
                </template>
            </el-table-column>


            <el-table-column
                label="合作者邮箱"
                min-width="150"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.email }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="创建时间"
                min-width="120"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="创建人"
                width="60"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.created_by }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="修改人"
                width="60"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.updated_by }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="联邦成员"
                width="70"
            >
                <template slot-scope="scope">
                    <p>{{scope.row.is_union_member}}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="状态"
                width="70"
            >
                <template slot-scope="scope">
                    <p>{{ clientStatus[scope.row.status] }}</p>
                </template>
            </el-table-column>


            <el-table-column
                label="操作"
                align="center"
                width="250"
                fixed="right"
            >
                <template slot-scope="scope">
                    <el-button
                        v-if="scope.row.status === 1"
                        type="danger"
                        @click="open(scope.row,0)"
                    >
                        禁用
                    </el-button>
                    <el-button
                        v-if="scope.row.status === 0"
                        type="success"
                        @click="open(scope.row,1)"
                    >
                        启用
                    </el-button>

                    <router-link
                        :to="{
                            name: 'partner-edit',
                            query: {
                                id: scope.row.id,
                                status: scope.row.status
                            },
                        }"
                    >
                        <el-button type="primary">
                            修改
                        </el-button>
                    </router-link>


                    <router-link
                        :to="{
                            name: 'partner-service-add',
                            query: {
                                partnerId : scope.row.id
                            },
                        }"
                    >
                        <el-button type="success">
                            开通服务
                        </el-button>
                    </router-link>
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

import table from '@src/mixins/table.js';
import { mapGetters } from 'vuex';

export default {
    name:   'PartnerList',
    mixins: [table],
    inject: ['refresh'],
    data() {
        return {
            search: {
                partnerName: '',
            },
            timeRange:    '',
            getListApi:   '/partner/query-list',
            clientStatus: {
                1: '启用',
                0: '禁用',
            },
        };
    },

    computed: {
        ...mapGetters(['userInfo']),
    },

    methods: {
        open(row, status) {
            this.$alert('是否修改？', '警告', {
                confirmButtonText: '确定',
                callback:          action => {
                    if (action === 'confirm') {
                        this.changeStatus(row, status);
                        setTimeout(() => {
                            this.refresh();
                        }, 1000);
                    }
                },
            });
        },
        async changeStatus(row, status) {
            const { code } = await this.$http.post({
                url:  '/partner/update',
                data: {
                    id:        row.id,
                    status,
                    name:      row.name,
                    email:     row.email,
                    updatedBy: this.userInfo.nickname,
                },
            });

            if (code === 0) {
                this.$message({
                    type:    'info',
                    message: '修改成功',
                });
            }
        },
    },
};
</script>

<style scoped>

</style>
