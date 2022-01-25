<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item label="服务名称：">
                <el-select
                    v-model="search.serviceId"
                    filterable
                    clearable
                    placeholder="请选择服务"
                >
                    <el-option
                        v-for="item in services"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="客户名称：">
                <el-select
                    v-model="search.clientId"
                    filterable
                    clearable
                    placeholder="请选择客户"
                >
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="调用时间：">
                <div class="block">
                    <el-date-picker
                        v-model="defaultTime"
                        type="datetimerange"
                        range-separator="-"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        value-format="timestamp"
                        @change="timeChange()"
                    />
                </div>
            </el-form-item>

            <el-button
                type="primary"
                @click="getList({ to: true})"
            >
                查询
            </el-button>

            <el-button
                @click="downloadStatistics"
            >
                下载
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

            <el-table-column
                label="序号"
                min-width="50"
                type="index"
            ></el-table-column>

            <el-table-column
                label="服务名称"
                min-width="80"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                    <p class="id">{{ scope.row.service_id }}</p>

                </template>
            </el-table-column>
            <el-table-column
                label="客户名称"
                min-width="80"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.client_name }}</p>
                    <p class="id">{{ scope.row.client_id }}</p>

                </template>
            </el-table-column>
            <el-table-column
                label="服务类型"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ serviceType[scope.row.service_type] }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="总调用次数"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.total_request_times }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="总成功次数"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.total_success_times }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="总失败次数"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.total_fail_times }}</p>
                </template>
            </el-table-column>


            <el-table-column
                label="操作"
                min-width="40"
            >
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        @click="getDetails(scope.row.service_id,scope.row.client_id)"
                    >
                        详情
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog
            title="调用详情"
            :visible.sync="dialogTableVisible"
            width="70%"
        >
            <el-table :data="apiCallDetails">
                <el-table-column
                    label="服务名称"
                    min-width="30"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.service_name }}</p>
                    </template>
                </el-table-column>
                <el-table-column
                    label="客户名称"
                    min-width="30"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.client_name }}</p>
                    </template>
                </el-table-column>
                <el-table-column
                    label="服务类型"
                    min-width="30"
                >
                    <template slot-scope="scope">
                        <p>{{ serviceType[scope.row.service_type] }}</p>
                    </template>
                </el-table-column>
                <el-table-column
                    label="调用时间"
                    min-width="40"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.created_time | dateFormat }}</p>
                    </template>
                </el-table-column>

                <el-table-column
                    label="调用 IP"
                    min-width="40"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.ip_add }}</p>
                    </template>
                </el-table-column>

                <el-table-column
                    label="请求结果"
                    min-width="30"
                >
                    <template slot-scope="scope">
                        <p>{{ requestResult[scope.row.request_result] }}</p>
                    </template>
                </el-table-column>
            </el-table>

            <div
                v-if="dialogPagination.total"
                class="mt20 text-r"
            >
                <el-pagination
                    :total="dialogPagination.total"
                    :page-sizes="[10, 20, 30, 40, 50]"
                    :page-size="dialogPagination.page_size"
                    :current-page="dialogPagination.page_index"
                    layout="total, sizes, prev, pager, next, jumper"
                    @current-change="dialogCurrentPageChange"
                    @size-change="dialogCurrentPageSizeChange"
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
import {mapGetters} from 'vuex';

export default {
    name: 'RequestStatistics',
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
            dialogPagination: {
                total: '',
                page_size: 10,
                page_index: 1,
                serviceId: '',
                clientId: '',
            },
            getListApi: '/requeststatistics/query-list',
            serviceType: {
                1: '两方匿踪查询',
                2: '两方交集查询',
                3: '多方安全统计(被查询方)',
                4: '多方安全统计(查询方)',
                5: '多方交集查询',
                6: '多方匿踪查询',
            },
            requestResult: {
                1: '成功',
                0: '失败',
            },
            apiCallDetails: [],

            dialogTableVisible: false,
        };
    },

    created() {

        this.defaultTime[0] = new Date(new Date().getFullYear() + '-'
            + new Date().getMonth() + 1 + '-'
            + new Date().getDate() + ' 00:00:00');

        this.defaultTime[1] = new Date(new Date().getFullYear() + '-'
            + new Date().getMonth() + 1 + '-'
            + new Date().getDate() + ' 23:59:59');

        this.getServices();
        this.getClients();
    },

    computed: {
        ...mapGetters(['userInfo']),
    },

    methods: {

        dialogCurrentPageChange(val) {
            this.dialogPagination.page_index = val
            this.getDetails(this.dialogPagination.serviceId, this.dialogPagination.clientId)
        },

        dialogCurrentPageSizeChange(val) {
            this.dialogPagination.page_size = val;
            this.getDetails(this.dialogPagination.serviceId, this.dialogPagination.clientId);
        },


        downloadStatistics() {

            const api = `${window.api.baseUrl}/apirequestrecord/download?serviceId=${this.search.serviceId}&clientId=${this.search.clientId}&startTime=${this.search.startTime}&endTime=${this.search.endTime}&token=${this.userInfo.token}`;
            const link = document.createElement('a');

            link.href = api;
            link.target = '_blank';
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();

        },


        timeChange() {
            if (!this.defaultTime) {
                this.search.startTime = ''
                this.search.endTime = ''
            } else {
                this.search.startTime = this.defaultTime[0]
                this.search.endTime = this.defaultTime[1]
            }
        },

        handleServices(data) {
            for (let i = 0; i < data.length; i++) {
                this.services.push({
                    label: data[i].name,
                    value: data[i].id,
                });
            }
        },

        handleClients(data) {
            for (let i = 0; i < data.length; i++) {
                this.clients.push({
                    label: data[i].name,
                    value: data[i].id,
                });
            }
        },

        async getServices() {
            const {code, data} = await this.$http.post({
                url: '/service/query',
                data: {
                    status: 1,
                }
            });

            if (code === 0) {
                this.handleServices(data.list);
            }
        },

        async getClients() {
            const {code, data} = await this.$http.post({
                url: '/client/query-list',
            });

            if (code === 0) {
                this.handleClients(data.list);
            }
        },


        async getDetails(serviceId, clientId) {

            this.dialogPagination.serviceId = serviceId
            this.dialogPagination.clientId = clientId


            this.apiCallDetails = '';
            const {code, data} = await this.$http.post({
                url: '/apirequestrecord/query-list',
                data: {
                    serviceId: this.dialogPagination.serviceId,
                    clientId: this.dialogPagination.clientId,
                    page_index: this.dialogPagination.page_index - 1,
                    page_size: this.dialogPagination.page_size
                },
            });

            if (code === 0) {
                this.apiCallDetails = data.list
                this.dialogTableVisible = true
                this.dialogPagination.total = data.total

            }
        },
    },
};
</script>

<style scoped>

</style>
