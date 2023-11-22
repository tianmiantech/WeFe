<template>
    <div
        v-loading="loading"
        class="page"
    >
        <el-card
            shadow="never"
            class="mb30"
        >
            <div class="project-title">
                <h3 class="text-c">
                    {{ task.name }}
                    <br>
                    <span class="id">{{ task.business_id }}</span>
                </h3>
            </div>

            <el-row
                class="pt10"
                :gutter="20"
            >
                <el-col class="project-desc">
                    <p class="project-desc-value"><span class="project-desc-key">任务简介：</span>{{ task.description }}</p>
                    <p class="project-desc-time f14">创建于 {{ task.created_time | dateFormat }}</p>
                </el-col>
            </el-row>
        </el-card>

        <el-card
            shadow="never"
            class="mb30"
        >
            <h3 class="mb10 card-title">
                数据资源
            </h3>

            <el-form
                class="mb20"
                inline
            >
                <el-table
                    v-if="task.data_resource_type == 'DataSet'"
                    :data="task.data_set_list"
                    stripe
                    border
                >
                    <el-table-column
                        type="index"
                        label="编号"
                        width="45"
                    />
                    <el-table-column
                        label="名称 / ID"
                        min-width="154"
                    >
                        <template slot-scope="scope">
                            <strong>{{ scope.row.name }}</strong>
                            <p class="id">{{ scope.row.id }}</p>
                        </template>
                    </el-table-column>

                    <el-table-column
                        label="资源类型"
                        min-width="150"
                    >
                        <el-tag>
                            数据集
                        </el-tag>
                    </el-table-column>
                    <el-table-column
                        label="融合公式"
                        min-width="150"
                        prop="hash_function"
                    />
                    <el-table-column
                        label="数据量"
                        prop="row_count"
                        width="100"
                    />
                    <el-table-column
                        label="描述"
                        prop="description"
                        width="200"
                    />
                    <el-table-column
                        label="创建时间"
                        min-width="150"
                    >
                        <template slot-scope="scope">
                            {{ scope.row.created_time | dateFormat }}
                        </template>
                    </el-table-column>

                    <el-table-column
                        label="更新时间"
                        min-width="150"
                    >
                        <template slot-scope="scope">
                            {{ scope.row.updated_time | dateFormat }}
                        </template>
                    </el-table-column>
                </el-table>

                <el-table
                    v-if="task.data_resource_type == 'BloomFilter'"
                    :data="task.bloom_filter_list"
                    stripe
                    border
                >
                    <el-table-column
                        type="index"
                        label="编号"
                        width="45px"
                    />
                    <el-table-column
                        label="id/名称"
                        min-width="150"
                    >
                        <template slot-scope="scope">
                            <strong>{{ scope.row.name }}</strong>
                            <p class="id">{{ scope.row.id }}</p>
                        </template>
                    </el-table-column>

                    <el-table-column
                        label="资源类型"
                        min-width="150"
                    >
                        <el-tag>
                            布隆过滤器
                        </el-tag>
                    </el-table-column>
                    <el-table-column
                        label="主键加密方式"
                        prop="hash_function"
                        min-width="150"
                    />

                    <el-table-column
                        label="数据量"
                        prop="row_count"
                        width="100"
                    />
                    <el-table-column
                        label="描述"
                        prop="description"
                        width="200"
                    />
                    <el-table-column
                        label="创建时间"
                        min-width="150"
                    >
                        <template slot-scope="scope">
                            {{ scope.row.created_time | dateFormat }}
                        </template>
                    </el-table-column>

                    <el-table-column
                        label="更新时间"
                        min-width="150"
                    >
                        <template slot-scope="scope">
                            {{ scope.row.updated_time | dateFormat }}
                        </template>
                    </el-table-column>
                </el-table>
            </el-form>
        </el-card>

        <el-card
            shadow="never"
            class="mb30"
        >
            <h3 class="mb10 card-title">
                合作伙伴
            </h3>
            <el-table
                :data="task.partner_list"
                stripe
                border
            >
                <el-table-column
                    type="index"
                    label="编号"
                    width="45"
                />
                <el-table-column
                    label="名称 / ID"
                    min-width="150"
                >
                    <template slot-scope="scope">
                        <strong>{{ scope.row.member_name }}</strong>
                        <p class="id">{{ scope.row.member_id }}</p>
                    </template>
                </el-table-column>

                <!-- <el-table-column
                    label="数据量"
                    prop="rows_count"
                    width="100px"
                /> -->
                <el-table-column
                    label="调用域名"
                    prop="base_url"
                    min-width="200"
                />
            </el-table>
        </el-card>

        <el-card
            v-loading="running"
            shadow="never"
            class="mb30"
        >
            <h3 class="mb10 card-title">
                处理状态
            </h3>
            <el-form>
                <el-form-item
                    label="状态："
                    label-width="100"
                >
                    <TaskStatusTag
                        v-if="task.status"
                        :status="task.status"
                    />
                </el-form-item>

                <el-form-item
                    label="融合量："
                    label-width="100"
                >
                    {{ task.fusion_count }}
                </el-form-item>

                <el-form-item
                    v-if="task.status === 'Success'"
                    label="任务进度："
                    label-width="100"
                >
                    <el-progress
                        :percentage="100"
                        status="success"
                    />
                    <i class="id">
                        已处理数据 {{ task.processed_count || 0 }} 行,已融合数据 {{ task.fusion_count || 0 }} 行
                    </i>
                </el-form-item>

                <el-form-item
                    v-if="task.status === 'Failure' || task.status === 'Interrupt'"
                    label="任务进度："
                    label-width="100"
                >
                    <el-progress
                        :percentage="50"
                        status="exception"
                    />
                    <i class="id">
                        已处理数据 {{ task.processed_count || 0 }} 行,已融合数据 {{ task.fusion_count || 0 }} 行
                    </i>
                </el-form-item>

                <el-form-item
                    v-if="task.status === 'Running'"
                    label="任务进度："
                    label-width="100"
                >
                    <el-progress :percentage="task.progress || 0" />
                    <p class="id">
                        已处理数据 {{ task.processed_count || 0 }} 行,已融合数据 {{ task.fusion_count || 0 }} 行,预计还需 {{ dateFormatter(task.stimated_spend) }}
                    </p>
                </el-form-item>

                <el-form-item
                    label="耗时："
                    label-width="100"
                >
                    {{ dateFormatter(task.spend) }}
                </el-form-item>
                <el-form-item
                    v-if="task.result_table"
                    label="融合数据已存储到表："
                >
                    {{ task.result_table }}
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script>
    import TaskStatusTag from '@src/components/views/task-status-tag';

    export default {
        components: {
            TaskStatusTag,
        },
        data() {
            return {
                loading: false,
                running: false,
                // task
                task:    {
                    id:                  '',
                    business_id:         '',
                    partner_member_id:   '',
                    partner_member_name: '',
                    name:                '',
                    data_resource_id:    '',
                    data_resource_name:  '',
                    data_resource_type:  '',
                    created_time:        '',
                    fusion_count:        '',
                    processed_count:     '',
                    data_count:          '',
                    status:              '',
                    data_set_list:       [],
                    bloom_filter_list:   [],
                    partner_list:        [],
                    spend:               '',
                    stimated_spend:      '',
                    progress:            '',
                    result_table:        '',
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

                // dataResource
                dataSetList:     [],
                bloomFilterList: [],

                timer: null,
            };
        },
        created() {
            this.getData();
        },

        beforeDestroy() {
            clearInterval(this.timer);
            this.timer = null;
        },

        methods: {
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    '/task/detail',
                    params: {
                        id: this.$route.query.id,
                    },
                });

                this.loading = false;
                if (code === 0) {
                    this.task = data;
                    if(this.task.status === 'Running'){
                        this.timer = setTimeout(this.getTaskInfo, 3000);
                    }
                }
            },

            async getDataSet () {
                const { code, data } = await this.$http.get('/data_set/query');

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

            async getTaskInfo () {
                this.running = true;
                const { code, data } = await this.$http.get({
                    url:    '/task/info',
                    params: {
                        business_id: this.task.business_id,
                    },
                });

                setTimeout(_ => {
                    this.running = false;
                }, 1000);
                if (code === 0) {
                    this.task.fusion_count = data.fusion_count;
                    this.task.processed_count = data.processed_count;
                    this.task.stimated_spend = data.stimated_spend;
                    this.task.progress = data.progress;
                    this.task.status = data.status;
                    this.task.spend = data.spend;

                    if(data.status !== 'Running') {
                        this.getData();
                        clearTimeout(this.timer);
                    } else {
                        this.timer = setTimeout(this.getTaskInfo, 3000);
                    }
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

<style lang="scss" scoped>
    .project-title{
        position: relative;
        h3{margin: 10px;}
    }
    .project-desc{
        .project-desc-key{
            font-weight: bold;
        }
        .project-desc-value{
            text-indent: 8px;
            padding: 8px 0;
        }
        .project-desc-time{
            text-indent: 8px;
            padding: 8px 0;
            color: #4d84f7;
            text-decoration: underline;
            text-underline-position: under;
        }
    }
    .el-progress{max-width:400px;}
    .el-form{
        ::v-deep .el-form-item__label{
            font-weight: bold;
        }
    }
    .form-item__wrap{
        display:inline-block;
        vertical-align: top;
        .el-input,
        .el-textarea{
            min-width: 300px;
        }
    }
    .cell{
        .icon{
            cursor: pointer;
            &:hover{color:#5088fc;}
        }
    }
    .save-btn {
        width: 100px;
    }
</style>
