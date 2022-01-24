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
                    placeholder="服务名称"
                />
            </el-form-item>

            <el-form-item label="客户名称：">
                <el-input
                    v-model="search.clientName"
                    clearable
                    placeholder="客户名称"
                />
            </el-form-item>

            <el-form-item label="服务类型：">
                <el-select
                    v-model="search.serviceType"
                    clearable
                    placeholder="请选择服务类型"
                >
                    <el-option
                        v-for="item in serviceTypes"
                        :key="item.value"
                        :label="item.name"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="统计方式：">
                <el-select
                    v-model="search.queryDateType"
                    clearable
                    placeholder="请选择统计方式"
                >
                    <el-option
                        v-for="item in queryDateTypes"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="时间范围：">
                <div class="block">
                    <el-date-picker
                        v-model="timeRange"
                        type="datetimerange"
                        range-separator="-"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        value-format="timestamp"
                        @change="timeChange"
                    />
                </div>
            </el-form-item>


            <el-button
                type="primary"
                @click="getList({ to: true })"
            >
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
                <TableEmptyData />
            </div>

            <el-table-column
                label="序号"
                min-width="50"
                type="index"
            />

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
                label="日期(默认按月)"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.query_date }}</p>
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
                label="单价(￥)/次"
                min-width="40"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.unit_price }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="付费类型"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ payTypes[scope.row.pay_type] }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="总计(￥)"
                min-width="60"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.total_fee }}</p>
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

export default {
    name:   'FeeDetail',
    mixins: [table],
    data() {
        return {
            services: [],
            clients:  [],
            search:   {
                serviceName:   '',
                clientName:    '',
                serviceType:   '',
                queryDateType: '',
                startTime:     '',
                endTime:       '',
            },
            timeRange:   '',
            serviceId:   '',
            clientId:    '',
            startTime:   '',
            endTime:     '',
            getListApi:  '/feedetail/query-list',
            serviceType: {
                1: '两方匿踪查询',
                2: '两方交集查询',
                3: '多方安全统计(被查询方)',
                4: '多方安全统计(查询方)',
                5: '多方交集查询',
                6: '多方匿踪查询',
            },
            serviceTypes: [
                {
                    name:  '两方匿踪查询',
                    value: '1',
                },
                {
                    name:  '多方匿踪查询',
                    value: '6',
                },
                {
                    name:  '两方交集查询',
                    value: '2',
                },
                {
                    name:  '多方交集查询',
                    value: '5',
                },
                {
                    name:  '多方安全统计(查询方)',
                    value: '4',
                },
                {
                    name:  '多方安全统计(被查询方)',
                    value: '3',
                },
            ],
            queryDateTypes: [
                { value: '1', label: '每年' },
                { value: '2', label: '每月' },
                { value: '3', label: '每日' },
                { value: '4', label: '每小时' },
            ],
            payTypes: {
                1: '预付费',
                0: '后付费',
            },
        };
    },

    created() {
        this.getServices();
        this.getClients();
    },

    methods: {

        timeChange() {
            if (!this.timeRange) {
                this.search.startTime = ''
                this.search.endTime = ''
            } else {
                this.search.startTime = this.timeRange[0]
                this.search.endTime = this.timeRange[1]
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
    },
};
</script>

<style scoped>

</style>
