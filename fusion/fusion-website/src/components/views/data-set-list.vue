<template>
    <div
        ref="dataSetList"
        class="data-set-list"
    >
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
                    <strong>{{ scope.row.name }}</strong>
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="列数"
                min-width="80"
            >
                <template slot-scope="scope">
                    {{ rowsFormatter(scope.row.rows) }}
                </template>
            </el-table-column>
            <el-table-column>
                <template slot-scope="scope">
                    <template v-if="scope.row.rows">
                        <el-tag
                            v-for="(item, index) in scope.row.rows.split(',')"
                            :key="index"
                        >
                            {{ item }}
                        </el-tag>
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="数据量"
                prop="row_count"
                min-width="80"
            />
            <el-table-column
                label="来源"
                min-width="120"
            >
                <template slot-scope="scope">
                    <p>{{ dataResourceSource[scope.row.data_resource_source] }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="上传时间"
                min-width="140"
            >
                <template slot-scope="scope">
                    <strong>{{ scope.row.creator_nickname }}</strong>
                    <p>{{ scope.row.created_time | dateFormat }}</p>
                </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                label="选择数据集"
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
                            @click="showDataSetPreview(scope.row)"
                        >
                            <i class="el-icon-view" />
                        </el-button>
                    </el-tooltip>
                    <el-button
                        type="success"
                        :disabled="scope.row.deleted"
                        @click="selectDataSet(scope.row)"
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
            :visible.sync="show_data_set_preview_dialog"
            append-to-body
        >
            <DataSetPreview ref="DataSetPreview" />
        </el-dialog>
    </div>
</template>

<script>
import table from '@src/mixins/table';
import DataSetPreview from '@comp/views/data-set-preview';

export default {
    components: {
        DataSetPreview,
    },
    mixins: [table],
    props:  {
        // api:         Object,
        // containsY:   String,
        // auditStatus: Boolean,
        searchField: {
            type:    Object,
            default: _ => {
            },
        },
        emitEventName: String,

        // dataSets:      Array,
    },
    data() {
        return {
            show_data_set_preview_dialog: false,

            tableLoading:       false,
            dataResourceSource: {
                'LocalFile':  '服务器文件上传',
                'UploadFile': '本地上传',
                'Sql':        '数据库上传',

            },
        };
    },
    async created() {
        await this.getDataList();
    },
    methods: {

        showDataSetPreview(item) {
            this.show_data_set_preview_dialog = true;


            this.$nextTick(() => {
                this.$refs['DataSetPreview'].loadData(item.id);
            });
        },


        async getDataList() {

            this.getListApi = '/data_set/query';
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


        selectDataSet(item) {
            item.$source_page = this.emitEventName;
            this.$emit('close-dialog');
            this.$emit('selectDataSet', item);
            this.$bus.$emit('selectDataSet', item);
        },
        cancelPopup() {
            this.$emit('close-dialog');
        },

        rowsFormatter(rows) {
            const count = rows.split(',');

            return count.length;
        },
    },
};
</script>

<style lang="scss" scoped>
.data-add {
    width: 200px;
    height: 34px;
    line-height: 34px;
    text-align: right;
}

.pagination {
    display: flex;
    margin-top: 20px;
}

.btns {
    text-align: right;
    flex: 1;
}
</style>
