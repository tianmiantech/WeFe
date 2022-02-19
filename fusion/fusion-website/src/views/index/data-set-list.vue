<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item label="ID:">
                <el-input v-model="search.id" />
            </el-form-item>

            <el-form-item label="名称:">
                <el-input v-model="search.name" />
            </el-form-item>

            <el-button
                type="primary"
                @click="getTableList"
            >
                查询
            </el-button>

            <router-link
                class="ml20"
                :to="{name: 'data-set-view'}"
            >
                <el-button>
                    新增
                </el-button>
            </router-link>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            border
        >
            <el-table-column
                type="index"
                label="编号"
                width="45"
            />
            <el-table-column
                label="数据集"
                min-width="154"
            >
                <template slot-scope="scope">
                    <router-link :to="{name: 'data-set-detail', query: {id: scope.row.id, name: scope.row.name }}">
                        {{ scope.row.name }}
                    </router-link>
                    <br>
                    {{ scope.row.id }}
                </template>
            </el-table-column>

            <el-table-column
                label="行数"
                prop="row_count"
                width="100"
            />
            <el-table-column
                label="字段信息"
                prop="rows"
            >
                <template
                    v-if="scope.row.rows"
                    slot-scope="scope"
                >
                    <el-tag
                        v-for="item in scope.row.rows.split(',')"
                        :key="item"
                        :type="item"
                        effect="plain"
                        style="margin-left : 5px"
                    >
                        {{ item }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                label="使用次数"
                prop="used_count"
                width="100"
            />

            <el-table-column
                label="创建时间"
                min-width="140"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="140"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="操作"
                width="100"
                fixed="right"
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

        <el-dialog
            :visible.sync="dataSet.editor"
            title="添加任务"
            width="600px"
        >
            <el-form>
                <el-form-item
                    label="任务名"
                    label-width="100px"
                >
                    <el-input v-model="dataSet.name" />
                </el-form-item>
                <el-form-item
                    label="合作方ID"
                    label-width="100px"
                >
                    <el-input v-model="dataSet.partner_id" />
                </el-form-item>
                <el-form-item
                    label="数据资源ID"
                    label-width="100px"
                >
                    <el-input v-model="dataSet.data_resource_id" />
                </el-form-item>
                <el-form-item
                    label="数据资源类型"
                    label-width="100px"
                >
                    <el-input v-model="dataSet.data_resource_type" />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="dataSet.editor=false">取消</el-button>
                <el-button
                    type="primary"
                    :disabled="!dataSet.name || !dataSet.partner_id || !dataSet.data_resource_id"
                    @click="dataSet.id ? editTask: addTask"
                >确定</el-button>
            </span>
        </el-dialog>
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
                getListApi:     '/data_set/query',
                userList:       [],
                taskStatusList: [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                dataDialog: false,
                jsonData:   '',

                 dataSet: {
                   editor:             false,
                   id:                 '',
                   name:               '',
                   partner_id:         '',
                   data_resource_id:   '',
                   data_resource_type: '',
                },
                timer1: null,
                timer2: null,

            };
        },
        created() {
            this.getTableList();
        },
        methods: {
            async getTableList() {
                this.getList();
            },
            async getStatus() {
                const { code, data } = await this.$http.get('/dataSet/status',{});

                if(code === 0) {
                    this.taskStatusList = data;
                }
            },
            showStrategys (string) {
                this.dataDialog = true;
                setTimeout(() => {
                    this.jsonData = string;
                });
            },
            async addTask () {
                const { code } = await this.$http.post({
                    url:  '/dataSet/add',
                    data: {
                        name: this.dataSet.name,
                        id:   this.dataSet.id,
                    },
                });

                if (code === 0) {
                    this.dataSet.editor = false;
                    this.$message('新增成功!');
                    this.getList();
                }
            },

            async deleteTask (id) {
                this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/data_set/delete',
                        data: {
                            id,
                        },
                    });

                    if (code === 0) {
                        this.$message('删除成功!');
                        this.getTableList();
                    }
                });

            },
        },
    };
</script>
