<template>
    <el-card class="page">
        <el-form>
            <el-form-item label="起止时间:">
                <el-date-picker
                    v-model="timeRange"
                    type="datetimerange"
                    range-separator="-"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="timestamp"
                    @change="timeChange"
                />
                <el-button
                    class="ml10"
                    type="primary"
                    @click="getList('to')"
                >
                    查询
                </el-button>
                <el-button
                    class="ml30"
                    @click="downloadLog"
                >
                    下载
                </el-button>
            </el-form-item>
        </el-form>

        <el-table
            :data="list"
            class="mt10"
            border
            stripe
        >
            <el-table-column
                label="流水号"
                prop="id"
            />
            <el-table-column label="时间">
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column label="类型">
                <template slot-scope="scope">
                    {{ scope.row.type }}
                </template>
            </el-table-column>
            <el-table-column label="充值（￥）">
                <template slot-scope="scope">
                    {{ scope.row.income }}
                </template>
            </el-table-column>
            <el-table-column label="支出（￥）">
                <template slot-scope="scope">
                    {{ scope.row.output }}
                </template>
            </el-table-column>
            <el-table-column label="余额（￥）">
                <template slot-scope="scope">
                    {{ scope.row.remain }}
                </template>
            </el-table-column>
            <el-table-column label="备注">
                <template slot-scope="scope">
                    {{ scope.row.mark }}
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
    mixins: [table],
    data() {
        return {
            search: {
                startTime: '',
                endTime:   '',
            },
            timeRange:  '',
            getListApi: '/fee/query-list',
            list:       [{
                id:           '5e2e9d44704f49deb82654418a66b282',
                created_time: 1641886026000,
                type:         '匿综查询',
                income:       '1000',
                output:       '230',
                remain:       '870',
                mark:         '-',
            }, {
                id:           'd15c002ec6894d94b06fd5403a1fec33',
                created_time: 1641885620000,
                type:         '交集查询',
                income:       '400',
                output:       '500',
                remain:       '100',
                mark:         '-',
            }, {
                id:           '7076ec98634c42e2ad92e3915d6c60a6',
                created_time: 1641884621000,
                type:         '安全聚合 (被查询方)',
                income:       '300',
                output:       '130',
                remain:       '200',
                mark:         '-',
            }, {
                id:           'b4bb1c9590ed43529d94e091818732ed',
                created_time: 1641882624000,
                type:         '安全聚合 (查询方)',
                income:       '100',
                output:       '70',
                remain:       '30',
                mark:         '-',
            }],
        };
    },
    methods: {
        timeChange() {
            this.search.startTime = this.timeRange[0];
            this.search.endTime = this.timeRange[1];
        },
        downloadLog() {
            if(!this.timeRange) {
                return this.$message.error('请选择时间段');
            }
        },
    },
};
</script>
