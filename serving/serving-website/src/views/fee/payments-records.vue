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
                    placeholder="请选择"
                >
                    <el-option
                        v-for="item in serviceTypes"
                        :key="item.value"
                        :label="item.name"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="收支类型：">
                <el-select
                    v-model="search.payType"
                    clearable
                    placeholder="请选择"
                >
                    <el-option
                        v-for="item in payTypes"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="创建时间：">
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
                @click="getList({ to: true})"
            >
                查询
            </el-button>

            <router-link
                class="ml10"
                :to="{name: 'payments-records-add'}"
            >
                <el-button>
                    新增
                </el-button>
            </router-link>


            <el-button
                class="ml10"
                @click="downloadPaymentsRecords"
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
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                    <p class="id">{{ scope.row.service_id }}</p>
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
                label="客户名称"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.client_name }}</p>
                    <p class="id">{{ scope.row.client_id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="收支类型"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ payType[scope.row.pay_type] }}</p>
                </template>
            </el-table-column>


            <el-table-column
                label="日期"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.created_time | dateFormat }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="金额(￥)"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.amount }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="余额(￥)"
                min-width="50"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.balance }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="备注"
                min-width="40"
            >
                <template slot-scope="scope">
                    <el-tooltip
                        class="item"
                        effect="dark"
                        :content="scope.row.remark"
                        placement="left-start"
                    >
                        <p v-if="scope.row.remark.length >= 10">{{ scope.row.remark.substring(0, 10) }} ...</p>
                        <p v-if="scope.row.remark.length < 10">{{ scope.row.remark }} </p>
                    </el-tooltip>
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
    name:   'PaymentsRecords',
    mixins: [table],
    data() {
        return {
            search: {
                serviceName: '',
                clientName:  '',
                serviceType: '',
                payType:     '',
                startTime:   '',
                endTime:     '',
            },
            timeRange:   '',
            getListApi:  '/paymentsrecords/query-list',
            serviceType: {
                1: '两方匿踪查询',
                2: '两方交集查询',
                3: '多方安全统计(被查询方)',
                4: '多方安全统计(查询方)',
                5: '多方交集查询',
                6: '多方匿踪查询',
            },
            payType: {
                1: '充值',
                2: '支出',
            },
            status: {
                1: '正常',
                2: '冲正',
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
            payTypes: [
                { value: '1', label: '充值' },
                { value: '2', label: '支出' },
            ],
        };
    },
    computed: {
        ...mapGetters(['userInfo']),
    },
    methods: {
        downloadPaymentsRecords() {

            const api = `${window.api.baseUrl}/paymentsrecords/download?serviceName=${this.search.serviceName}&clientName=${this.search.clientName}&startTime=${this.search.startTime}&endTime=${this.search.endTime}&payType=${this.search.payType}&serviceType=${this.search.serviceType}&token=${this.userInfo.token}`;
            const link = document.createElement('a');

            link.href = api;
            link.target = '_blank';
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();

        },


        timeChange() {
            if (!this.timeRange) {
                this.search.startTime = '';
                this.search.endTime = '';
            } else {
                this.search.startTime = this.timeRange[0];
                this.search.endTime = this.timeRange[1];
            }
        },
    },
};
</script>

<style scoped>

</style>
