<template>
    <el-card class="page">
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item label="成员名称:">
                <el-input v-model="search.name" clearable />
            </el-form-item>
            <el-form-item label="成员 ID:">
                <el-input v-model="search.id" clearable />
            </el-form-item>
            <el-form-item label="已隐身">
                <el-select v-model="search.hidden" style="width:100px;" clearable>
                    <el-option label="是" value="true" />
                    <el-option label="否" value="false" />
                </el-select>
            </el-form-item>
            <el-form-item label="已冻结">
                <el-select v-model="search.freezed" style="width:100px;" clearable>
                    <el-option label="是" value="true" />
                    <el-option label="否" value="false" />
                </el-select>
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
            <el-table-column label="成员名称" width="120">
                <template v-slot="scope">
                    <strong>{{ scope.row.name }}</strong>
                    <p class="p-id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>
            <el-table-column label="查看资源" width="100">
                <template v-slot="scope">
                    <router-link :to="{ name: 'data-list', query: { member_id: scope.row.id }}">
                        资源
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column label="成员状态">
                <template v-slot="scope">
                    {{ memberStatus(scope.row) }}
                </template>
            </el-table-column>
            <el-table-column label="企业实名认证" min-width="120">
                <template v-slot="scope">
                    <span v-if="scope.row.ext_json.real_name_auth_status === 0">未认证</span>
                    <span v-if="scope.row.ext_json.real_name_auth_status === 1">待审核</span>
                    <span v-if="scope.row.ext_json.real_name_auth_status === 2">已认证</span>
                    <template v-if="scope.row.ext_json.real_name_auth_status === -1">
                        <span>已拒绝</span>
                        <p class="color-danger">理由: {{ scope.row.ext_json.audit_comment }}</p>
                    </template>
                </template>
            </el-table-column>
            <el-table-column label="最后活跃时间" width="140">
                <template v-slot="scope">
                    {{ dateFormat(scope.row.last_activity_time) }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                fixed="right"
                min-width="300"
            >
                <template v-slot="scope">
                    <template v-if="!scope.row.status">
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
                            v-if="scope.row.ext_json.principal_name && scope.row.ext_json.real_name_auth_status === 1"
                            type="primary"
                            @click="authorized($event, scope.row)"
                        >
                            企业实名认证
                        </el-button>
                        <el-button
                            v-if="scope.row.ext_json.principal_name && scope.row.ext_json.real_name_auth_status === 2"
                            type="primary"
                            @click="authorized($event, scope.row, true)"
                        >
                            查看实名认证信息
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

        <el-dialog
            title="企业实名认证"
            v-model="authorize"
        >
            <div v-loading="pending">
                <el-form class="flex-form">
                    <el-form-item label="企业名称&类型:">
                        {{ member.principalName }} ({{ member.authType }})
                    </el-form-item>
                    <el-form-item label="企业简介:">
                        {{ member.description }}
                    </el-form-item>
                    <el-form-item>
                        <label class="el-form-item__label">附件:</label>
                        <ul>
                            <li
                                v-for="file in fileList"
                                :key="file.fileId"
                            >
                                <embed
                                    :type="file.type"
                                    :src="file.data"
                                    :alt="file.name"
                                    :style="`width: 100%;${file.type.includes('image') ? 'height:auto;' : 'min-height:calc(100vh - 50px);' }display:block;`"
                                >
                                <p class="mb10">{{ file.name }}</p>
                            </li>
                        </ul>
                        <span class="color-danger">无法查看?</span> 下载附件:
                        <p v-for="file in fileList" :key="file.fileId">
                            <el-link type="primary" :underline="false" @click="downloadFile($event, file)">{{file.name}}</el-link>
                        </p>
                    </el-form-item>
                </el-form>

                <div v-if="!authorizeReadOnly" class="text-r">
                    <el-button
                        type="danger"
                        @click="memberAuthorize($event, false)"
                    >
                        拒绝
                    </el-button>
                    <el-button
                        type="primary"
                        @click="memberAuthorize($event, true)"
                    >
                        通过
                    </el-button>
                </div>
            </div>
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
                    id:      '',
                    name:    '',
                    hidden:  '',
                    freezed: '',
                    status:  '',
                },
                statusMap: {
                    find:     '取消失联',
                    lost:     '失联',
                    freeze:   '冻结',
                    unfreeze: '解冻',
                },
                watchRoute:        true,
                defaultSearch:     true,
                requestMethod:     'post',
                getListApi:        '/member/query',
                authorize:         false,
                authorizeReadOnly: false,
                member:            {
                    id:            '',
                    principalName: '',
                    authType:      '',
                    description:   '',
                    list:          [],
                },
                pending:  false,
                fileList: [],
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
            async changeStatus(event, member, status) {
                const params = {
                    id: member.id,
                };

                switch (status) {
                case 'freeze':
                    params.freezed = true;
                    break;
                case 'unfreeze':
                    params.freezed = false;
                    break;
                }

                this.$confirm(`你确定要进行${ this.statusMap[status] }操作吗?`, '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                }).then(async _ => {
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
                });
            },
            authorized(event, row, readonly) {
                const list = row.ext_json.realname_auth_file_info_list;

                this.authorize = true;
                this.member.id = row.id;
                this.authorizeReadOnly = readonly;
                this.member.authType = row.ext_json.auth_type;
                this.member.description = row.ext_json.description;
                this.member.principalName = row.ext_json.principal_name;
                this.member.list = list;
                this.fileList = [];
                this.pending = true;

                list.forEach(file => {
                    this.getFile(file.file_id, list.length);
                });
            },
            blobToDataURI(blob, callback) {
                const reader = new FileReader();

                reader.readAsDataURL(blob);
                reader.onload = function (e) {
                    callback(e.target.result);
                };
            },
            async getFile(fileId, files) {
                const { code, data, response: { headers } } = await this.$http.post({
                    url:          '/download/file',
                    responseType: 'blob',
                    data:         {
                        fileId,
                    },
                });
                const contentDisposition = headers['content-disposition'] || headers['Content-Disposition'];

                let fileName = '';

                if (contentDisposition) {
                    fileName = window.decodeURI(contentDisposition.split('filename=')[1], 'UTF-8');
                }

                if(code === 0) {
                    this.blobToDataURI(data, result => {
                        this.fileList.push({
                            data: result,
                            name: window.decodeURIComponent(fileName),
                            type: data.type,
                            fileId,
                        });

                        if(this.fileList.length === files) {
                            setTimeout(() => {
                                this.pending = false;
                            }, 1000);
                        }
                    });
                }
            },
            async memberAuthorize(event, flag) {
                this.authorize = false;
                if(flag) {
                    const { code } = await this.$http.post({
                        url:  '/member/realname/auth/audit',
                        data: {
                            id:                 this.member.id,
                            realNameAuthStatus: 2,
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
                        cancelButtonText:  '取消',
                        confirmButtonText: '确定',
                    })
                        .then(async ({ value }) => {
                            const { code } = await this.$http.post({
                                url:  '/member/realname/auth/audit',
                                data: {
                                    id:                 this.member.id,
                                    auditComment:       value,
                                    realNameAuthStatus: -1,
                                },
                                btnState: {
                                    target: event,
                                },
                            });

                            if(code === 0) {
                                this.authorize = false;
                                this.$message.success('处理成功!');
                                setTimeout(() => {
                                    this.refresh();
                                }, 500);
                            }
                        });
                }
            },
            downloadFile(event, file) {
                if(this.loading) return;
                this.loading = true;

                const api = `${window.api.baseUrl}/download/file?fileId=${file.fileId}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = api;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();

                setTimeout(() => {
                    this.loading = false;
                }, 300);
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
