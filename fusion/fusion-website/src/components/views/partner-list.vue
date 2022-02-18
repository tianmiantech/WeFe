<template>
    <div class="page">
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
                type="index"
                label="编号"
                width="45px"
            />
            <el-table-column
                prop="member_id"
                label="id"
                width="240px"
            />
            <el-table-column
                label="合作方"
                prop="member_name"
                width="200px"
            />

            <el-table-column
                label="调用域名"
                prop="base_url"
                width="360px"
            />
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
                        @click="selectPartner(scope.row)"
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
    </div>
</template>

<script>
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        props:  {
            searchField: {
                type:    Object,
                default: _ => {},
            },
            emitEventName: String,
        },
        data() {
            return {
                show_data_set_preview_dialog: false,
                tableLoading:                 false,
            };
        },
        created() {
            this.getDataList();
        },
        methods: {

            showDataSetPreview(item){
                this.show_data_set_preview_dialog = true;

                this.$nextTick(() =>{
                    this.$refs['DataSetPreview'].loadData(item.id);
                });
            },


            async getDataList() {
                this.getListApi = '/partner/paging';
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


            selectPartner(item) {
                item.$source_page = this.emitEventName;
                this.$emit('close-dialog');
                this.$emit('selectPartner', item);
                this.$bus.$emit('selectPartner', item);
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
