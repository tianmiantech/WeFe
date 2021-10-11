<template>
    <div class="base-table">
        <el-table
            :border="border"
            :stripe="stripe"
            :height="height"
            :data="tableData"
            :max-height="maxHeight"
            :highlight-current-row="highlightCurrentRow"
            :span-method="spanMethod"
            @select="select"
            @select-all="selectAll"
            @selection-change="selectionChange"
            @sort-change="sortChange"
            @row-click="rowClick"
        >
            <slot
                :columns="columns"
                :tableData="body"
            />
        </el-table>

        <div
            v-if="paging"
            class="table-action text-r"
        >
            <el-pagination
                background
                :total="total"
                :layout="layout"
                :disabled="disablePaging"
                :page-sizes="pageSizes"
                :page-size="pageSizeNum"
                :current-page="currentPageNum"
                @current-change="currentChange"
                @prev-click="prevClick"
                @next-click="nextClick"
                @size-change="sizeChange"
            />
        </div>
    </div>
</template>

<script>
    // import jsonToExcel from '../JsonToExcel/JsonToExcel';
    import { deepMerge } from '@js/utils/types';

    export default {
        name:       'BaseTable',
        components: {
            // jsonToExcel,
        },
        props: {

            height: {
                type:    String,
                default: () => null,
            },

            data: {
                type:    Object,
                default: () => {},
            },

            stripe: {
                type:    Boolean,
                default: () => true,
            },

            border: {
                type:    Boolean,
                default: () => true,
            },

            maxHeight: {
                type:    Number,
                default: () => null,
            },

            highlightCurrentRow: {
                type:    Boolean,
                default: () => true,
            },

            spanMethod: {
                type:    Function,
                default: () => [1, 1],
            },
            http: {
                type:    Array,
                default: () => ['get', '', {}],
            },
            dataTransform: {
                type:    Function,
                default: () => null,
            },

            disablePaging: {
                type:    Boolean,
                default: () => false,
            },

            localPaging: {
                type:    Boolean,
                default: () => false,
            },

            paging: {
                type:    Boolean,
                default: () => true,
            },

            pageSize: {
                type:    Number,
                default: () => 10,
            },

            pageSizes: {
                type:    Array,
                default: () => [10, 20, 30, 50, 100],
            },

            currentPage: {
                type:    Number,
                default: () => 1,
            },

            layout: {
                type:    String,
                default: () => 'sizes, prev, pager, next',
            },

            /* showDownload: {
                type: Boolean,
                default: () => false,
            }, */
        },
        data() {
            return {
                total:          0,
                isLocal:        false,
                tableData:      [],
                localTableData: [],
                currentPageNum: this.currentPage,
                pageSizeNum:    this.pageSize,
                columns:        [
                    {
                        prop:  'date',
                        label: '日期',
                    },
                    {
                        prop:  'name',
                        label: '名字',
                    },
                    {
                        prop:  'address',
                        label: '地址',
                    },
                ],
                body: [
                    {
                        index:   1,
                        date:    '0820',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   2,
                        date:    '0821',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   3,
                        date:    '0822',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   3,
                        date:    '0822',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   3,
                        date:    '0822',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   3,
                        date:    '0822',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   3,
                        date:    '0822',
                        name:    '名字',
                        address: '深圳',
                    },
                    {
                        index:   3,
                        date:    '0822',
                        name:    '名字',
                        address: '深圳',
                    },
                ],
            };
        },
        created() {
            this.init({ currentPage: this.currentPageNum, pageSize: this.pageSizeNum });
        },
        methods: {
            async init(opt = { currentPage: 1, pageSize: 10 }) {
                if (this.localPaging && this.localTableData.length) {

                    this.localPagination(opt.currentPage, opt.pageSize);
                } else {

                    let method,
                        service,
                        param = {};

                    if (this.http.length < 3) {
                        method = 'get';
                        service = this.http[0];
                        param = this.http[1];
                    } else {
                        method = this.http[0];
                        service = this.http[1];
                        param = this.http[2];
                    }


                    const params = deepMerge(
                        {
                            urltail: `${opt.pageSize}/${opt.currentPage}`,
                            data:    {
                                pageIndex: opt.currentPage,
                                pageSize:  opt.pageSize,
                            },
                        },
                        param,
                    );

                    const { code, data } = await this.$http[method](service, params);

                    this.tableData = this.body;
                    // this.tableData = this.dataTransform(this.body) || data;
                    if (code === 0) {
                        const transformedData = (await this.dataTransform(data)) || data;

                        if (this.localPaging) {
                            this.localTableData = transformedData;
                        } else {
                            this.tableData = transformedData;
                        }
                    }
                }
            },

            reload(opt = { currentPage: 1 }) {
                console.log(opt.currentPage);

                if (this.paging) {
                    this.currentPageNum = opt.currentPage;
                }
                this.$nextTick(() => {
                    this.init();
                });
            },

            localPagination(currentPage, pageSize) {
                this.tableData = [];
                this.localTableData.forEach((value, index) => {
                    this.tableData.push(value);
                });
            },
            currentChange(currentPage) {
                this.reload({ currentPage });
                // this.$emit('current-change', currentPage);
            },
            prevClick(currentPage) {
                // this.$emit('prev-click', currentPage);
            },
            nextClick(currentPage) {
                // this.$emit('next-click', currentPage);
            },
            sizeChange(items) {
                this.reload({ items });
                // this.$emit('size-change', items);
            },
            select(...arg) {
                this.$emit('select', ...arg);
            },
            selectAll(...arg) {
                this.$emit('select-all', ...arg);
            },
            selectionChange(...arg) {
                this.$emit('selection-change', ...arg);
            },
            sortChange(...arg) {
                this.$emit('sort-change', ...arg);
            },
            rowClick(...arg) {
                this.$emit('row-click', ...arg);
            },
            /* download() {
                this.$refs.jsonToExcel.generate();
            }, */
        },
    };
</script>

<style lang="scss">
    .base-table {
        background: #fff;
        padding: 20px;
        .table-action {
            margin-top: 20px;
        }
    }

    .el-table__empty-block {
        min-height: 400px;
    }
</style>
