<template>
    <el-card
        v-loading="loading"
        shadow="never"
        class="page"
    >
        <h2 class="title">任务对比</h2>

        <el-table
            v-if="baseInfoList.length"
            class="compare-head"
            :data="baseInfoList[0]"
        >
            <el-table-column
                prop="$title"
                width="120"
            />
            <el-table-column
                v-for="member in members"
                :key="member.id"
                :label="member.name"
            >
                <template v-slot:header>
                    {{ member.name }}
                    <p class="head-id">
                        {{ member.id }}
                    </p>
                </template>
            </el-table-column>
        </el-table>
        <el-collapse v-model="activeName">
            <div class="nav-title" name="基础信息">
                <el-collapse-item
                    title="基础信息"
                    name="baseInfo"
                >
                    <el-table
                        v-for="(list, index) in baseInfoList"
                        :key="index"
                        :data="list"
                        :show-header="false"
                        stripe
                    >
                        <el-table-column
                            prop="$title"
                            width="120"
                        />
                        <el-table-column
                            v-for="member in members"
                            :key="member.id"
                            :label="member.name"
                        >
                            <template v-slot="scope">
                                {{ scope.row[member.id] }}
                            </template>
                        </el-table-column>
                    </el-table>
                </el-collapse-item>
            </div>
            <div class="nav-title" name="组件详情">
                <el-collapse-item
                    title="组件详情"
                    name="detail"
                >
                    <el-table
                        :data="detailList"
                        :show-header="false"
                        stripe
                    >
                        <el-table-column
                            prop="$title"
                            width="120"
                        />
                        <!-- Used to add page navigation -->
                        <el-table-column width="1">
                            <template v-slot="scope">
                                <div
                                    class="nav-title"
                                    :name="scope.$index + 1"
                                />
                            </template>
                        </el-table-column>
                        <el-table-column
                            v-for="member in members"
                            :key="member.id"
                            :label="member.name"
                        >
                            <template v-slot="scope">
                                <template v-if="scope.row[member.id] && scope.row[member.id].task">
                                    <h4 class="f14 mb10">
                                        <strong>{{ scope.row[member.id].task.component_name }}:</strong>
                                    </h4>
                                    <template v-if="componentsList[scope.row[member.id].task.task_type]">
                                        <component
                                            :autoReadResult="true"
                                            :is="`${scope.row[member.id].task.task_type}-result`"
                                            :flow-node-id="scope.row[member.id].task.flow_node_id"
                                            :flow-id="scope.row[member.id].task.flow_id"
                                            :job-id="scope.row[member.id].task.job_id"
                                            :my-role="member.my_role"
                                        />
                                    </template>
                                </template>
                                <p v-else>暂无结果</p>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-collapse-item>
            </div>
        </el-collapse>
    </el-card>
</template>

<script>
    import {
        componentsList,
        resultComponents,
    } from '../visual/component-list/component-map';

    export default {
        components: {
            ...componentsList,
            ...resultComponents,
        },
        data() {
            return {
                componentsList,
                loading:     false,
                flow_id:     '',
                ids:         [],
                members:     [],
                member_role: [],
                activeName:  ['baseInfo', 'detail'],
                jobStatus:   {
                    created:          '已创建',
                    wait_run:         '等待运行',
                    running:          '运行中',
                    stop:             '人为结束',
                    wait_stop:        '等待结束',
                    stop_on_running:  '人为关闭',
                    error_on_running: '程序异常关闭',
                    error:            '执行失败',
                    success:          '成功(正常结束)',
                },
                baseInfoList: [],
                detailList:   [],
            };
        },
        created() {
            const { flow_id, ids, member_role } = this.$route.query;

            this.flow_id = flow_id;
            this.member_role = member_role.split(',');
            if(ids) {
                this.ids = ids.split(',');
                this.getResult();
            }
        },
        methods: {
            async getResult() {
                const Queue = [];

                this.loading = true;
                this.ids.forEach((job_id, index) => {
                    Queue.push(this.$http.get({
                        url:    '/flow/job/detail',
                        params: {
                            flow_id:     this.flow_id,
                            member_role: this.member_role[index],
                            needResult:  true,
                            job_id,
                        },
                    }));
                });

                const res = await Promise.all(Queue);

                this.loading = false;
                if(res) {
                    const types = {
                        vertical:   '纵向',
                        horizontal: '横向',
                        mix:        '混合',
                    };
                    const baseInfoKey = {
                        status:   '运行状态',
                        my_role:  '我的角色',
                        during:   '执行时长',
                        job_type: '任务类型',
                        progress: '任务进度',
                    };

                    this.members = [];
                    this.baseInfoList = [];
                    for(const key in baseInfoKey) {
                        const val = baseInfoKey[key];

                        this.baseInfoList.push([{
                            $key:   key,
                            $title: val,
                        }]);
                    }
                    this.detailList = [];
                    res.forEach(({ code, data }) => {
                        if(code === 0) {
                            const { job, task_views } = data;

                            this.members.push(job);
                            this.baseInfoList.forEach(list => {
                                const row = list[0];

                                switch(row.$key) {
                                case 'during':
                                    row[job.id] = job.finish_time > job.start_time ? this.timeFormat(job.finish_time / 1000 > job.start_time / 1000) : '--';
                                    break;
                                case 'job_type':
                                    row[job.id] = types[job.federated_learning_type];
                                    break;
                                case 'status':
                                    row[job.id] = this.jobStatus[job[row.$key]];
                                    break;
                                case 'progress':
                                    row[job.id] = `${job[row.$key]}%`;
                                    break;
                                default:
                                    row[job.id] = job[row.$key];
                                    break;
                                }
                            });

                            task_views.forEach((task, index) => {
                                const value = {
                                    results: task.results || [],
                                    task:    task.task || {
                                        status: 'wait_run',
                                    },
                                };

                                if(this.detailList[index]) {
                                    this.detailList[index][job.id] = value;
                                } else {
                                    this.detailList[index] = {
                                        $title:   index + 1,
                                        [job.id]: value,
                                    };
                                }
                            });
                        }
                    });

                    this.$nextTick(_ => {
                        this.$bus.$emit('update-title-navigator');
                    });
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .page{margin-right: 100px;}
    .title{
        margin:0 0 20px;
        font-size: 18px;
    }
    .compare-head{
        :deep(.el-table__body-wrapper){display: none;}
    }
    .head-id{
        font-weight: normal;
        line-height: 14px;
        margin-top: 10px;
        font-size: 12px;
    }
    .el-collapse-item{
        :deep(.el-collapse-item__header):hover{background: #F5F7FA;}
    }
    .el-collapse{
        :deep(.el-collapse-item__content){padding-bottom: 0;}
    }
    .el-table{
        border-top:1px solid #EBEEF5;
        :deep(td){vertical-align: top;}
        :deep(.result){
            max-height: 1000px;
            overflow: auto;
        }
        :deep(.history-btn){display:none;}
    }
</style>
