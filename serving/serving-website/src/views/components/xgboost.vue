<template>
    <div class="xx">
        <el-table
            :data="list"
            stripe
            border
        >
            <div slot="empty">
                <TableEmptyData />
            </div>
            <el-table-column
                label="名称"
            >
                <template slot-scope="scope">
                    {{ scope.row.name }}
                    <br>
                    <span class="id">{{ scope.row.id }}</span>
                </template>
            </el-table-column>
            <el-table-column
                label="训练类型"
                prop="fl_type"
                width="120px"
            />
            <el-table-column
                label="创建者"
                prop="creator_nickname"
                width="120px"
            />
            <el-table-column
                label="创建时间"
                width="120px"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column
                label="修改者"
                prop="creator_nickname"
                width="120px"
            />
            <el-table-column
                label="修改时间"
                width="120px"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                width="160px"
            >
                <template slot-scope="scope">
                    <router-link :to="{name: 'modeling-config-view', query: { id: scope.row.id, algorithm:'XGBoost' }}">
                        <el-button type="primary">
                            查看
                        </el-button>
                    </router-link>
                    <el-button
                        type="danger"
                        class="ml10"
                        @click="deleteData(scope.row)"
                    >
                        删除
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
    </div>
</template>

<script>
import table from '@src/mixins/table';

export default {
    mixins: [table],
    props:  {
        tableLoading: Boolean,
        searchField:  {
            type:    Object,
            default: _ => {},
        },
    },
    created() {
        this.$parent = this.$parent.$parent.$parent.$parent;
        this.search = this.searchField;
        this.getDataList();
    },
    methods: {
        getDataList() {
            this.getListApi = '/modeling_config/query?algorithm=XGBoost';
            this.getList();
        },
        // 删除
        deleteData(row) {

            this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                type: 'warning',
            }).then(async () => {
                const { code } = await this.$http.post({
                    url:  '/modeling_config/delete',
                    data: {
                        id:        row.id,
                        algorithm: 'XGBoost',
                    },
                });

                if(code === 0) {
                    this.$message.success('删除成功!');
                    this.getList();
                }
            });
        },
    },
};
</script>
