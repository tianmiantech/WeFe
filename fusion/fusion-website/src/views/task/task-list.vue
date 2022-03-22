<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item label="任务id:">
                <el-input
                    v-model="search.business_id"
                    clearable
                />
            </el-form-item>

            <el-form-item label="任务状态:">
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
            <el-form-item label="角色:">
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
                width="45"
            />
            <el-table-column
                label="任务"
                width="240"
            >
                <template slot-scope="scope">
                    <strong>{{ scope.row.name }}</strong>
                    <p class="id">{{ scope.row.business_id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="合作方"
                prop="partner_member_name"
                width="200"
            />

            <el-table-column
                label="状态"
                width="120"
            >
                <template slot-scope="scope">
                    <TaskStatusTag :status="scope.row.status" />
                </template>
            </el-table-column>

            <el-table-column
                label="数据资源"
                width="200"
            >
                <template slot-scope="scope">
                    <strong>{{ scope.row.data_resource_name }}</strong>
                    <p class="id">{{ scope.row.data_resource_id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="数据量"
                prop="data_count"
                width="120"
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
                min-width="100"
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
                width="150"
                fixed="right"
            >
                <template slot-scope="scope">
                    <router-link
                        v-if="scope.row.status === 'Pending'"
                        :to="{name: 'task-pending-view', query: { id: scope.row.id }}"
                    >
                        <el-button
                            size="small"
                            type="success"
                        >
                            审核
                        </el-button>
                    </router-link>
                    <router-link
                        v-else
                        :to="{name: 'task-view', query: { id: scope.row.id }}"
                    >
                        <el-button
                            size="small"
                            type="primary"
                        >
                            详情
                        </el-button>
                    </router-link>

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
                getListApi: '/task/paging',
                myRoleList: [
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

            this.getList();
        },
        methods: {

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
                    time = `${map.day}天${map.hours === 0 ? 1 : map.hours}小时`;
                } else if (map.hours) {
                    time = `${map.hours}小时${map.minutes === 0 ? 1 : map.minutes}分钟`;
                } else if (map.minutes) {
                    time = `${map.minutes}分钟${map.range === 0 ? 1 : map.range}秒`;
                } else if (map.range >= 0) {
                    time = `${map.range === 0 ? 1 : map.range}秒`;
                }

                return time;
            },
        },
    };
</script>
