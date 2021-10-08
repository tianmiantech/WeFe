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
                label="业务Id:"
                label-width="100px"
            >
                <el-input
                    v-model="search.business_id"
                    clearable
                />
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
                width="45px"
            />
            <el-table-column
                label="业务Id / 任务"
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
                width="240px"
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
                label="数据量"
                prop="data_count"
                width="120px"
            >
                <template slot-scope="scope">
                    样本量: {{ scope.row.row_count }} <br>
                    对齐量: {{ scope.row.data_count }} <br>
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
                min-width="50px"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="50px"
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
                    <router-link
                        :to="{name: 'task-pending-view', query: { id: scope.row.id }}"
                    >
                        <el-button
                            size="small"
                            type="primary"
                        >
                            审核
                        </el-button>
                    </router-link>
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

        <!--  <el-dialog
                      :title="策略"
                      :visible.sync="dataDialog"
                  >
                  <json-view :data="jsonData"/>
           </el-dialog> -->
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';
    // import jsonView from 'vue-json-views';
    import TaskStatusTag from '@src/components/views/task-status-tag';


    export default {
        // components: {
        //             jsonView,
        //         },
        components: {
            TaskStatusTag,
        },
        mixins: [table],
        data() {
            return {
                 search: {
                   id:     '',
                   status: 'Pending',
                 },
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

                 task: {
                   editor:             false,
                   id:                 '',
                   business_id:        '',
                   partner_id:         '',
                   partner_name:       '',
                   name:               '',
                   data_resource_id:   '',
                   data_resource_name: '',
                   data_resource_type: '',
                   data_count:         '',
                   row_count:          '',
                   fusion_count:       '',
                },
            };
        },
        async created() {

            await this.getStatus();

            this.getList();
        },
        methods: {

            async getStatus() {
                const { code, data } = await this.$http.get('/task/status',{
                     },
                );

                if(code === 0) {
                    this.taskStatusList = data;
               //     this.search.status = this.taskStatusList[0]
                }
            },

            showStrategys (string) {
                this.dataDialog = true;
                setTimeout(() => {
                    this.jsonData = string;
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
                };

                if (map.day) {
                    time = `${map.day}d${map.hours === 0 ? 1 : map.hours}h`;
                } else if (map.hours) {
                    time = `${map.hours}h${map.minutes === 0 ? 1 : map.minutes}min`;
                } else if (map.minutes >= 0) {
                    time = `${map.minutes === 0 ? 1 : map.minutes}min`;
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
