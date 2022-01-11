<template>
    <el-card class="page" shadow="never">
        <el-form class="mb20" inline>
            <el-form-item label="服务名称：">
                <el-select v-model="search.serviceId" filterable clearable placeholder="请选择服务">
                    <el-option
                        v-for="item in services"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="客户名称：">
                <el-select v-model="search.clientId" filterable clearable placeholder="请选择客户">
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="调用时间：">
                <div class="block">
                    <el-date-picker
                        v-model="defaultTime"
                        type="datetimerange"
                        range-separator="To"
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
            <el-table-column label="序号" min-width="40" type="index">

            </el-table-column>

            <el-table-column label="服务名称" min-width="80">
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                    <p class="id">{{ scope.row.service_id }}</p>
                </template>
            </el-table-column>
            <el-table-column label="客户名称" min-width="80">
                <template slot-scope="scope">
                    <p>{{ scope.row.client_name }}</p>
                    <p class="id">{{ scope.row.client_id }}</p>
                </template>
            </el-table-column>
            <el-table-column label="服务类型" min-width="50">
                <template slot-scope="scope">
                    <p>{{ serviceType[scope.row.service_type] }}</p>
                </template>
            </el-table-column>

            <el-table-column label="总调用次数" min-width="50">
                <template slot-scope="scope">
                    <p>{{ scope.row.total_request_times }}</p>
                </template>
            </el-table-column>

            <el-table-column label="总成功次数" min-width="50">
                <template slot-scope="scope">
                    <p>{{ scope.row.total_success_times }}</p>
                </template>
            </el-table-column>

            <el-table-column label="总失败次数" min-width="50">
                <template slot-scope="scope">
                    <p>{{ scope.row.total_fail_times }}</p>
                </template>
            </el-table-column>

            <el-table-column label="总耗时(s)" min-width="50">
                <template slot-scope="scope">
                    <p>{{ scope.row.total_spend }}</p>
                </template>
            </el-table-column>

            <el-table-column label="操作" min-width="40">

                <template slot-scope="scope">
                    <el-button type="primary" @click="getDetails(scope.row.service_id,scope.row.client_id)">
                        详情
                    </el-button>


                </template>
            </el-table-column>
        </el-table>

        <el-dialog title="调用详情" :visible.sync="dialogTableVisible" width="70%">
            <el-table :data="apiCallDetails">
                <el-table-column label="服务名称" min-width="30">
                    <template slot-scope="scope">
                        <p>{{ scope.row.service_name }}</p>
                    </template>
                </el-table-column>
                <el-table-column label="客户名称" min-width="30">
                    <template slot-scope="scope">
                        <p>{{ scope.row.client_name }}</p>
                    </template>
                </el-table-column>
                <el-table-column label="服务类型" min-width="30">
                    <template slot-scope="scope">
                        <p>{{ serviceType[scope.row.service_type] }}</p>
                    </template>
                </el-table-column>
                <el-table-column label="调用时间" min-width="40">
                    <template slot-scope="scope">
                        <p>{{ scope.row.created_time | dateFormat }}</p>
                    </template>
                </el-table-column>

                <el-table-column label="耗时(s)" min-width="30">
                    <template slot-scope="scope">
                        <p>{{ scope.row.spend }}</p>
                    </template>
                </el-table-column>
                <el-table-column label="IP 白名单" min-width="40">
                    <template slot-scope="scope">
                        <p>{{ scope.row.ip_add }}</p>
                    </template>
                </el-table-column>
                <el-table-column label="请求结果" min-width="30">
                    <template slot-scope="scope">
                        <p>{{ requestResult[scope.row.request_result] }}</p>
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
        </el-dialog>


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

import table from '@src/mixins/table';
import RoleTag from "../components/role-tag";

export default {
    name: "request-statistics",
    components: {
        RoleTag,
    },
    mixins: [table],
    data() {
        return {
            services: [],
            clients: [],
            search: {
                serviceId: '',
                clientId: '',
                startTime: '',
                endTime: '',
            },
            defaultTime: [
                '',
                '',
            ],
            getListApi: '/requeststatistics/query-list',
            serviceType: {
                1: "匿踪查询",
                2: "交集查询",
                3: "安全聚合(被查询方)",
                4: "安全聚合(查询方)",
            },
            requestResult: {
                1: "成功",
                0: "失败"
            },
            apiCallDetails: [],
            dialogTableVisible: false,
        }
    },

    created() {

        this.defaultTime[0] = new Date(new Date().getFullYear() + '-'
            + new Date().getMonth() + 1 + '-'
            + new Date().getDate() + ' 00:00:00')

        this.defaultTime[1] = new Date(new Date().getFullYear() + '-'
            + new Date().getMonth() + 1 + '-'
            + new Date().getDate() + ' 23:59:59')

        this.getServices()
        this.getClients()
    },

    methods: {
        timeChange() {
            this.search.startTime = this.defaultTime[0]
            this.search.endTime = this.defaultTime[1]
        },

        handleServices(data) {
            for (let i = 0; i < data.length; i++) {
                this.services.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        },

        handleClients(data) {
            for (let i = 0; i < data.length; i++) {
                this.clients.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        },

        async getServices() {
            const {code, data} = await this.$http.post({
                url: '/service/query',
            });

            if (code === 0) {
                this.handleServices(data.list)
            }
        },

        async getClients() {
            const {code, data} = await this.$http.post({
                url: '/client/query-list',
            });

            if (code === 0) {
                this.handleClients(data.list)
            }
        },


        async getDetails(serviceId, clientId) {
            this.apiCallDetails = ''
            const {code, data} = await this.$http.post({
                url: '/apirequestrecord/query-list',
                data: {
                    serviceId: serviceId,
                    clientId: clientId
                }
            });

            if (code === 0) {
                this.apiCallDetails = data.list
                this.dialogTableVisible = true
            }
        }
    }
}
</script>

<style scoped>

</style>
