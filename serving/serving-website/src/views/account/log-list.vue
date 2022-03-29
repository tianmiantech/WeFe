<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            @submit.prevent
        >
            <el-form-item label="请求接口：">
                <el-input
                    v-model="search.log_interface"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="操作人："
                label-width="120"
            >
                <el-select
                    v-model="search.operator_id"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="(user, index) in userList"
                        :key="index"
                        :label="user.nickname"
                        :value="user.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="起止时间:">
                <el-date-picker
                    v-model="time"
                    type="daterange"
                    range-separator="-"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="yyyy-MM-dd"
                    value-format="timestamp"
                    @change="datePickerChange"
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    native-type="button"
                    @click="getList({ to: true, resetPagination: true })"
                >
                    查询
                </el-button>
            </el-form-item>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            class="mt20"
            border
            stripe
        >
            <el-table-column
                label="请求接口"
                prop="interface_name"
                width="200"
            >
                <template v-slot="scope">
                    {{ scope.row.interface_name }}
                    <br>
                    {{ scope.row.log_interface }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作人"
                prop="operator_nickname"
                min-width="230"
            >
                <template v-slot="scope">
                    {{ scope.row.operator_nickname }}
                    <br>
                    {{ scope.row.operator_id }}
                </template>
            </el-table-column>
            <el-table-column
                label="请求结果编码"
                prop="result_code"
            />
            <el-table-column
                label="请求 IP"
                prop="request_ip"
                min-width="100"
            />
            <el-table-column
                label="响应信息"
                prop="result_message"
                min-width="100"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.result_message">
                        <p>{{ scope.row.result_message.length > 100 ? scope.row.result_message.substring(0, 101) + '...' : scope.row.result_message }}</p>
                        <el-button
                            v-if="scope.row.response_message && scope.row.response_message.length > 100"
                            type="primary"
                            size="mini"
                            @click="checkLog($event, scope.row)"
                        >
                            查看更多
                        </el-button>
                    </template>
                    <template v-else>
                        success
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="时间"
                width="140px"
            >
                <template v-slot="scope">
                    {{ scope.row.created_time | dateFormat }}
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

    export default {
        mixins: [table],
        data() {
            return {
                types: [{
                    label: '',
                    type:  '',
                }],
                datePicker: '',
                search:     {
                    log_interface:          '',
                    operator_id:            '',
                    startTime:              '',
                    endTime:                '',
                    'request-from-refresh': false,
                },
                getListApi:   '/operation_log/query',
                fillUrlQuery: false,
                userList:     [],
                time:         '',
            };
        },
        mounted() {
            this.getUploaders();
            this.syncUrlParams();
            this.getList();
        },
        methods: {
            async getUploaders() {
                const { code, data } = await this.$http.get('/account/query');

                if (code === 0) {
                    this.userList = data.list;
                }
            },
            syncUrlParams() {
                this.search = {
                    log_interface:          '',
                    operator_id:            '',
                    startTime:              '',
                    endTime:                '',
                    'request-from-refresh': false,
                    ...this.$route.query,
                };
                if(this.search.startTime && this.search.endTime) {
                    this.time = [this.search.startTime, this.search.endTime];
                }
            },
            datePickerChange(val) {
                if(val) {
                    this.search.startTime = val[0];
                    this.search.endTime = val[1];
                } else {
                    this.search.startTime = '';
                    this.search.endTime = '';
                }
            },
            checkLog(event, row) {
                this.$alert(row.response_message, '响应信息', {
                    confirmButtonText: '确定',
                });
            },
        },
    };
</script>
