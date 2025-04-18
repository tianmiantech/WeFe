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
                <el-input
                    v-model="search.serviceName"
                    clearable
                />
            </el-form-item>

            <el-form-item label="请求方名称：">
                <el-input
                    v-model="search.requestPartnerName"
                    clearable
                />
            </el-form-item>

            <el-form-item label="响应方名称：">
                <el-input
                    v-model="search.responsePartnerName"
                    clearable
                />
            </el-form-item>

            <el-form-item label="统计粒度：">
                <el-select
                    v-model="search.statisticalGranularity"
                    clearable
                    placeholder="请选择(默认分钟)"
                >
                    <el-option
                        v-for="item in statistical_granularity"
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
                        order-statistics
                        @change="timeChange()"
                    />
                </div>
            </el-form-item>

            <el-button
                class="ml10"
                type="primary"
                @click="getList({resetPagination: true})"
            >
                查询
            </el-button>

            <el-button
                class="ml10"
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
                <TableEmptyData />
            </div>

            <el-table-column
                label="序号"
                min-width="50"
                type="index"
            />

            <el-table-column
                label="服务名称"
                min-width="100"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                    <p class="id">{{ scope.row.service_id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="请求方名称"
                min-width="120"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.request_partner_name }}</p>
                    <p class="id">{{ scope.row.request_partner_id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="响应方名称"
                min-width="120"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.response_partner_name }}</p>
                    <p class="id">{{ scope.row.response_partner_id }}</p>
                </template>
            </el-table-column>


            <el-table-column
                label="总成功次数"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.success_times }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="总失败次数"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.failed_times }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="总请求次数"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.call_times }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="调用时间"
                min-width="80"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.date_time }}</p>
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
import table from '@src/mixins/table';
import { mapGetters } from 'vuex';
import { downLoadFileTool } from '@src/utils/tools';

export default {
    name:   'OrderStatistics',
    mixins: [table],
    data() {
        return {
            loading:    false,
            list:       [],
            services:   [],
            clients:    [],
            getListApi: '/orderstatistics/query-list',
            search:     {
                serviceName:            '',
                requestPartnerName:     '',
                responsePartnerName:    '',
                statisticalGranularity: 'minute',
                startTime:              '',
                endTime:                '',
            },
            defaultTime:      [],
            dialogPagination: {
                total:       '',
                page_size:   10,
                page_index:  1,
                serviceId:   '',
                clientId:    '',
                change_flag: false,
            },

            statistical_granularity: [
                { value: 'month', label: '月' },
                { value: 'day', label: '日' },
                { value: 'hour', label: '小时' },
                { value: 'minute', label: '分钟' },
            ],

            apiCallDetails: [],

            dialogTableVisible: false,
            defaultSearch:      false,
        };
    },

    created() {

        this.search.statisticalGranularity = 'minute';
        this.getList();
    },

    computed: {
        ...mapGetters(['userInfo']),
    },

    methods: {
        dialogCurrentPageChange(val) {
            this.dialogPagination.change_flag = true;
            this.dialogPagination.page_index = val;
            this.getDetails(this.dialogPagination.serviceId, this.dialogPagination.clientId, this.dialogPagination.change_flag);
        },

        dialogCurrentPageSizeChange(val) {
            this.dialogPagination.change_flag = true;
            this.dialogPagination.page_size = val;
            this.dialogPagination.page_index = 1;
            this.getDetails(this.dialogPagination.serviceId, this.dialogPagination.clientId, this.dialogPagination.change_flag);
        },


        downloadStatistics() {
            downLoadFileTool('/orderstatistics/download', {
                serviceName:         this.search.serviceName,
                requestPartnerName:  this.search.requestPartnerName,
                responsePartnerName: this.search.responsePartnerName,
                orderType:           this.search.orderType,
                startTime:           this.search.startTime,
                endTime:             this.search.endTime,
                version:             Math.random(),
            });
        },


        timeChange() {
            if (!this.defaultTime) {
                this.search.startTime = '';
                this.search.endTime = '';
            } else {
                this.search.startTime = this.defaultTime[0];
                this.search.endTime = this.defaultTime[1];
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
            const { code, data } = await this.$http.post({
                url:  '/service/query',
                data: {
                    status: 1,
                },
            });

            if (code === 0) {
                this.handleServices(data.list);
            }
        },

        async getClients() {
            const { code, data } = await this.$http.post({
                url: '/client/query-list',
            });

            if (code === 0) {
                this.handleClients(data.list);
            }
        },


        async getDetails(serviceId, clientId, change_flag) {

            this.dialogPagination.serviceId = serviceId;
            this.dialogPagination.clientId = clientId;

            this.apiCallDetails = [];
            const { code, data } = await this.$http.post({
                url:  '/orderstatistics/query-list',
                data: {
                    serviceId:  this.dialogPagination.serviceId,
                    clientId:   this.dialogPagination.clientId,
                    page_index: change_flag ? this.dialogPagination.page_index - 1 : 0,
                    page_size:  change_flag ? this.dialogPagination.page_size : 10,
                    startTime:  this.search.startTime,
                    endTime:    this.search.endTime,
                },
            });

            if (code === 0) {
                this.apiCallDetails = data.list;
                this.dialogTableVisible = true;
                this.dialogPagination.total = data.total;

            }
        },
    },
};
</script>

<style scoped>

</style>
