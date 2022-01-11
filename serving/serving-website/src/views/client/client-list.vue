<template>

    <el-card class="page" shadow="never">
        <el-form class="mb20" inline>
            <el-form-item label="客户名称：">
                <el-input v-model="search.clientName" clearable/>
            </el-form-item>

            <el-form-item label="创建时间：">
                <div class="block">
                    <el-date-picker
                        v-model="timeRange"
                        type="datetimerange"
                        range-separator="-"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        @change="timeChange()"
                        value-format="timestamp"
                    >
                    </el-date-picker>
                </div>
            </el-form-item>


            <el-button type="primary" @click="getList('to')">
                查询
            </el-button>
            <router-link
                :to="{name: 'client-add',}">
                <el-button>
                    新增
                </el-button>
            </router-link>


        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <div slot="empty">
                <TableEmptyData/>
            </div>
            <el-table-column label="序号" min-width="80">
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>

            <el-table-column label="客户 code" min-width="60">
                <template slot-scope="scope">
                    <p>{{ scope.row.code }}</p>
                </template>
            </el-table-column>

            <el-table-column label="客户名称" min-width="50">
                <template slot-scope="scope">
                    <p>{{ scope.row.name }}</p>
                </template>
            </el-table-column>
            <el-table-column label="客户邮箱" min-width="80">
                <template slot-scope="scope">
                    <p>{{ scope.row.email }}</p>
                </template>
            </el-table-column>

            <el-table-column label="IP 白名单" min-width="60">
                <template slot-scope="scope">
                    <p>{{ scope.row.ip_add }}</p>
                </template>
            </el-table-column>


            <el-table-column label="创建时间" min-width="80">
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column label="操作" align="center">
                <template slot-scope="scope">


                    <router-link
                        :to="{
                            name: 'client-add',
                            query: {
                                id: scope.row.id
                            },

                        }"
                    >
                        <el-button type="primary">
                            修改
                        </el-button>
                    </router-link>


                    <router-link
                        :to="{
                            name: 'client-service-add',
                            query: {
                                id: scope.row.id
                            },
                        }">
                        <el-button type="success">
                            新增服务
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
import RoleTag from "../components/role-tag";

export default {
    name: "client",
    components: {
        RoleTag,
    },
    mixins: [table],
    data() {
        return {
            search: {
                clientName: '',
                status: '',
                startTime: '',
                endTime: '',

            },
            timeRange: '',
            getListApi: '/client/query-list',
        };
    },
    methods: {
        timeChange() {
            this.search.startTime = this.timeRange[0]
            this.search.endTime = this.timeRange[1]
        }

    },
};
</script>

<style scoped>

</style>
