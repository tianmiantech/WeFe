<template>
    <div class="data-set-list">
        <el-table
            v-loading="tableLoading"
            max-height="500"
            :data="list"
            stripe
            border
        >
            <div slot="empty">
                <TableEmptyData />
            </div>
            <el-table-column
                label="名称 / Id"
                min-width="200"
            >
                <template slot-scope="scope">
                    <div :title="scope.row.description">
                        {{ scope.row.name }}
                        <p class="id">{{ scope.row.id }}</p>
                    </div>
                </template>
            </el-table-column>

            <el-table-column
                label="列数"
                prop="feature_count"
                min-width="80"
            />
            <el-table-column
                label="数据量"
                prop="row_count"
                min-width="80"
            />
            <el-table-column
                label="使用次数"
                prop="used_count"
                min-width="80"
            />

            <el-table-column
                label="来源"
                prop="data_resource_source"
                min-width="120"
            />

            <el-table-column
                label="上传时间"
                min-width="120"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column
                label="上传者"
                prop="created_by"
                min-width="120"
            />
            <el-table-column
                fixed="right"
                label="操作"
                width="140px"
            >
                <template slot-scope="scope">
                    <el-tooltip
                        content="预览数据"
                        placement="top"
                    >
                        <el-button
                            circle
                            type="info"
                            @click="showBloomFilterPreview(scope.row)"
                        >
                            <i class="el-icon-view" />
                        </el-button>
                    </el-tooltip>
                    <el-button
                        type="success"
                        :disabled="scope.row.deleted"
                        @click="selectBloomFilter(scope.row)"
                    >
                        选择
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <div
            v-if="pagination.total"
            :class="['pagination', 'text-r']"
        >
            <el-pagination
                :pager-count="5"
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
            title="数据预览"
            :visible.sync="show_bloom_filter_preview_dialog"
            append-to-body
        >
            <BloomFilterPreview ref="BloomFilterPreview" />
        </el-dialog>
    </div>
</template>

<script>
import table from '@src/mixins/table';
import BloomFilterPreview from '@comp/views/bloom-filter-preview';

export default {
    components: {
        BloomFilterPreview,
    },
    mixins: [table],
    props:  {
        // api:         Object,
        // containsY:   String,
        // auditStatus: Boolean,
        searchField: {
            type:    Object,
            default: _ => {},
        },
        emitEventName: String,
        // dataSets:      Array,
    },
    data() {
        return {
            show_bloom_filter_preview_dialog: false,

            tableLoading: false,
        };
    },
    async created() {
        await this.getDataList();
    },
    methods: {

        showBloomFilterPreview(item){
            this.show_bloom_filter_preview_dialog = true;

            this.$nextTick(() =>{
                this.$refs['BloomFilterPreview'].loadData(item.id);
            });
        },


        async getDataList() {

            this.getListApi = '/filter/query';
            this.tableLoading = true;
            this.isIndeterminate = false;
            this.search = this.searchField;
            await this.getList();

            this.list.forEach((item, index) => {
                item.$checked = false;
                this.$set(this.list, index, item);
            });
            this.tableLoading = false;
        },


        selectBloomFilter(item) {
            item.$source_page = this.emitEventName;
            this.$emit('close-dialog');
            this.$emit('selectBloomFilter', item);
            this.$bus.$emit('selectBloomFilter', item);
        },
        cancelPopup() {
            this.$emit('close-dialog');
        },
    },
};
</script>

<style lang="scss" scoped>
    .data-add{
        width:200px;
        height:34px;
        line-height:34px;
        text-align:right;
    }
    .pagination{
        display: flex;
        margin-top: 20px;
    }
    .btns{
        text-align: right;
        flex: 1;
    }
</style>
