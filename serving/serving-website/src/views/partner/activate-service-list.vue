<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item label="服务提供商名称：">
                <el-input
                    v-model="search.clientName"
                    clearable
                />
            </el-form-item>

            <el-form-item label="服务名称：">
                <el-input
                    v-model="search.serviceName"
                    clearable
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="getList({ to: true })"
                >
                    查询
                </el-button>
                <router-link
                    class="ml10"
                    :to="{name: 'activate-service-add'}"
                >
                    <el-button>
                        激活外部服务
                    </el-button>
                </router-link>
            </el-form-item>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <div slot="empty">
                <TableEmptyData/>
            </div>
            <el-table-column
                label="序号"
                width="50"
                type="index"
            />
            <el-table-column
                label="服务提供商名称"
                width="150"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.client_name }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="服务名称"
                width="250"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="服务访问URL"
                width="400"
            >
                <template slot-scope="scope">
                    <el-tooltip
                        class="item"
                        effect="dark"
                        :content="scope.row.url"
                        placement="left-start"
                    >
                        <p>{{ scope.row.url }} </p>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column
                label="我的code"
                width="240"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.code }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="创建时间"
                width="120"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="创建人"
                width="100"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_by ? scope.row.created_by:"-" }}
                </template>
            </el-table-column>

            <el-table-column
                label="修改人"
                width="100"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_by ? scope.row.updated_by:"-" }}
                </template>
            </el-table-column>

            <el-table-column
                label="操作"
            >
                <template slot-scope="scope">
                    <el-button
                        type="danger"
                        @click="delete_activate(scope.row)"
                    >
                        删除
                    </el-button>
                    <router-link style="padding-left: 3px"
                                 :to="{
                            name: 'activate-service-edit',
                            query: {
                                serviceId: scope.row.service_id,
                                clientId: scope.row.client_id,
                            }
                        }"
                    >
                        <el-button>
                            修改
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
    </el-card>
</template>

<script>

import table from '@src/mixins/table.js';
import {mapGetters} from 'vuex';

export default {
    name: 'PartnerServiceList',
    mixins: [table],
    inject: ['refresh'],
    data() {
        return {
            fillUrlQuery: false,
            search: {
                clientName: '',
                status: '',
                serviceName: '',
                type:1,
            },
            options: [{
                value: '1',
                label: '已启用',
            }, {
                value: '0',
                label: '未启用',
            }],
            types:[
                {
                    value : '1',
                    label:'激活',
                },
                {
                    value : '0',
                    label:'开通',
                }
            ],
            list:[],
            getListApi:       '/clientservice/query-list',
            changeStatusType: '',
        };
    },

    computed: {
        ...
            mapGetters(['userInfo']),
    },
    async created() {
    },
    methods: {
        open(row, status) {
            if(row.type === 1){
                return;
            }
            this.$alert(status === 1 ? '是否启用？' : '是否禁用？', '警告', {
                confirmButtonText: '确定',
                callback: action => {
                    if (action === 'confirm') {
                        this.changeStatus(row, status);
                        setTimeout(() => {
                            this.refresh();
                        }, 1000);
                    }


                },
            });
        },
        delete_activate(row) {
            this.$confirm('确定删除？', '警告', {
                type: 'warning',
            }).then(async () => {
                const {code} = await this.$http.post({
                    url: '/clientservice/delete_activate',
                    data: {
                        serviceId: row.service_id,
                        clientId: row.client_id,
                    },
                });
                if (code === 0) {
                    this.$message('删除成功!');
                    setTimeout(() => {
                        this.refresh();
                    }, 1000);
                }
            });
        },
    },
};
</script>

<style scoped>

</style>
