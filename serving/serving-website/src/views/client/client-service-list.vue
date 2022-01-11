<template>

    <el-card class="page" shadow="never">
        <el-form class="mb20" inline>
            <el-form-item label="客户名称：">
                <el-input v-model="search.clientName" clearable/>
            </el-form-item>

            <el-form-item label="服务名称：">
                <el-input v-model="search.serviceName" clearable/>
            </el-form-item>

            <el-form-item label="是否启用：">

                <el-select v-model="search.status" placeholder="请选择" clearable>
                    <el-option
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>


            </el-form-item>

            <el-button type="primary" @click="getList('to')">
                查询
            </el-button>
            <router-link :to="{name: 'client-service-add'}">
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
            <el-table-column label="序号 ID" min-width="80">
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>
            <el-table-column label="客户名称" min-width="50">
                <template slot-scope="scope">
                    <p>{{ scope.row.client_name }}</p>
                </template>
            </el-table-column>
            <el-table-column label="服务名称" min-width="80">
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                </template>
            </el-table-column>

            <el-table-column label="服务类型" min-width="60">
                <template slot-scope="scope">
                    <p>{{ serviceType[scope.row.service_type] }}</p>
                </template>
            </el-table-column>

            <el-table-column label="IP 白名单" min-width="80">
                <template slot-scope="scope">
                    {{ scope.row.ip_add }}
                </template>
            </el-table-column>

            <el-table-column label="请求地址" min-width="80">
                <template slot-scope="scope">
                    <el-tooltip class="item" effect="dark" :content="scope.row.url" placement="left-start">
                        <p v-if="scope.row.url.length >= 20">{{ scope.row.url.substring(0, 20) }} ...</p>
                        <p v-if="scope.row.url.length < 20">{{ scope.row.url }} </p>
                    </el-tooltip>
                </template>
            </el-table-column>

            <el-table-column label="单价(￥)" min-width="50">
                <template slot-scope="scope">
                    {{ scope.row.unit_price }}
                </template>
            </el-table-column>

            <el-table-column label="付费类型" min-width="50">
                <template slot-scope="scope">
                    {{ payType[scope.row.pay_type] }}
                </template>
            </el-table-column>

            <el-table-column label="启用状态" min-width="50">
                <template slot-scope="scope">
                    <el-button v-if="scope.row.status === 0" type="success"
                               @click="open(scope.row,1)">启用
                    </el-button>
                    <el-button v-if="scope.row.status === 1" type="danger"
                               @click="open(scope.row,0)">禁用
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

import table from '@src/mixins/table.js';
import RoleTag from "../components/role-tag";

export default {
    name: "client-service-list",
    inject: ['refresh'],
    components: {
        RoleTag,
    },
    mixins: [table],
    data() {
        return {
            search: {
                clientName: '',
                status: '',
                serviceName: '',
            },
            options: [{
                value: "1",
                label: '已启用'
            }, {
                value: "0",
                label: '未启用'
            }],
            serviceType: {
                1: "匿踪查询",
                2: "交集查询",
                3: "安全聚合(被查询方)",
                4: "安全聚合(查询方)",
            },
            payType: {
                1: "预付费",
                0: "后付费",
            },
            // 启用状态
            statusType: {
                1: "已启用",
                0: "未启用"
            },
            changeStatusType: '',
            getListApi: '/clientservice/query-list',

        };
    },
    methods: {
        open(row, status) {
            this.$alert('是否确定修改启用状态？', '修改启用状态', {
                confirmButtonText: '确定',
                callback: action => {
                    this.changeStatus(row, status)
                    setTimeout(() => {
                        this.refresh()
                    }, 1000)
                    this.$message({
                        type: 'info',
                        message: `修改成功`
                    });
                }
            });
        },

        async changeStatus(row, status) {
            const {code} = await this.$http.post({
                url: '/clientservice/save',
                data: {
                    serviceId: row.service_id,
                    clientId: row.client_id,
                    status: status,
                    payType: row.pay_type,
                    unitPrice: row.unit_price

                }
            });

            if (code === 0) {
                this.success("修改成功")

            }
        }
    }
};
</script>

<style scoped>

</style>
