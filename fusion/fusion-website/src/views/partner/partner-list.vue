<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item label="合作方id:">
                <el-input
                    v-model="search.member_id"
                    clearable
                />
            </el-form-item>

            <el-form-item label="合作方名称:">
                <el-input
                    v-model="search.member_name"
                    clearable
                />
            </el-form-item>

            <el-form-item>
                <el-button
                    type="primary"
                    @click="getList('to')"
                >
                    查询
                </el-button>

                <el-button
                    class="ml20"
                    @click="
                        partner.editor=true,
                        partner.id='',
                        partner.member_name='',
                        partner.member_id='',
                        partner.rsa_public_key='',
                        partner.base_url=''
                    "
                >
                    新增
                </el-button>
            </el-form-item>
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
                prop="member_id"
                label="id"
                min-width="200px"
            />
            <el-table-column
                label="合作方"
                prop="member_name"
                min-width="140px"
            />

            <el-table-column
                label="调用域名"
                prop="base_url"
                min-width="260px"
            />
            <el-table-column
                label="操作"
                width="160"
            >
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        @click="
                            partner.editor=true,
                            partner.id=scope.row.id,
                            partner.member_id=scope.row.member_id,
                            partner.member_name=scope.row.member_name,
                            partner.rsa_public_key=scope.row.rsa_public_key,
                            partner.base_url=scope.row.base_url"
                    >
                        编辑
                    </el-button>

                    <el-button
                        type="danger"
                        @click="deletePartner(scope.row.id)"
                    >
                        删除
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
            :visible.sync="partner.editor"
            title="添加合作方"
            width="600px"
        >
            <div class="el-alert--info is-light">
                <div>
                    <span class="el-form-item__label">合作方ID：</span>
                    <span class="el-form-item__content">合作方的fusion系统成员ID</span>
                </div>
                <div>
                    <span class="el-form-item__label">合作方名称：</span>
                    <span class="el-form-item__content">填写合作方名称</span>
                </div>
                <div>
                    <span class="el-form-item__label">调用地址（域名/IP）：</span>
                    <span class="el-form-item__content">合作方fusion融合系统的外网地址</span>
                </div>
                <div>
                    <span class="el-form-item__label">合作方公钥：</span>
                    <span class="el-form-item__content">合作方融合系统的成员公钥</span>
                </div>
            </div>
            <el-form class="mt20">
                <el-form-item
                    label="合作方id"
                    label-width="100px"
                    required
                >
                    <el-input v-model="partner.member_id" />
                </el-form-item>
                <el-form-item
                    label="合作方"
                    label-width="100px"
                    required
                >
                    <el-input v-model="partner.member_name" />
                </el-form-item>
                <el-form-item
                    label="调用域名"
                    label-width="100px"
                    required
                >
                    <el-input v-model="partner.base_url" />
                </el-form-item>

                <el-form-item
                    label="公钥"
                    label-width="100px"
                    required
                >
                    <el-input
                        v-model="partner.rsa_public_key"
                        type="textarea"
                        autosize
                    />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="partner.editor=false">取消</el-button>
                <el-button
                    v-loading="loading"
                    type="primary"
                    :disabled="!partner.member_name || !partner.member_id || !partner.rsa_public_key"
                    @click="partner.id ? editPartner($event): addPartner($event)"
                >确定</el-button>
            </span>
        </el-dialog>
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';

    export default {
        mixins: [table],
        data() {
            return {
                loading: false,
                search:  {
                    member_id:   '',
                    member_name: '',
                },
                headers: {
                    token: localStorage.getItem('token') || '',
                },
                getListApi: '/partner/paging',
                partner:    {
                    editor:         false,
                    id:             '',
                    member_id:      '',
                    member_name:    '',
                    rsa_public_key: '',
                    base_url:       '',
                },
            };
        },
        created() {
            this.getList();
        },
        methods: {
            async addPartner () {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/partner/add',
                    data: {
                        member_name:    this.partner.member_name,
                        member_id:      this.partner.member_id,
                        rsa_public_key: this.partner.rsa_public_key,
                        base_url:       this.partner.base_url,
                    },
                });

                this.loading = false;
                if (code === 0) {
                    this.partner.editor = false;
                    this.$message('新增成功!');
                    this.getList();
                }
            },

            async editPartner () {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/partner/update',
                    data: {
                        id:             this.partner.id,
                        member_name:    this.partner.member_name,
                        member_id:      this.partner.member_id,
                        rsa_public_key: this.partner.rsa_public_key,
                        base_url:       this.partner.base_url,
                    },
                });

                this.loading = false;
                if (code === 0) {
                    this.partner.editor = false;
                    this.$message('更新成功!');
                    this.getList();
                }
            },


            async deletePartner (id) {

                this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/partner/delete',
                        data: {
                            id,
                        },
                    });

                    if (code === 0) {
                        this.$message('删除成功!');
                        this.getList();
                    }
                });
            },
        },
    };
</script>

<style lang="scss">
    .el-alert--info{
        padding:10px;
        .el-form-item__label{
            width:170px;
            line-height: 20px;
            color: #28C2D7;
        }
        .el-form-item__content{
            line-height: 20px;
        }
    }
</style>
