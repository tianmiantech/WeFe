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
                label="成员ID："
                label-width="80px"
            >
                <el-input
                    v-model="search.member_id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="成员名称："
                label-width="100px"
            >
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>

            <el-button
                type="primary"
                @click="getList({ to: true })"
            >
                查询
            </el-button>
            <el-button
                @click="member.editor=true,
                        member.name='',
                        member.api='',
                        member.public_key='',
                        member.member_id='',
                        member.id=''"
            >
                新增
            </el-button>
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
                label="成员ID"
                width="300"
            >
                <template slot-scope="scope">
                    <p class="id">{{ scope.row.member_id }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="名称"
                width="200"
            >
                <template slot-scope="scope">
                    {{ scope.row.name }}
                </template>
            </el-table-column>
            <el-table-column
                label="预测接口地址"
                width="500"
            >
                <template slot-scope="scope">
                    {{ scope.row.api }}
                </template>
            </el-table-column>
            <el-table-column label="创建时间">
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                width="300px"
            >
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        @click="
                            member.editor=true,
                            member.name=scope.row.name,
                            member.api=scope.row.api,
                            member.public_key=scope.row.public_key,
                            member.member_id=scope.row.member_id,
                            member.id=scope.row.id"
                    >
                        编辑
                    </el-button>
                    <el-button
                        type="danger"
                        @click="delMember(scope.row.id)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog
            :visible.sync="member.editor"
            title="编辑成员信息"
            width="500px"
        >
            <el-form>
                <el-form-item
                    label="成员ID"
                    label-width="60px"
                >
                    <el-input v-model="member.member_id" />
                </el-form-item>
                <el-form-item
                    label="名称"
                    label-width="60px"
                >
                    <el-input v-model="member.name" />
                </el-form-item>
                <el-form-item
                    label="路径"
                    label-width="60px"
                >
                    <el-input v-model="member.api" />
                </el-form-item>
                <el-form-item
                    label="公钥"
                    label-width="60px"
                >
                    <el-input
                        v-model="member.public_key"
                        type="textarea"
                        autosize
                    />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="member.editor=false">取消</el-button>
                <el-button
                    type="primary"
                    :disabled="!member.member_id || !member.name || !member.api || !member.public_key"
                    @click="member.id?editMember():addMember()"
                >确定</el-button>
            </span>
        </el-dialog>

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
    // import RoleTag from '../components/role-tag';

    export default {
        components: {
            // RoleTag,
        },
        mixins: [table],
        data() {
            return {

                search: {
                    member_id: '',
                    name:      '',
                },

                getListApi: '/member/query',

                member: {
                    id:         '',
                    visible:    false,
                    editor:     false,
                    member_id:  '',
                    name:       '',
                    api:        '',
                    public_key: '',
                },
            };
        },
        methods: {
           async addMember () {
               const { code } = await this.$http.post({
                   url:  '/member/save',
                   data: {
                       member_id:  this.member.member_id,
                       name:       this.member.name,
                       api:        this.member.api,
                       public_key: this.member.public_key,
                   },
               });

               if (code === 0) {
                   this.member.editor = false;
                   this.$message('新增成功!');
                   this.getList();
               }
           },
           async editMember () {
               const { code } = await this.$http.post({
                   url:  '/member/save',
                   data: {
                       member_id:  this.member.member_id,
                       name:       this.member.name,
                       api:        this.member.api,
                       public_key: this.member.public_key,
                   },
               });

               if (code === 0) {
                   this.member.editor = false;
                   this.$message('编辑成功!');
                   this.getList();
               }
           },
           delMember (id) {
               this.$confirm('你确定要删除这个成员信息吗', '警告', {
                   type:              'warning',
                   confirmButtonText: '删除',
                   callback:          async action => {
                       if (action === 'confirm') {
                           const { code } = await this.$http.post({
                               url:  '/member/delete',
                               data: {
                                   id,
                               },
                           });

                           if (code === 0) {
                               this.member.editor = false;
                               this.$message('删除成功!');
                               this.getList();
                           }
                       }
                   },
               });
           },
        },
    };
</script>

<style lang="scss">
    .structure-table{
        .ant-table-title{
            font-weight: bold;
            text-align: center;
            padding: 10px;
            font-size:16px;
        }
    }
    .radio-group{
        margin-top: 10px;
        .el-radio{
            width: 90px;
            margin-bottom: 10px;
        }
        .el-radio__label{padding-left: 10px;}
    }
</style>
