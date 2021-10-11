<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item
                label="服务id："
                min-width="80"
            >
                <el-input
                    v-model="search.service_id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="实例名称："
                min-width="100"
            >
                <el-input
                    v-model="search.instance_name"
                    clearable
                />
            </el-form-item>
            <el-button
                type="primary"
                native-type="submit"
                @click="getList({ to: true, resetPagination: true })"
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
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column
                label="服务id"
                prop="service_id"
                min-width="250"
            />
            <el-table-column
                label="实例名称"
                prop="instance_name"
                min-width="250"
            />
            <el-table-column
                label="服务类型"
                prop="service_type"
                min-width="250"
            />
            <el-table-column
                label="实例URI"
                prop="instance_uri"
                min-width="250"
            />
            <el-table-column
                label="可用性检测周期"
                prop="check_availability_interval"
                min-width="150"
            />

            <el-table-column
                label="最后心跳时间"
                min-width="150"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.last_heartbeat_time) }}
                </template>
            </el-table-column>

            <el-table-column
                label="状态"
                min-width="100"
            >
                <template
                    v-slot="scope"
                >
                    <span
                        v-if="scope.row.status == 'success'"
                        class="super_admin_role"
                    >
                        <i
                            class="el-icon-check"
                            style="color: #67C23A;font-size: 25px"
                        />
                    </span>
                    <span
                        v-else
                        class="not_super_admin_role"
                    >
                        <i
                            class="el-icon-close color-danger"
                            style="font-size: 25px;"
                        />
                    </span>
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
                    service_id:    '',
                    instance_name: '',
                },
                getListApi:     '/service/list',
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
            };
        },
        created() {
            this.getList();
        },
    };
</script>
