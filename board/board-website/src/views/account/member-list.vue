<template>
    <el-card class="page">
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item label="成员名称">
                <el-input v-model="search.name" />
            </el-form-item>
            <!-- <el-form-item label="成员 ID">
                <el-input v-model="search.id" />
            </el-form-item> -->
            <el-button
                type="primary"
                native-type="submit"
                @click="getList({ resetPagination: true })"
            >
                搜索
            </el-button>
        </el-form>

        <ul
            v-loading="loading"
            class="card-list"
        >
            <EmptyData v-if="list.length === 0" />
            <template v-else>
                <li
                    v-for="member in list"
                    :key="member.id"
                    class="member-cards"
                >
                    <MemberCard
                        :form="member"
                        :edit="false"
                    >
                        <div class="more-info">
                            <div class="float-left">
                                最后活动时间:
                                <span>{{ dateFormat(member.last_activity_time) }}</span>
                            </div>
                            <router-link
                                class="link"
                                :to="{ name: member.id === userInfo.member_id ? 'data-list' : 'union-data-list', query: { member_id: member.id }}"
                            >
                                查看数据集
                            </router-link>
                        </div>
                    </MemberCard>
                </li>
            </template>
        </ul>

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
        mixins: [table],
        data() {
            return {
                search: {
                    name: '',
                    // id: '',
                },
                defaultSearch: true,
                getListApi:    '/union/member/query',
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
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
        :deep(.nickname){font-size:40px;}
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
</style>
