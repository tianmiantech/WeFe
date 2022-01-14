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
                <el-input v-model="search.serviceName" />
            </el-form-item>

            <el-form-item
                label="服务类型:"
                label-width="100px"
            >
                <el-select
                    v-model="search.serviceType"
                    size="medium"
                    clearable
                >
                    <el-option
                        v-for="item in serviceTypeList"
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
                        value="1"
                        label="在线"
                    />
                    <el-option
                        value="0"
                        label="离线"
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
                width="45px"
            />
            <el-table-column
                label="名称"
                min-width="240px"
            >
                <template slot-scope="scope">
                    {{ scope.row.name }}
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>

            <el-table-column
                label="服务类型"
                prop="service_type"
                min-width="100px"
            >
                <template slot-scope="scope">
                    {{ serviceTypeMap[scope.row.service_type] }}
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
                min-width="160px"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="160px"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
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
                serviceName: '',
                serviceType: '',
                status:      '',
            },
            getListApi:      '/service/union/query',
            userList:        [],
            serviceList:     [],
            dataDialog:      false,
            jsonData:        '',
            serviceTypeList: [
                {
                    name:  '两方匿踪查询',
                    value: '1',
                },
                {
                    name:  '多方匿踪查询',
                    value: '6',
                },
                {
                    name:  '两方交集查询',
                    value: '2',
                },
                {
                    name:  '多方交集查询',
                    value: '5',
                },
                {
                    name:  '多方安全统计(查询方)',
                    value: '4',
                },
                {
                    name:  '多方安全统计(被查询方)',
                    value: '3',
                },
            ],
            serviceTypeMap: {
                1: '两方匿踪查询',
                2: '两方交集查询',
                3: '多方安全统计(被查询方)',
                4: '多方安全统计(查询方)',
                5: '多方交集查询',
                6: '多方匿踪查询',
            },
        };
    },

    created() {
        this.getUnionService();

    },
    methods: {

        // async getUnionService() {
        //
        //     const {code, data} = await this.$http.post({
        //         url: '/union/query-list',
        //         data: {
        //             serviceName: this.search.serviceName,
        //             serviceType: this.search.serviceType,
        //             status: this.search.status,
        //         }
        //     });
        //
        //     if (code === 0) {
        //         this.list = data.list
        //     }
        // }

    },
};
</script>

<style lang="scss">
.structure-table {
    .ant-table-title {
        font-weight: bold;
        text-align: center;
        padding: 10px;
        font-size: 16px;
    }
}
</style>
