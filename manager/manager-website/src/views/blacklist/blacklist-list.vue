<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-button
                type="warning"
                @click="showSelectMemberDialog"
            >
                添加黑名单
            </el-button>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column
                label="ID"
                prop="id"
                min-width="150"
            />
            <el-table-column
                label="成员名"
                prop="member_name"
                min-width="150"
            />
            <el-table-column
                label="创建时间"
                min-width="100"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.created_time) }}
                </template>
            </el-table-column>
            <el-table-column
                label="备注"
                prop="remark"
                min-width="200"
            />
            <el-table-column
                label="操作"
                width="100"
            >
                <template v-slot="scope">
                    <el-button
                        type="danger"
                        class="ml10"
                        :disabled="scope.row.usage_count > 0"
                        @click="deleteData(scope.row)"
                    >
                        移除
                    </el-button>
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

        <SelectMemberDialog
            ref="SelectMemberDialog"
            @select-member="selectMember"
        />
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';
    import SelectMemberDialog from './components/select-member-dialog';

    export default {
        components: {
            SelectMemberDialog,
        },
        mixins: [table],
        data() {
            return {
                search: {
                    phone_number: '',
                    nickname:     '',
                },
                getListApi:     '/blacklist/list',
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
            };
        },
        created() {
            this.getList();
        },
        methods: {
            deleteData(row) {
                this.$confirm('是否继续 将此记录移除黑名单?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/blacklist/delete',
                        data: {
                            id: row.id,
                        },
                    });

                    if (code === 0) {
                        this.$message.success('删除成功!');
                        this.getList();
                    }
                });
            },
            showSelectMemberDialog() {
                const ref = this.$refs['SelectMemberDialog'];

                ref.show = true;
                ref.loadDataList(true);
            },

            selectMember(item) {
                this.$prompt('请填写理由，<span class="color-danger">加入黑名单后 gateway 服务将会拒绝所有来自该成员的请求。</span>', '将 [' + item.name + '] 加入黑名单，是否继续?', {
                    inputPattern:             /^\S{1,100}$/,
                    inputErrorMessage:        '请填写正当理由',
                    dangerouslyUseHTMLString: true,
                }).then(async ({ value }) => {
                    const { code } = await this.$http.post({
                        url:  '/blacklist/add',
                        data: {
                            memberIds: [item.id],
                            remark:    value,
                        },
                    });

                    if (code === 0) {
                        this.$message.success('添加成功!');
                        this.getList();
                    }
                });

            },
        },
    };
</script>
