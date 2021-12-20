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
                label="服务名称:"
                label-width="80px"
            >
                <el-input v-model="search.name" />
            </el-form-item>

            <el-form-item
                label="服务类型:"
                label-width="100px"
            >
                <el-select
                    v-model="search.service_type"
                    size="medium"
                    clearable
                >
                    <el-option
                        v-for="item in ServiceTypeList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
                    />
                </el-select>
            </el-form-item>

            <el-form-item
                label="是否在线:"
                label-width="100px"
            >
                <el-select
                    v-model="search.status"
                    size="medium"
                    clearable
                >
                    <el-option
                        v-for="item in StatusList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
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
                :to="{name: 'service-view'}"
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
                label="ID"
                min-width="120px"
            >
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="名称"
                prop="name"
                width="300px"
            />

            <el-table-column
                label="服务类型"
                prop="service_type"
                width="100px"
            >
                <template slot-scope="scope">
                    <div v-if="scope.row.service_type == 1">
                        匿踪查询
                    </div>
                    <div v-else-if="scope.row.service_type == 2">
                        交集查询
                    </div>
                    <div v-else>
                        安全聚合
                    </div>
                </template>
            </el-table-column>
            <el-table-column
                label="状态"
                prop="status"
                width="100px"
            >
                <template slot-scope="scope">
                    <div v-if="scope.row.status == 0">
                        离线
                    </div>
                    <div v-else>
                        在线
                    </div>
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
                width="150px"
                fixed="right"
            >
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        @click="editService(scope.row)"
                    >
                        编辑
                    </el-button>
                    <el-button
                        type="primary"
                        @click="online(scope.row.id)"
                     v-if="scope.row.status == 0">
                        上线
                    </el-button>
                    <el-button
                        type="danger"
                        @click="offline(scope.row.id)" v-else>
                        下线
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
                    name:   '',
                    service_type: '',
                    status: '',
                },
                headers: {
                    token: localStorage.getItem('token') || '',
                },
                getListApi:     '/service/query',
                userList:       [],
                taskStatusList: [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                dataDialog: false,
                jsonData:   '',
                ServiceTypeList: [{
                    name:  '匿踪查询',
                    value: '1',
                },
                {
                    name:  '交集查询',
                    value: '2',
                },
                {
                    name:  '安全聚合',
                    value: '3',
                }],
                StatusList:[{
                    name:  '在线',
                    value: '1',
                },
                {
                    name:  '离线',
                    value: '0',
                }]
            };
        },
        created() {
            this.getList();
        },
        methods: {

            showStrategys (string) {
                this.dataDialog = true;
                setTimeout(() => {
                    this.jsonData = string;
                });
            },

            editService(row) {
                this.$router.replace({
                    name:  'service-view',
                    query: { id: row.id},
                });
            },

            async offline (id) {
                this.$confirm('是否下线该服务？', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/service/offline',
                        data: {
                            id,
                        },
                    });

                    if (code === 0) {
                        this.$message('下线成功!');
                        this.getList();
                    }
                });
            },
            async online (id) {
                this.$confirm('是否上线该服务？', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/service/online',
                        data: {
                            id,
                        },
                    });

                    if (code === 0) {
                        this.$message('上线成功!');
                        this.getList();
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
