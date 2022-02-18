<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item label="过滤器ID:">
                <el-input v-model="search.id" />
            </el-form-item>

            <el-form-item label="过滤器:">
                <el-input v-model="search.name" />
            </el-form-item>

            <el-button
                type="primary"
                @click="getList"
            >
                查询
            </el-button>

            <router-link
                class="ml20"
                :to="{name: 'filter-view'}"
            >
                <el-button>
                    新增
                </el-button>
            </router-link>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <el-table-column
                type="index"
                label="编号"
                width="45px"
            />
            <el-table-column
                label="过滤器名称/ID"
                min-width="154px"
            >
                <template slot-scope="scope">
                    <router-link :to="{name: 'filter-data-detail', query: {id: scope.row.id, name: scope.row.name }}">
                        {{ scope.row.name }}
                    </router-link>
                    <br>
                    {{ scope.row.id }}
                </template>
            </el-table-column>

            <el-table-column
                label="数据量"
                prop="row_count"
                width="100px"
            />
            <el-table-column
                label="描述"
                prop="description"
                width="160px"
            />

            <el-table-column
                label="使用次数"
                prop="used_count"
                width="100px"
            />

            <el-table-column
                label="创建时间"
                min-width="140px"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="140px"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="操作"
                width="92px"
            >
                <template slot-scope="scope">
                    <el-button
                        type="danger"
                        @click="deleteTask(scope.row.id)"
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
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';

    export default {
        mixins: [table],
        data() {
            return {
                search: {
                    id:     '',
                    status: '',
                },
                headers: {
                    token: localStorage.getItem('token') || '',
                },
                getListApi: '/filter/query',
            };
        },
        created() {
            this.getList();
        },
        methods: {
            async deleteTask (id) {
                this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/filter/delete',
                        data: {
                            id,
                        },
                    });

                    if (code === 0) {
                        this.$message('删除成功!');
                        this.getList();
                    }
                });
            },
        },
    };
</script>
