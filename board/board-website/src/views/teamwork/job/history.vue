<template>
    <el-card
        shadow="never"
        class="page"
    >
        <el-form inline @submit.prevent>
            <el-form-item label="名称">
                <el-input v-model="search.name" />
            </el-form-item>
            <el-form-item label="状态">
                <el-select v-model="search.status">
                    <el-option
                        v-for="item in statusList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
                    />
                </el-select>
            </el-form-item>
            <el-button
                type="primary"
                @click="getList"
            >
                搜索
            </el-button>
            <el-button
                type="primary"
                class="job-history mb10"
                :disabled="selection.length < 2"
                @click="jobCompare"
            >
                <el-popover
                    width="200"
                    trigger="hover"
                    content="勾选任务后可对比, 最多勾选 3 个"
                >
                    <template #reference>
                        <el-icon>
                            <elicon-info-filled />
                        </el-icon>
                    </template>
                </el-popover>
                任务对比 ({{ selection.length }}/3)
            </el-button>
        </el-form>
        <el-table
            ref="list"
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <el-table-column width="40">
                <template v-slot="scope">
                    <el-checkbox
                        v-model="scope.row.checked"
                        @change="selectionChange(scope.row)"
                    />
                </template>
            </el-table-column>
            <el-table-column
                label="流程名称"
                min-width="110"
            >
                <template v-slot="scope">
                    <router-link
                        :to="{
                            name: 'project-flow',
                            query: {
                                member_role: scope.row.my_role,
                                project_id: scope.row.project_id,
                                flow_id: scope.row.flow_id,
                                job_id: scope.row.job_id,
                            }
                        }"
                    >
                        {{ scope.row.name }}
                    </router-link>
                    <br>
                    <span class="p-id">{{ scope.row.job_id }}</span>
                </template>
            </el-table-column>
            <el-table-column
                label="角色"
                width="105px"
            >
                <template v-slot="scope">
                    <RoleTag :role="scope.row.my_role" />
                </template>
            </el-table-column>
            <el-table-column
                label="联邦类型"
                width="80px"
            >
                <template v-slot="scope">
                    {{ learningType(scope.row.federated_learning_type) }}
                </template>
            </el-table-column>
            <el-table-column
                label="进度"
                min-width="160"
            >
                <template v-slot="scope">
                    <div class="flex-cell">
                        <div style="flex: 1;">
                            <el-tooltip
                                v-if="scope.row.message"
                                popper-class="popper-max-width"
                                placement="top"
                            >
                                <template #content>
                                    {{ scope.row.message }}
                                </template>
                                <JobStatusTag
                                    :status="scope.row.status"
                                />
                            </el-tooltip>
                            <JobStatusTag
                                v-else
                                :status="scope.row.status"
                            />
                            <br>
                            <el-progress
                                class="mt5"
                                :percentage="scope.row.progress || 0"
                            />
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column
                label="时间"
                width="140"
            >
                <template v-slot="scope">
                    <p>{{ dateFormat(scope.row.start_time) }}</p>
                    <p>{{ dateFormat(scope.row.finish_time) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="运行时长"
                width="150"
            >
                <template v-slot="scope">
                    <span v-if="scope.row.finish_time > 0">
                        {{ timeFormat((scope.row.finish_time - scope.row.start_time) / 1000) }}
                    </span>
                    <span v-else>
                        {{ timeFormat((new Date().valueOf() - scope.row.start_time) / 1000) }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                width="200"
            >
                <template v-slot="scope">
                    <router-link
                        :to="{
                            name: 'project-job-detail',
                            query: {
                                member_role: scope.row.my_role,
                                project_id:  scope.row.project_id,
                                flow_id:     scope.row.flow_id,
                                job_id:      scope.row.job_id,
                            }
                        }"
                    >
                        <el-button type="primary">
                            查看详情
                        </el-button>
                    </router-link>
                    <p class="mt10">
                        <DownloadJobLog :job-id="scope.row.job_id" />
                    </p>
                </template>
            </el-table-column>
        </el-table>

        <div class="flex_box">
            <el-button
                type="primary"
                :disabled="selection.length < 2"
                style="bottom: -32px;"
                @click="jobCompare"
            >
                <el-popover
                    width="200"
                    trigger="hover"
                    content="勾选任务后可对比, 最多勾选 3 个"
                >
                    <template #reference>
                        <el-icon>
                            <elicon-info-filled />
                        </el-icon>
                    </template>
                </el-popover>
                任务对比 ({{ selection.length }}/3)
            </el-button>

            <el-pagination
                class="text-r"
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
    </el-card>
</template>

<script>
    import table from '@src/mixins/table';
    import DownloadJobLog from '../components/download-job-log.vue';
    import JobStatusTag from '@src/components/views/job-status-tag';
    import RoleTag from '@src/components/views/role-tag';

    export default {
        components: {
            DownloadJobLog,
            JobStatusTag,
            RoleTag,
        },
        mixins: [table],
        data() {
            return {
                project_id: '',
                flow_id:    '',
                search:     {
                    name:   '',
                    status: '',
                },
                statusList: [{
                    name:  '已创建',
                    value: 'created',
                }, {
                    name:  '等待运行',
                    value: 'wait_run',
                }, {
                    name:  '运行中',
                    value: 'running',
                }, {
                    name:  '等待结束',
                    value: 'wait_stop',
                }, {
                    name:  '人为关闭',
                    value: 'stop_on_running',
                }, {
                    name:  '程序异常关闭',
                    value: 'error_on_running',
                }, {
                    name:  '成功',
                    value: 'success',
                }],
                jobStatus: {
                    created:          '已创建',
                    wait_run:         '等待运行',
                    running:          '运行中',
                    wait_stop:        '等待结束',
                    stop_on_running:  '人为关闭',
                    error_on_running: '程序异常关闭',
                    success:          '成功',
                },
                selection:   [],
                getListApi:  '/flow/job/query',
                unUseParams: ['job_id'],
                taskTimer:   null,
            };
        },
        computed: {
            learningType() {
                return function (val) {
                    const types = {
                        vertical:   '纵向',
                        horizontal: '横向',
                        mix:        '混合',
                    };

                    return types[val] || '-';
                };
            },
        },
        created() {
            const { project_id, flow_id } = this.$route.query;

            this.flow_id = flow_id;
            this.project_id = project_id;
            this.pagination.page_size = 20;
            this.getTableList();
        },
        beforeUnmount() {
            clearTimeout(this.taskTimer);
        },
        methods: {
            async getTableList() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    this.getListApi,
                    params: {
                        ...this.search,
                        page_index: this.pagination.page_index - 1,
                        page_size:  this.pagination.page_size,
                    },
                });

                if(code === 0) {
                    const checked = this.list.findIndex(x => x.checked);

                    if(~checked) {
                        this.list.forEach(row => {
                            const item = data.list.find(x => x.id === row.id);

                            if(item) {
                                row = {
                                    checked: row.checked,
                                    ...item,
                                };
                            }
                        });
                    } else {
                        this.list = data.list;
                    }
                    this.pagination.total = data.total;
                    this.loading = false;

                    if (this.pagination.page_index === 1) {
                        clearTimeout(this.taskTimer);
                        this.taskTimer = setTimeout(this.getTableList, 5000);
                    }
                }
            },

            currentPageChange(val) {
                if (this.watchRoute || this.turnPageRoute) {
                    this.$router.push({
                        query: {
                            ...this.search,
                            page_index: val,
                        },
                    });
                }
                this.pagination.page_index = val;
                if (val === 1) {
                    this.getTableList();
                }
            },

            selectionChange(row) {
                if(this.selection.length === 3 && row.checked) {
                    row.checked = false;
                    this.$message.warning('最多选择 3 个流程!');
                } else if(row.checked) {
                    this.selection.push(row);
                } else {
                    const $index = this.selection.findIndex(item => item.job_id === row.job_id);

                    if($index >= 0) {
                        this.selection.splice($index, 1);
                    }
                }
            },

            afterTableRender() {
                if(this.list.length){
                    const { ids } = this.$route.query;

                    this.selection = [];
                    if(ids) {
                        ids.split(',').forEach(id => {
                            this.selection.push({ job_id: id });
                        });
                    }

                    this.list.forEach((row, index) => {
                        const $index = this.selection.findIndex(x => x.job_id === row.job_id);

                        this.list[index] = {
                            ...row,
                            checked: $index >= 0,
                        };
                    });
                }
            },

            jobCompare() {
                const { href } = this.$router.resolve({
                    name:  'project-job-compare',
                    query: {
                        flow_id:     this.flow_id,
                        project_id:  this.project_id,
                        member_role: this.selection.map(row => row.my_role).join(','),
                        ids:         this.selection.map(row => row.job_id).join(','),
                    },
                });

                window.open(href, '_blank');
            },
        },
    };
</script>

<style lang="scss" scoped>
.job-history {
    float:right;
    margin-top: 5px;
    padding: 5px 10px;
}
.flex-cell {
    display: flex;
    justify-content: space-between;
    align-items: center;
    :deep(.el-icon-refresh-right) {
        font-size: 18px;
        color: #4c84ff;
        background: #efefef;
        padding: 8px;
        border-radius: 50%;
        cursor: pointer;
    }
}
.flex_box {
    height: 34px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 30px;
}
</style>
