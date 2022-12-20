<template>
    <el-card class="page">
        <template v-if="(list && list.length)">
            <el-table
                v-loading="loading"
                class="card-list"
                :data="list"
                border
                stripe
            >
                <el-table-column label="序号" type="index"></el-table-column>
                <el-table-column
                    label="ID"
                    width="200"
                >
                    <template v-slot="scope">
    <!--                    <router-link class="mb10" :to="{ name: 'cert-view', query: { pk_id: scope.row.pk_id}}">-->
    <!--                        {{ scope.row.pk_id }}-->
    <!--                    </router-link>-->
                        {{ scope.row.pk_id }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="证书序列号"
                    width="170"
                >
                    <template v-slot="scope">
                        {{ scope.row.serial_number }}
                    </template>

                </el-table-column>
                <el-table-column label="成员ID" width="200">
                    <template v-slot="scope">
                        {{ scope.row.user_id }}
                    </template>
                </el-table-column>
                <el-table-column label="申请人(组织名称/常用名)" width="150">
                    <template v-slot="scope">
                        {{ scope.row.subject_org }} / {{ scope.row.subject_cn }}
                    </template>
                </el-table-column>
                <el-table-column label="签发人(组织名称/常用名)" width="150">
                    <template v-slot="scope">
                        {{ scope.row.issuer_org }} / {{ scope.row.issuer_cn }}
                    </template>
                </el-table-column>
                <el-table-column label="根证书" width="80">
                    <template v-slot="scope">
                        {{ scope.row.is_root_cert ? '是' : '否' }}
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" width="200">
                    <template v-slot="scope">
                        <p>{{ dateFormat(scope.row.create_time) }}</p>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="90">
                    <template v-slot="scope">
                        <el-tag :type="scope.row.status ? 'success' : 'danger'">
                            {{ scope.row.status ? '已启用' : '已禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="145">
                    <template v-slot="scope">
    <!--                    <template v-if="scope.row.status === 0 && !scope.row.is_ca_cert">-->
    <!--                        <el-button-->
    <!--                            type="primary"-->
    <!--                            @click="changeStatus($event, scope.row.pk_id, 2)"-->
    <!--                        >-->
    <!--                            置为有效-->
    <!--                        </el-button>-->
    <!--                    </template>-->
                        <template v-if="scope.row.status === 2 && !scope.row.is_ca_cert">
                            <el-button
                                type="danger"
                                @click="changeStatus($event, scope.row.pk_id, 0)"
                            >
                                置为无效
                            </el-button>
                        </template>

                        <template v-if="scope.row.status === 2 && scope.row.is_ca_cert && !scope.row.can_trust">
                            <el-button
                                type="primary"
                                @click="trustCert($event, scope.row.pk_id, 'add')"
                            >
                                添加到信任库
                            </el-button>
                        </template>

                        <template v-if="scope.row.status === 2 && scope.row.is_ca_cert && scope.row.can_trust">
                            <el-button
                                type="danger"
                                @click="trustCert($event, scope.row.pk_id, 'delete')"
                            >
                                从信任库移除
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
        </template>

        <template v-else>
            <div style="text-align: center;">
                <el-form
                    inline
                    :model="init_form"
                    label-width="100px"
                >
                    <el-row>
                        <el-col :span="24" style="">
                            <el-form-item label="所属组织名称:">
                                <el-input v-model="init_form.organization_name" placeholder="Welab Inc." />
                            </el-form-item>
                        </el-col>
                        <el-col :span="24">
                        <el-form-item label="常用名:">
                                <el-input v-model="init_form.common_name" placeholder="Welab" />
                            </el-form-item>
                        </el-col>
                        <el-col :span="24">
                            <el-form-item label="所属单位名称:">
                                <el-input v-model="init_form.organization_unit_name" placeholder="IT" />
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-form>
                <el-button
                    type="primary"
                    native-type="submit"
                    @click="initRoot($event)"
                >
                    初始化根证书
                </el-button>
            </div>
        </template>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table';
    import EmptyData from '@comp/Common/EmptyData';

    export default {
        components: { EmptyData },
        inject:     ['refresh'],
        mixins:     [table],
        data() {
            return {
                authorizeId:   '',
                authorizeName: '',
                init_form:     {
                    common_name:            '',
                    organization_name:      '',
                    organization_unit_name: '',
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
            changeStatus($event, pk_id, status) {
                this.$confirm(`你确定要${status === 0 ? '禁用' : '启用'}该证书吗? 此操作不可逆`, '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                }).then(async _ => {
                    const { code } = await this.$http.post({
                        url:  '/cert/update_status',
                        data: {
                            cert_id: pk_id,
                            status,
                        },
                        btnState: {
                            target: $event,
                        },
                    });

                    if(code === 0) {
                        this.$message.success('操作成功!');
                        this.refresh();
                    }
                });
            },
            trustCert($event, pk_id, op){
                this.$confirm(`你确定要${op === 'add' ? '添加' : '删除'}该证书到信任库吗?`, '警告', {
                    type:              'warning',
                    cancelButtonText:  '取消',
                    confirmButtonText: '确定',
                }).then(async _ => {
                    const { code } = await this.$http.post({
                        url:  '/trust/certs/update',
                        data: {
                            cert_id: pk_id,
                            op,
                        },
                        btnState: {
                            target: $event,
                        },
                    });

                    if(code === 0) {
                        this.$message.success('操作成功!');
                        this.refresh();
                    }
                });
            },
            async initRoot($event) {
                const { code } =await this.$http.post({
                    url:  '/cert/init_root',
                    data: {
                        common_name:            this.init_form.common_name,
                        organization_name:      this.init_form.organization_name,
                        organization_unit_name: this.init_form.organization_unit_name,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.$message.success('初始化成功!');
                    setTimeout(() => {
                        this.refresh();
                    }, 1000);
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
.card-list {
    min-height: calc(100vh - 250px);
}

.member-cards {
    margin-left: 40px;
    margin-bottom: 40px;
    position: relative;
    display: inline-block;
    vertical-align: top;

    :deep(.realname) {
        font-size: 40px;
    }
}

.more-info {
    width: 100%;
    font-size: 14px;
    padding-left: 40px;
    padding-right: 20px;
    color: $color-light;
    text-align: right;
    position: absolute;
    bottom: 15px;
    right: 0;
}

.link {
    color: #eee;
}

.manager-icon-s-promotion {
    cursor: pointer;

    &:hover {
        color: $color-link-base;
    }
}
</style>
