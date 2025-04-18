<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item label="合作者名称：">
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

            <el-form-item label="是否启用：">
                <el-select
                    v-model="search.status"
                    placeholder="请选择"
                    clearable
                >
                    <el-option
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
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
                    :to="{name: 'partner-service-add'}"
                >
                    <el-button>
                        为合作者开通服务
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
                <TableEmptyData />
            </div>
            <el-table-column
                label="序号"
                width="50"
                type="index"
            />
            <el-table-column
                label="合作者名称"
                width="220"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.client_name }}</p>
                    <p class="id">{{ scope.row.client_id }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="服务名称"
                width="310"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.service_name }}</p>
                    <p class="id">{{ scope.row.service_id }}</p>
                    <p class="id">url:{{ scope.row.url }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="服务类型"
                width="170"
            >
                <template slot-scope="scope">
                    <p>{{ scope.row.type === 0 ? scope.row.service_type : '激活服务' }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="调用方出口IP"
                width="200"
            >
                <template slot-scope="scope">
                    {{ scope.row.ip_add }}
                </template>
            </el-table-column>

            <el-table-column
                label="单价(￥)"
                width="65"
            >
                <template slot-scope="scope">
                    {{ scope.row.unit_price }}
                </template>
            </el-table-column>

            <el-table-column
                label="付费类型"
                width="70"
            >
                <template slot-scope="scope">
                    {{ scope.row.pay_type }}
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
                        v-if="scope.row.status === '未启用' && scope.row.type === 0"
                        type="success"
                        @click="open(scope.row,1)"
                    >
                        启用
                    </el-button>
                    <el-button
                        v-if="scope.row.status === '已启用' && scope.row.type === 0"
                        type="danger"
                        @click="open(scope.row,0)"
                    >
                        禁用
                    </el-button>
                    <router-link
                        style="padding-left: 3px"
                        :to="{
                            name: scope.row.type === 0 ?'partner-service-edit':'activate-service-edit',
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
import { mapGetters } from 'vuex';

export default {
    name:   'PartnerServiceList',
    mixins: [table],
    inject: ['refresh'],
    data() {
        return {
            fillUrlQuery: false,
            search:       {
                clientName:  '',
                status:      '',
                serviceName: '',
                type:        0,
            },
            options: [{
                value: '1',
                label: '已启用',
            }, {
                value: '0',
                label: '未启用',
            }],
            types: [
                {
                    value: '1',
                    label: '激活',
                },
                {
                    value: '0',
                    label: '开通',
                },
            ],
            list:             [],
            getListApi:       '/clientservice/query-list',
            changeStatusType: '',
        };
    },

    computed: {
        ...
            mapGetters(['userInfo']),
    },
    async created() {},
    methods: {
        open(row, status) {
            if(row.type === 1){
                return;
            }
            this.$alert(status === 1 ? '是否启用？' : '是否禁用？', '警告', {
                confirmButtonText: '确定',
                callback:          action => {
                    if (action === 'confirm') {
                        this.changeStatus(row, status);
                        setTimeout(() => {
                            this.refresh();
                        }, 1000);
                    }


                },
            });
        },

        async changeStatus(row, status) {

            const { code } = await this.$http.post({
                url:  '/clientservice/update_status',
                data: {
                    serviceId: row.service_id,
                    clientId:  row.client_id,
                    status,
                    updatedBy: this.userInfo.nickname,
                },
            });

            if (code === 0) {
                this.$message({
                    type:    'success',
                    message: status === 1 ? '启用成功' : '禁用成功',
                });
            }
        },
    },
};
</script>

<style scoped>

</style>
