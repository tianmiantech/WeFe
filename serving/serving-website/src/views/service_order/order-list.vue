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
                <!--                <el-select-->
                <!--                    v-model="search.serviceId"-->
                <!--                    filterable-->
                <!--                    clearable-->
                <!--                    placeholder="请选择服务"-->
                <!--                >-->
                <!--                    <el-option-->
                <!--                        v-for="item in services"-->
                <!--                        :key="item.value"-->
                <!--                        :label="item.label"-->
                <!--                        :value="item.value"-->
                <!--                    />-->
                <!--                </el-select>-->
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

            <el-form-item label="时间：">
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

            <el-button class="ml10"
                       type="primary"
                       @click="getList({ to: true})"
            >
                查询
            </el-button>

            <el-button class="ml10"
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
                width="50"
                type="index"
            ></el-table-column>

            <el-table-column
                label="订单号"
                min-width="240"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="服务名称"
                min-width="230"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                    <p class="id">{{ scope.row.service_id }}</p>

                </template>
            </el-table-column>
            <el-table-column
                label="请求方名称"
                min-width="230"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.request_partner_name }}</p>
                    <p class="id">{{ scope.row.request_partner_id }}</p>

                </template>
            </el-table-column>


            <el-table-column
                label="响应方名称"
                min-width="230"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.response_partner_name }}</p>
                    <p class="id">{{ scope.row.response_partner_id }}</p>

                </template>
            </el-table-column>

            <el-table-column
                label="服务类型"
                min-width="100"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_type }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="订单状态"
                min-width="100"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.status }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="是否为我方发起"
                min-width="120"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.order_type }}</p>
                </template>
            </el-table-column>


            <el-table-column
                label="操作"
                min-width="80"
                align="center"
                fixed="right"
            >
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        @click="getDetails(scope.row.id)"
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
                    label="序号"
                    min-width="40"
                    type="index"
                ></el-table-column>

                <el-table-column
                    label="请求Id"
                    min-width="120"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.request_id }}</p>
                    </template>
                </el-table-column>
                <el-table-column
                    label="请求数据"
                    min-width="200"
                >
                    <template v-slot="scope">
                        <template v-if="scope.row.request_data">
                            <p>{{
                                    scope.row.request_data.length > 100 ? scope.row.request_data.substring(0, 101) + '...' : scope.row.request_data
                                }}</p>
                            <el-button
                                v-if="scope.row.request_data.length > 100"
                                type="text"
                                @click="showRequest(scope.row.request_data)"
                            >
                                查看更多
                            </el-button>
                        </template>
                    </template>
                    <!--                    <template slot-scope="scope">-->
                    <!--                        <p>{{ scope.row.request_data }}</p>-->
                    <!--                    </template>-->
                </el-table-column>
                <el-table-column
                    label="响应Id"
                    min-width="120"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.response_id }}</p>
                    </template>
                </el-table-column>

                <el-table-column
                    label="响应数据"
                    min-width="200"
                >
                    <template v-slot="scope">
                        <template v-if="scope.row.response_data">
                            <p>{{
                                    scope.row.response_data.length > 100 ? scope.row.response_data.substring(0, 101) + '...' : scope.row.response_data
                                }}</p>
                            <el-button
                                v-if="scope.row.response_data.length > 100"
                                type="text"
                                @click="showResponse(scope.row.response_data)"
                            >
                                查看更多
                            </el-button>
                        </template>
                    </template>
                    <!--                    <template slot-scope="scope">-->
                    <!--                        <p>{{ scope.row.response_data }}</p>-->
                    <!--                    </template>-->
                </el-table-column>

                <el-table-column
                    label="请求方IP"
                    min-width="80"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.request_ip }}</p>
                    </template>
                </el-table-column>

                <el-table-column
                    label="调用时间"
                    min-width="120"
                >
                    <template slot-scope="scope">
                        <p>{{ scope.row.created_time | dateFormat }}</p>
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


        <el-dialog
            :title="title"
            :visible.sync="requestDataDialog"
        >
            <JsonViewer
                :value="jsonData"
                :expand-depth="5"
                copyable
            />
        </el-dialog>
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
            requestDataDialog: false,
            jsonData: '',
            title: '',
            services: [],
            clients: [],
            search: {
                serviceName: '',
                requestPartnerName: '',
                responsePartnerName: '',
                orderType: 0,
                startTime: '',
                endTime: '',
            },
            defaultTime: [],
            dialogPagination: {
                total: '',
                page_size: 10,
                page_index: 1,
                serviceId: '',
                id: '',  // orderId
                clientId: '',
                change_flag: false,
            },
            getListApi: '/serviceorder/query-list',
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
                2: '成功(没有数据)',
                3: '失败(服务不可用)',
                4: '失败(服务未授权)',
                5: '失败(IP被限制)',
                6: '失败(服务异常)',
            },
            apiCallDetails: [],

            dialogTableVisible: false,
        };
    },

    created() {

        // this.defaultTime[0] = new Date(new Date().getFullYear() + '-'
        //     + new Date().getMonth() + 1 + '-'
        //     + new Date().getDate() + ' 00:00:00');
        //
        // this.defaultTime[1] = new Date(new Date().getFullYear() + '-'
        //     + new Date().getMonth() + 1 + '-'
        //     + new Date().getDate() + ' 23:59:59');

        // this.getServices();
        // this.getClients();
    },

    computed: {
        ...mapGetters(['userInfo']),
    },

    methods: {
        showRequest(data) {
            this.requestDataDialog = true;
            this.title = '请求体';
            setTimeout(() => {
                this.jsonData = JSON.parse(data);
            });
        },

        showResponse(data) {
            this.requestDataDialog = true;
            this.title = '响应体';
            setTimeout(() => {
                this.jsonData = JSON.parse(data);
            });
        },

        dialogCurrentPageChange(val) {
            this.dialogPagination.change_flag = true
            this.dialogPagination.page_index = val
            this.getDetails(this.dialogPagination.id, this.dialogPagination.change_flag)
        },

        dialogCurrentPageSizeChange(val) {
            this.dialogPagination.change_flag = true
            this.dialogPagination.page_size = val
            this.dialogPagination.page_index = 1
            this.getDetails(this.dialogPagination.id, this.dialogPagination.change_flag)
        },


        downloadStatistics() {

            const api = `${window.api.baseUrl}/serviceorder/download?serviceName=${this.search.serviceName}&requestPartnerName=${this.search.requestPartnerName}&responsePartnerName=${this.search.responsePartnerName}&orderType=${this.search.orderType}&startTime=${this.search.startTime}&endTime=${this.search.endTime}&token=${this.userInfo.token}`;
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


        async getDetails(id, change_flag) {

            // this.dialogPagination.serviceId = serviceId
            // this.dialogPagination.clientId = clientId
            this.dialogPagination.id = id

            this.apiCallDetails = []
            const {code, data} = await this.$http.post({
                url: '/servicecalllog/query-list',
                data: {
                    serviceId: this.dialogPagination.serviceId,
                    orderId: this.dialogPagination.id,
                    page_index: change_flag ? this.dialogPagination.page_index - 1 : 0,
                    page_size: change_flag ? this.dialogPagination.page_size : 10,
                    // startTime: this.search.startTime,
                    // endTime: this.search.endTime
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
