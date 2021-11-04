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
            <el-form-item label="成员 ID">
                <el-input v-model="search.id" />
            </el-form-item>
            <el-form-item>
                <el-checkbox label="已失联" v-model="search.lostContact"></el-checkbox>
                <el-checkbox label="已隐身" v-model="search.hidden"></el-checkbox>
                <el-checkbox label="已冻结" v-model="search.freezed"></el-checkbox>
                <el-checkbox label="已删除" v-model="search.status"></el-checkbox>
            </el-form-item>
            <el-button
                type="primary"
                native-type="submit"
                @click="getList({ to: true, resetPagination: true })"
            >
                搜索
            </el-button>
        </el-form>

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
            <el-table-column label="头像" width="120">
                <template v-slot="scope">
                    <MemberAvatar :img="scope.row.logo" />
                </template>
            </el-table-column>
            <el-table-column label="成员名称" prop="name" />
            <el-table-column label="查看数据集" width="100">
                <template v-slot="scope">
                    <router-link
                        :to="{ name: 'data-list', query: { member_id: scope.row.id }}"
                    >
                        数据集
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column label="最后活动时间" width="140">
                <template v-slot="scope">
                    {{ dateFormat(scope.row.last_activity_time) }}
                </template>
            </el-table-column>
            <el-table-column label="成员状态">
                <template v-slot="scope">
                    {{ memberStatus(scope.row) }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                fixed="right"
                width="240"
            >
                <template v-slot="scope">
                    <el-button
                        v-if="!scope.row.lost_contact"
                        type="danger"
                        @click="changeStatus($event, scope.row, 'lost')"
                    >
                        标记失联
                    </el-button>
                    <el-button
                        v-if="scope.row.lost_contact"
                        type="primary"
                        @click="changeStatus($event, scope.row, 'find')"
                    >
                        取消标记失联
                    </el-button>
                    <el-button
                        v-if="!scope.row.freezed"
                        type="danger"
                        @click="changeStatus($event, scope.row, 'freeze')"
                    >
                        冻结
                    </el-button>
                    <el-button
                        v-if="scope.row.freezed"
                        type="primary"
                        @click="changeStatus($event, scope.row, 'unfreeze')"
                    >
                        取消冻结
                    </el-button>
                    <el-button
                        v-if="!scope.row.ext_json.real_name_auth && scope.row.ext_json.principal_name"
                        type="primary"
                        @click="authorized($event, scope.row)"
                    >
                        企业认证
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

        <el-dialog
            title="企业认证"
            v-model="authorize"
        >
            <el-form class="flex-form">
                <el-form-item label="企业名称&类型">
                    {{ member.principalName }} ({{ member.authType }})
                </el-form-item>
                <el-form-item label="企业简介">
                    {{ member.description }}
                </el-form-item>
                <el-form-item label="附件:">
                    {{ member.description }}
                </el-form-item>
            </el-form>
            <el-button
                type="primary"
                @click="memberAuthorize($event, true)"
            >
                通过
            </el-button>
            <el-button
                type="danger"
                @click="memberAuthorize($event, false)"
            >
                拒绝
            </el-button>
        </el-dialog>
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
                checkList: '',
                search:    {
                    id:          '',
                    name:        '',
                    lostContact: '',
                    hidden:      '',
                    freezed:     '',
                    status:      '',
                },
                watchRoute:    true,
                defaultSearch: true,
                requestMethod: 'post',
                getListApi:    '/member/query',
                authorize:     false,
                member:        {
                    id:            '',
                    principalName: '',
                    authType:      '',
                    description:   '',
                    list:          [],
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
            memberStatus() {
                return member => {
                    const arr = [];

                    if(member.lost_contact) arr.push('已失联');
                    if(member.freezed) arr.push('已冻结');

                    return arr.join(', ') || '正常';
                };
            },
        },
        methods: {
            _getUrlParams() {
                const { query } = this.$route;
                const params = ['lostContact', 'freezed', 'hidden', 'status'];

                this.unUseParams = [];

                for (const $key in this.search) {
                    this.search[$key] = '';
                }
                params.forEach(key => {
                    const val = query[key];

                    if(val) {
                        this.search[key] = val === 'true';
                    } else {
                        this.search[key] = false;
                        this.unUseParams.push(key);
                    }
                });

            },
            async changeStatus(event, member, status) {
                const params = {
                    id: member.id,
                };

                switch (status) {
                case 'lost':
                    params.lostContact = true;
                    break;
                case 'find':
                    params.lostContact = false;
                    break;
                case 'freeze':
                    params.freezed = true;
                    break;
                case 'unfreeze':
                    params.freezed = false;
                    break;
                }

                const { code } = await this.$http.post({
                    url:      '/member/update',
                    data:     params,
                    btnState: {
                        target: event,
                    },
                });

                if(code === 0) {
                    this.refresh();
                }
            },
            authorized(event, row) {
                this.authorize = true;
                this.member.id = row.id;
                this.member.authType = row.auth_type;
                this.member.description = row.description;
                this.member.principalName = row.principal_name;
                this.member.list = row.real_name_auth_file_info_list;
            },
            async memberAuthorize(event, flag) {
                if(flag) {
                    const { code } = await this.$http.post({
                        url:  '/member/realname/auth/audit',
                        data: {
                            id:           this.member.id,
                            realNameAuth: true,
                            auditComment: '',
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        this.refresh();
                        this.$message.success('处理成功!');
                    }
                } else {
                    this.$prompt('原因:', '拒绝', {
                        inputPattern:      /\S/,
                        inputErrorMessage: '不能为空',
                    })
                        .then(async ({ value }) => {
                            const { code } = await this.$http.post({
                                url:  '/member/realname/auth/audit',
                                data: {
                                    id:           this.member.id,
                                    realNameAuth: false,
                                    auditComment: value,
                                },
                                btnState: {
                                    target: event,
                                },
                            });

                            if(code === 0) {
                                this.authorize = false;
                                this.$message.success('处理成功!');
                                this.refresh();
                            }
                        });
                }
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
    .el-icon-s-promotion{
        cursor: pointer;
        &:hover{color: $color-link-base;}
    }
</style>
