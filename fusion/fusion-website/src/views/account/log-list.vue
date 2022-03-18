<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            @submit.prevent
        >
            <el-form-item label="接口">
                <el-input
                    v-model="search.api_name"
                    clearable
                />
            </el-form-item>
            <el-form-item label="操作人">
                <el-input
                    v-model="search.caller_name"
                    maxlength="11"
                    clearable
                />
            </el-form-item>
            <el-form-item label="起止时间">
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
                label="接口"
                prop="api_name"
                width="200"
            >
                <template v-slot="scope">
                    {{ scope.row.api_name }}
                    <br>
                    {{ scope.row.log_interface }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作人"
                prop="caller_name"
                min-width="250"
            >
                <template v-slot="scope">
                    {{ scope.row.caller_name }}
                    <br>
                    {{ scope.row.caller_id }}
                </template>
            </el-table-column>
            <el-table-column
                label="请求结果编码"
                prop="response_code"
            />
            <el-table-column
                label="请求 IP"
                prop="caller_ip"
                min-width="100"
            />
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
                    api_name:    '',
                    caller_name: '',
                    startTime:   '',
                    endTime:     '',
                },
                getListApi:   '/log/query',
                fillUrlQuery: false,
                time:         '',
            };
        },
        mounted() {
            this.syncUrlParams();
            this.getList();
        },
        methods: {
            syncUrlParams() {
                this.search = {
                    api_name:    '',
                    caller_name: '',
                    startTime:   '',
                    endTime:     '',
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
        },
    };
</script>
