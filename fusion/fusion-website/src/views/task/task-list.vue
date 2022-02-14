<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item
                label="任务序号:"
                label-width="80px"
            >
                <el-input
                    v-model="search.business_id"
                    clearable
                />
            </el-form-item>


            <el-form-item label="状态">
                <el-select
                    v-model="search.status"
                    clearable
                >
                    <el-option
                        v-for="item in statusList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="角色">
                <el-select
                    v-model="search.my_role"
                    clearable
                >
                    <el-option
                        v-for="item in myRoleList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.text"
                    />
                </el-select>
            </el-form-item>

            <el-button
                type="primary"
                @click="getList('to')"
            >
                查询
            </el-button>

            <router-link
                :to="{name: 'task-add'}"
            >
                <el-button
                    size="small"
                >
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
                label="任务"
                width="240px"
            >
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.business_id }}</p>
                    {{ scope.row.name }}
                </template>
            </el-table-column>

            <el-table-column
                label="合作方"
                prop="partner_name"
                width="200px"
            />
            <el-table-column
                label="状态"
                width="85px"
            >
                <template slot-scope="scope">
                    <TaskStatusTag
                        :status="scope.row.status"
                    />
                </template>
            </el-table-column>

            <el-table-column
                label="数据资源"
                width="360px"
            >
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.data_resource_id }}</p>
                    {{ scope.row.data_resource_name }}
                </template>
            </el-table-column>


            <el-table-column
                v-if="psi_actuator_role=='server'"
                label="数据量"
                prop="data_count"
                width="120px"
            >
                <template slot-scope="scope">
                    我方样本量: {{ scope.row.row_count }} <br>
                    对方样本量: {{ scope.row.data_count }} <br>
                    <div v-if="scope.row.status=='Success'">
                        融合量: {{ scope.row.fusion_count }}
                    </div>
                </template>
            </el-table-column>

            <el-table-column
                label="数据量"
                prop="data_count"
                width="120px"
            >
                <template slot-scope="scope">
                    样本量: {{ scope.row.row_count }} <br>
                    <div v-if="scope.row.status=='Success'">
                        融合量: {{ scope.row.fusion_count }}
                    </div>
                </template>
            </el-table-column>

            <el-table-column
                label="耗时"
                prop="spend"
                width="100px"
            >
                <template slot-scope="scope">
                    {{ dateFormatter(scope.row.spend) }}
                </template>
            </el-table-column>

            <el-table-column
                label="创建时间"
                min-width="120"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="120"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="操作"
                width="140px"
                fixed="right"
            >
                <template slot-scope="scope">
                    <router-link
                        :to="{name: 'task-view', query: { id: scope.row.id }}"
                    >
                        <el-button
                            size="small"
                            type="primary"
                        >
                            查看
                        </el-button>
                    </router-link>

                    <el-button
                        type="button"
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
            :visible.sync="task.editor"
            title="添加任务"
            width="600px"
        >
            <el-form>
                <el-form-item
                    label="任务名称："
                    label-width="120px"
                >
                    <el-input
                        v-model="task.name"
                    />
                </el-form-item>
                <el-form-item
                    label="合作方："
                    label-width="120px"
                >
                    <el-select
                        v-model="task.partner_id"
                        clearable
                    >
                        <el-option
                            v-for="(tag, index) in partnerList"
                            :key="index"
                            :value="tag.partner_id"
                            :label="tag.name"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item
                    label="数据资源："
                    label-width="120px"
                >
                    <el-button
                        type="primary"
                        icon="el-icon-edit"
                        @click="dataResource.visible=true"
                    >
                        {{ dataResource.name }}
                    </el-button>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="task.editor=false">取消</el-button>
                <el-button
                    type="primary"
                    :disabled="!task.name || !task.partner_id || !task.data_resource_id"
                    @click="task.id?editTask():addTask()"
                >确定</el-button>
            </span>
        </el-dialog>


        <!-- 资源选择 -->
        <el-dialog
            :visible.sync="dataResource.visible"
            title="资源选择"
            width="70%"
        >
            <el-tabs
                tab-position="top"
                type="card"
            >
                <el-tab-pane label="数据集">
                    <el-table :data="dataSetList">
                        <el-table-column
                            type="index"
                            label="序号"
                        />
                        <el-table-column
                            label="名称"
                            prop="name"
                            width="120"
                        />
                        <el-table-column
                            label="描述"
                            prop="description"
                        />
                        <el-table-column
                            label="操作"
                            width="300px"
                        >
                            <template slot-scope="scope">
                                <el-button
                                    type="primary"
                                    @click="dataResource.name=scope.row.name,
                                            dataResource.id=scope.row.id,
                                            dataResource.description=scope.row.description,
                                            dataResource.type=scope.row.type,
                                            task.data_resource_id=scope.row.id,
                                            task.data_resource_type=scope.row.type,
                                            task.data_count=scope.row.rows_count,
                                            dataResource.visible=false"
                                >
                                    选择
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-tab-pane>
                <el-tab-pane
                    label="过滤器选择"
                >
                    <el-table :data="bloomFilterList">
                        <el-table-column
                            type="index"
                            label="序号"
                        />
                        <el-table-column
                            label="名称"
                            prop="name"
                            width="120"
                        />
                        <el-table-column
                            label="描述"
                            prop="description"
                        />
                        <el-table-column
                            label="操作"
                            width="300px"
                        >
                            <template slot-scope="scope">
                                <el-button
                                    type="primary"
                                    @click="dataResource.name=scope.row.name,
                                            dataResource.id=scope.row.id,
                                            dataResource.description=scope.row.description,
                                            dataResource.type=scope.row.type,
                                            task.data_resource_id=scope.row.id,
                                            task.data_resource_type=scope.row.type,
                                            dataResource.visible=false"
                                >
                                    选择
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-tab-pane>
            </el-tabs>
        </el-dialog>
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';
    import TaskStatusTag from '@src/components/views/task-status-tag';

    export default {
        components: {
            TaskStatusTag,
        },
        mixins: [table],
        data() {
            return {
                 search: {
                   business_id: '',
                   status:      '',
                   my_role:     '',
                 },

                statusList: [{
                    name:  '等待合作方审核',
                    value: 'Pending',
                }, {
                    name:  '等待我方审核',
                    value: 'Await',
                }, {
                    name:  '正在对齐中',
                    value: 'Running',
                }, {
                    name:  '任务中断',
                    value: 'Interrupt',
                }, {
                    name:  '任务异常关闭',
                    value: 'Failure',
                }, {
                    name:  '成功',
                    value: 'Success',
                }],

                 headers: {
                     token: localStorage.getItem('token') || '',
                 },
                getListApi:     '/task/paging',
                userList:       [],
                taskStatusList: [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                dataDialog: false,
                jsonData:   '',
                 task:       {
                   editor:             false,
                   id:                 '',
                   business_id:        '',
                   name:               '',
                   partner_id:         '',
                   data_resource_id:   '',
                   data_resource_type: '',
                   data_count:         '',
                   row_count:          '',
                   fusion_count:       '',
                },
                // dataResource
                dataResource: {
                    visible:     false,
                    editor:      false,
                    id:          '',
                    name:        '',
                    type:        '',
                    description: '',
                },
                dataSetList:       [],
                bloomFilterList:   [],
                partnerList:       [],
                psi_actuator_role: '',
                myRoleList:        [
                    {
                        text:  '我发起的',
                        value: 'promoter',
                    },
                    {
                        text:  '我协同的',
                        value: 'provider',
                    },
                ],
            };
        },
        async created() {
            if (this.$route.query.type === 'my_role') {
                this.search.my_role = this.$route.query.value;
            } else {
                this.search.status = this.$route.query.value;
            }
            await this.getStatus();
            await this.getDataSet();
            await this.getBloomFilter();
            await this.getPartner();

            this.getList();
        },
        methods: {

            async getStatus() {
                const { code, data } = await this.$http.get('/task/status',{
                     },
                );

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
                    url:  '/task/add',
                    data: {
                        name:               this.task.name,
                        partner_id:         this.task.partner_id,
                        data_resource_id:   this.task.data_resource_id,
                        data_resource_type: this.task.data_resource_type,
                        data_count:         this.task.data_count,
                    },
                });

                    if (code === 0) {
                        this.task.editor = false;
                        this.$message('新增成功!');
                        this.getList();
                    }
            },


            async deleteTask (id) {

                this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/task/delete',
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


            async getDataSet () {
                const { code, data } = await this.$http.get(
                       '/data_set/query',{
                     },
                );

                if (code === 0) {
                    this.dataSetList = data.list;
                }
            },


            async getBloomFilter () {
                const { code, data } = await this.$http.get(
                       '/filter/query',{
                     },
                );

                if (code === 0) {
                    this.bloomFilterList = data.list;
                }
            },


            async getPartner () {
                const { code, data } = await this.$http.get(
                       '/partner/paging',{
                     },
                );

                if (code === 0) {
                    this.partnerList = data.list;
                }
            },


            dateFormatter(timeStamp) {
                let time = '';
                // const now = Date.now();
                const before = +new Date(timeStamp);
                const range = Math.floor(before / 1000);
                const minutes = Math.floor(range / 60);
                const hours = Math.floor(minutes / 60);
                const map = {
                    day:     Math.floor(hours / 24),
                    hours:   hours % 24,
                    minutes: minutes % 60,
                    range:   range % 60,
                };

                if (map.day) {
                    time = `${map.day}d${map.hours === 0 ? 1 : map.hours}h`;
                } else if (map.hours) {
                    time = `${map.hours}h${map.minutes === 0 ? 1 : map.minutes}min`;
                } else if (map.minutes) {
                    time = `${map.minutes}min${map.range === 0 ? 1 : map.range}s`;
                } else if (map.range >= 0) {
                    time = `${map.range === 0 ? 1 : map.range}s`;
                }

                return time;
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
