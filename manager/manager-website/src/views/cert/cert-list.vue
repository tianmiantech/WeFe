<template>
    <el-card class="page">
        <el-table
            v-loading="loading"
            class="card-list"
            :data="list"
            border
            stripe
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column label="序号" type="index"></el-table-column>
            <el-table-column
                label="ID"
                width="240"
            >
                <template v-slot="scope">
                    <router-link class="mb10" :to="{ name: 'cert-view', query: { pk_id: scope.row.pk_id}}">
                        {{ scope.row.pk_id }}
                    </router-link>

                </template>
            </el-table-column>
            <el-table-column
                label="证书序列号"
                width="200"
            >
                <template v-slot="scope">
                    {{ scope.row.serial_number }}
                </template>

            </el-table-column>
            <el-table-column label="成员ID" width="230">
                <template v-slot="scope">
                    {{ scope.row.user_id }}
                </template>
            </el-table-column>
            <el-table-column label="subject_cn" width="120">
                <template v-slot="scope">
                    {{ scope.row.subject_cn }}
                </template>
            </el-table-column>
            <el-table-column label="subject_org" width="120">
                <template v-slot="scope">
                    {{ scope.row.subject_org }}
                </template>
            </el-table-column>
            <el-table-column label="是否是根证书" width="120">
                <template v-slot="scope">
                    {{ scope.row.is_root_cert }}
                </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
                <template v-slot="scope">
                    <el-tag :type="scope.row.status ? 'success' : 'danger'">
                        {{ scope.row.status ? '已启用' : '已禁用' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                fixed="right"
                min-width="240"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.status">
                        <el-button
                            type="danger"
                            @click="changeStatus($event, scope.row)"
                        >
                            置为有效
                        </el-button>
                    </template>
                    <template v-else>
                        <el-button
                            type="danger"
                            @click="changeStatus($event, scope.row)"
                        >
                            置为无效
                        </el-button>
                    </template>
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
import { mapGetters } from 'vuex';
import table from '@src/mixins/table';

export default {
    inject: ['refresh'],
    mixins: [table],
    data() {
        return {
            authorizeId:   '',
            authorizeName: '',
            search:        {
                name: '',
            },
            watchRoute:    true,
            defaultSearch: true,
            requestMethod: 'post',
            getListApi:    'cert/query',
            authorize:     false,
        };
    },
    computed: {
        ...mapGetters(['userInfo']),
    },
    methods: {
        changeStatus($event, row) {
            this.$confirm(`你确定要${ row.status ? '禁用' : '启用' }该证书吗?`, '警告', {
                type:              'warning',
                cancelButtonText:  '取消',
                confirmButtonText: '确定',
            }).then(async _ => {
                await this.$http.post({
                    url:  '/cert/update_status',
                    data: {
                        pk_id: row.pk_id,
                        status:row.status !== '1',
                    },
                    btnState: {
                        target: $event,
                    },
                });
            });
        },
    },
};
</script>

<style lang="scss" scoped>
.card-list{min-height: calc(100vh - 250px);}
.member-cards{
    margin-left: 40px;
    margin-bottom: 40px;
    position: relative;
    display: inline-block;
    vertical-align: top;
    :deep(.realname){font-size:40px;}
}
.more-info{
    width: 100%;
    font-size:14px;
    padding-left: 40px;
    padding-right:20px;
    color: $color-light;
    text-align: right;
    position: absolute;
    bottom: 15px;
    right:0;
}
.link{color: #eee;}
.el-icon-s-promotion{
    cursor: pointer;
    &:hover{color: $color-link-base;}
}
</style>
