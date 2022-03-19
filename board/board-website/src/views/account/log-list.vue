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
                <el-input v-model="search.logInterface" clearable />
            </el-form-item>
            <el-form-item
                label="操作人："
                label-width="120"
            >
                <el-select
                    v-model="search.operatorId"
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
                <DateTimePicker
                    type="datetimerange"
                    ref="dateTimePicker"
                    valueFormat="x"
                    clearable
                    @change="datePickerChange"
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    native-type="submit"
                    @click="getList({ to: true, resetPagination: true })"
                >
                    查询
                </el-button>
            </el-form-item>
        </el-form>

        <el-table
            :data="list"
            v-loading="loading"
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
            />
            <el-table-column label="时间" width="140px">
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
                    logInterface: '',
                    operatorId:   '',
                    startTime:    '',
                    endTime:      '',
                },
                getListApi:   '/log/query',
                fillUrlQuery: false,
                userList:     [],
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
                    logInterface: '',
                    operatorId:   '',
                    startTime:    '',
                    endTime:      '',
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
