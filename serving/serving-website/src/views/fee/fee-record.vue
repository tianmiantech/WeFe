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
            <el-table-column label="流水号" prop="id"></el-table-column>
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
            <el-table-column label="收入（￥）">
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
            search:   {
                startTime:     '',
                endTime:       '',
            },
            timeRange:   '',
            getListApi:  '/fee/query-list',
        }
    },
    methods: {
        timeChange() {
            this.search.startTime = this.timeRange[0];
            this.search.endTime = this.timeRange[1];
        },
        downloadLog() {
            if(!timeRange) {
                return this.$message.error('请选择时间段');
            }
        }
    }
}
</script>
