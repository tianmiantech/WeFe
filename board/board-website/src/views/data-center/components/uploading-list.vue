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
        <el-table-column label="名称 / Id" min-width="180">
            <template v-slot="scope">
                <span>{{ scope.row.data_resource_name }}</span>
                <br>
                <p class="p-id">{{ scope.row.id }}</p>
            </template>
        </el-table-column>
        <el-table-column label="数据资源类型" min-width="100">
            <template v-slot="scope">
                {{ sourceTypeMap[scope.row.data_resource_type] }}
            </template>
        </el-table-column>
        <el-table-column label="上传进度">
            <template v-slot="scope">
                {{ scope.row.progress_ratio }}%
                <p>{{ scope.row.status === 'completed' ? '已完成' : scope.row.status === 'failed' ? '已失败' : '正在上传' }}</p>
                <p>{{scope.row.error_message}}</p>
            </template>
        </el-table-column>
        <el-table-column label="上传样本总量" min-width="100" prop="total_data_count"></el-table-column>
        <el-table-column label="已处理样本量" min-width="100" prop="completed_data_count"></el-table-column>
        <el-table-column label="无效数据量" min-width="100" prop="invalid_data_count"></el-table-column>
        <el-table-column
            label="上传时间"
            min-width="120"
        >
            <template v-slot="scope">
                {{ scope.row.creator_nickname }}
                <br>
                {{ dateFormat(scope.row.created_time) }}
            </template>
        </el-table-column>
        <el-table-column label="耗时">
            <template v-slot="scope">
                {{ timeFormat(scope.row.estimate_remaining_time / 1000) }}
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
                getListApi:    '/data_resource/upload_task/query',
                search:        {},
                defaultSearch: false,
                watchRoute:    false,
                turnPageRoute: false,
                pagination:    {
                    page_index: 1,
                    page_size:  20,
                    total:      0,
                },
                sourceTypeMap: {
                    BloomFilter:  '布隆过滤器',
                    ImageDataSet: '图像数据集',
                    TableDataSet: '结构化数据集',
                },
            };
        },
        methods: {
            getDataList(opt) {
                this.getList(opt);
            },
        },
    };
</script>
