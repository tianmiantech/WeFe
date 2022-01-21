<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item label="数据源Id:">
                <el-input v-model="search.id" />
            </el-form-item>

            <el-form-item label="数据源名称:">
                <el-input v-model="search.name" />
            </el-form-item>

            <el-button
                type="primary"
                @click="getList({ to: true })"
            >
                查询
            </el-button>

            <el-button @click="editSource">
                新增
            </el-button>
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
                label="名称"
                min-width="120px"
            >
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.id }}</p>
                    {{ scope.row.name }}
                </template>
            </el-table-column>

            <el-table-column
                label="Host"
                prop="host"
                width="300px"
            />

            <el-table-column
                label="端口"
                prop="port"
                width="100px"
            />
            <el-table-column
                label="数据库类型"
                prop="database_type"
                width="100px"
            />


            <el-table-column
                label="创建时间"
                min-width="160px"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="160px"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="操作"
                width="150px"
                fixed="right"
            >
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        @click="editSource($event, scope.row)"
                    >
                        编辑
                    </el-button>
                    <el-button
                        type="danger"
                        @click="deleteSource(scope.row.id)"
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

        <DataSourceEditor
            :id="dataSourceEditorId"
            ref="DataSourceEditor"
            @data-source-add="refresh"
        />
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';
    import DataSourceEditor from './data-source-edit';

    export default {
        components: {
            DataSourceEditor,
        },
        mixins: [table],
        inject: ['refresh'],
        data() {
            return {
                search: {
                    id:   '',
                    name: '',
                },
                getListApi:         '/data_source/query',
                dataSourceEditorId: '',
            };
        },
        methods: {
            editSource(event, row) {
                this.dataSourceEditorId = row? row.id : '';

                const ref = this.$refs['DataSourceEditor'];

                ref.show(row);
            },

            async deleteSource (id) {
                this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/data_source/delete',
                        data: {
                            id,
                        },
                    });

                    if (code === 0) {
                        this.$message('删除成功!');
                        this.refresh();
                    }
                });
            },
        },
    };
</script>

<style lang="scss">
    .structure-table{
        .ant-table-title{
            font-weight: bold;
            text-align: center;
            padding: 10px;
            font-size:16px;
        }
    }
</style>
