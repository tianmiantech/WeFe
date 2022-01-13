<template>
    <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
    >
        <template #empty>
            <EmptyData />
        </template>
        <el-table-column label="名称 / Id">
            <template v-slot="scope">
                <span>{{ scope.row.name }}</span>
                <br>
                <span class="p-id">{{ scope.row.id }}</span>
            </template>
        </el-table-column>
        <el-table-column label="数据集类型" prop="data_resource_type"></el-table-column>
        <el-table-column
            label="样本量"
            prop="row_count"
        >
            <template v-slot="scope">
                {{ scope.row.row_count }}
            </template>
        </el-table-column>
        <el-table-column label="上传样本总量" prop="total_row_count"></el-table-column>
        <el-table-column label="已处理样本量" prop="added_row_count"></el-table-column>
        <el-table-column label="主键重复条数" prop="repeat_id_row_count"></el-table-column>
        <el-table-column
            label="上传者"
            prop="creator_nickname"
            min-width="140"
        >
            <template v-slot="scope">
                {{ scope.row.creator_nickname }}
                <br>
                {{ dateFormat(scope.row.created_time) }}
            </template>
        </el-table-column>
        <el-table-column
            label="上传进度"
            prop="progress"
        >
            <template v-slot="scope">
                {{ scope.row.progress }}%
            </template>
        </el-table-column>
        <el-table-column
            label="耗时"
            prop="estimate_time"
        >
            <template v-slot="scope">
                {{ timeFormat(scope.row.estimate_time / 1000) }}
            </template>
        </el-table-column>
        <el-table-column
            label="错误信息"
            prop="error_message"
        >
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
</template>

<script>
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        props:  {
            tableLoading: Boolean,
        },
        data() {
            return {
                getListApi: '/data_resource/upload_task/query',
                search:     {
                    requestFromRefresh: true,
                },
                defaultSearch: false,
                watchRoute:    false,
                pagination:    {
                    page_index: 1,
                    page_size:  20,
                    total:      0,
                },
            };
        },
        methods: {
            async getDataList(opt) {
                this.pagination.page_index = +this.$route.query.page_index || 1;
                this.pagination.page_size = +this.$route.query.page_size || 20;
                await this.getList(opt);
            },
        },
    };
</script>
