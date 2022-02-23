<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            @submit.native.prevent
        >
            <el-form-item label="操作类型">
                <el-input
                    v-model="search.action"
                    clearable
                />
            </el-form-item>
            <el-form-item label="操作人手机号">
                <el-input
                    v-model="search.operator_phone"
                    maxlength="11"
                    clearable
                />
            </el-form-item>
            <el-form-item label="起止时间">
                <DateTimePicker
                    ref="dateTimePicker"
                    type="datetimerange"
                    value-format="x"
                    clearable
                    @change="datePickerChange"
                />
            </el-form-item>
            <el-button
                type="primary"
                @click="getList({ to: true, resetPagination: true })"
            >
                查询
            </el-button>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            class="mt20"
            border
            stripe
        >
            <el-table-column
                label="接口"
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
                prop="operator_phone"
                min-width="250"
            >
                <template v-slot="scope">
                    {{ scope.row.operator_phone }}
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
                label="操作类型"
                prop="log_action"
                min-width="120"
            />
            <el-table-column
                label="时间"
                width="140px"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.created_time) }}
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
                    action:         '',
                    operator_phone: '',
                    startTime:      '',
                    endTime:        '',
                },
                getListApi:   '/log/query',
                fillUrlQuery: false,
            };
        },
        mounted() {
            this.syncUrlParams();
            this.getList();
        },
        methods: {
            syncUrlParams() {
                this.search = {
                    action:         '',
                    operator_phone: '',
                    startTime:      '',
                    endTime:        '',
                    ...this.$route.query,
                };
                if(this.search.startTime && this.search.endTime) {
                    this.$refs['dateTimePicker'].vData.value = [this.search.startTime, this.search.endTime];
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
        },
    };
</script>
