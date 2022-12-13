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
                width="300"
            >
                <template v-slot="scope">
                    {{ scope.row.pk_id }}
                </template>
            </el-table-column>
            <el-table-column label="所属用户ID" width="300">
                <template v-slot="scope">
                    {{ scope.row.user_id }}
                </template>
            </el-table-column>
            <el-table-column label="私钥算法" width="300">
                <template v-slot="scope">
                    {{ scope.row.key_alg }}
                </template>
            </el-table-column>
            <el-table-column label="创建时间" width="200">
                <template v-slot="scope">
                    <p>{{ dateFormat(scope.row.create_time) }}</p>
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
                watchRoute:    true,
                defaultSearch: true,
                requestMethod: 'post',
                getListApi:    'cert_key/query',
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
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
.manager-icon-s-promotion{
    cursor: pointer;
    &:hover{color: $color-link-base;}
}
</style>
