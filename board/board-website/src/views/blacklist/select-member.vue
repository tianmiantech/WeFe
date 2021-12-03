<template>
    <el-dialog
        v-model="show"
        destroy-on-close
        title="请选择加入黑名单的成员"
        width="750px"
    >
        <el-form
            inline
            v-loading="loading"
            @submit.prevent
        >
            <el-form-item
                label="Id："
                label-width="40px"
            >
                <el-input
                    v-model="search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="名称："
                label-width="60px"
            >
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>
            <el-button
                type="primary"
                @click="loadDataList({ resetPagination: true })"
            >
                查询
            </el-button>
            <el-table
                :data="list"
                max-height="500"
                border
                stripe
            >
                <el-table-column
                    property="id"
                    label="Id"
                    min-width="150"
                >
                    <template v-slot="scope">
                        {{ scope.row.name }}<br>
                        <span class="p-id">
                            {{ scope.row.id }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="E-Mail"
                    prop="email"
                    min-width="120"
                />
                <el-table-column
                    label="电话"
                    prop="mobile"
                    min-width="80"
                />
                <el-table-column
                    label="操作"
                    width="100"
                >
                    <template v-slot="scope">
                        <template
                            v-if="!scope.row.in_blacklist"
                        >
                            <el-button
                                type="success"
                                :disabled="scope.row.use"
                                @click="selectMember(scope.row)"
                            >
                                选择
                            </el-button>
                        </template>
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
        </el-form>
    </el-dialog>
</template>

<script>
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        emits:  ['select-member'],
        data() {
            return {
                loading:    false,
                show:       false,
                getListApi: '/blacklist/member',
                search:     {
                    id:   '',
                    name: '',
                },
                watchRoute:    false,
                turnPageRoute: false,
            };
        },
        watch: {
            show: {
                handler(val) {
                    if(val) {
                        this.search.id = '';
                        this.search.name = '';
                        this.pagination = {
                            page_index: 1,
                            page_size:  20,
                            total:      0,
                        };
                    }
                },
            },
        },
        methods: {
            async loadDataList(opt = {}) {
                this.loading = true;
                await this.getList(opt);
                this.loading = false;
            },
            selectMember(item) {
                this.$emit('select-member', item);
                this.show = false;
            },
            currentPageChange(val) {
                this.pagination.page_index = val;
                this.loadDataList();
            },
            pageSizeChange(val) {
                this.pagination.page_size = val;
                this.loadDataList();
            },
            showSelectMemberDialog() {
                const ref = this.$refs['SelectMemberDialog'];

                ref.show = true;
                ref.loadDataList();
            },

        },
    };
</script>
