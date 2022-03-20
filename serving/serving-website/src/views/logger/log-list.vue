<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item label="流水号：">
                <el-input
                    v-model="search.seq_no"
                    clearable
                />
            </el-form-item>
            <el-form-item label="成员ID：">
                <el-input
                    v-model="search.member_id"
                    clearable
                />
            </el-form-item>
            <el-form-item label="模型ID：">
                <el-input
                    v-model="search.model_id"
                    clearable
                />
            </el-form-item>
            <el-form-item label="算法类型：">
                <el-select
                    v-model="search.algorithm"
                    clearable
                >
                    <el-option
                        v-for="(item) in algorithmOptions"
                        :key="item.value"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="训练类型：">
                <el-select
                    v-model="search.fl_type"
                    clearable
                >
                    <el-option
                        v-for="(item) in flTypeOptions"
                        :key="item.value"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>

            <el-form-item>
                <el-button
                    type="primary"
                    @click="getList({ to: true })"
                >
                    查询
                </el-button>
            </el-form-item>
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
                label="流水号"
                width="230px"
            >
                <template slot-scope="scope">
                    {{ scope.row.seq_no }}
                </template>
            </el-table-column>

            <el-table-column
                label="模型"
                width="390px"
            >
                <template slot-scope="scope">
                    <RoleTag :role="scope.row.my_role" />
                    {{ scope.row.name }}
                    <br>
                    <p class="id">model_id：{{ scope.row.model_id }}</p>
                    <p class="id">member_id: {{ scope.row.member_id }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="算法"
                width="130px"
            >
                <template slot-scope="scope">
                    <div v-if="scope.row.algorithm === 'LogisticRegression'">
                        逻辑回归<br>
                        <span class="id">{{ scope.row.algorithm }}</span>
                    </div>
                    <div v-else>
                        联邦安全树<br>
                        <span class="id">{{ scope.row.algorithm }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column
                label="联邦类型"
                width="80"
            >
                <template slot-scope="scope">
                    <div v-if="scope.row.fl_type === 'horizontal'">
                        横向
                    </div>
                    <div v-else>
                        纵向
                    </div>
                </template>
            </el-table-column>

            <el-table-column
                label="request"
                width="280"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.request">
                        <p>{{ scope.row.request.length > 100 ? scope.row.request.substring(0, 101) + '...' : scope.row.request }}</p>
                        <el-button
                            v-if="scope.row.request.length > 100"
                            type="text"
                            @click="showResquest(scope.row.request)"
                        >
                            查看更多
                        </el-button>
                    </template>
                </template>
            </el-table-column>

            <el-table-column
                label="response"
                width="280"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.response">
                        <p>{{ scope.row.response.length > 100 ? scope.row.response.substring(0, 101) + '...' : scope.row.response }}</p>
                        <el-button
                            v-if="scope.row.response.length > 100"
                            type="text"
                            @click="showResponse(scope.row.response)"
                        >
                            查看更多
                        </el-button>
                    </template>
                    <template v-else>
                        success
                    </template>
                </template>
            </el-table-column>

            <el-table-column
                label="创建时间"
                width="125"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column label="耗时(ms)">
                <template slot-scope="scope">
                    {{ scope.row.spend }}
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
            :title="title"
            :visible.sync="logDialog"
        >
            <json-view
                :value="jsonData"
                :expand-depth="5"
            />
        </el-dialog>
    </el-card>
</template>

<script>

    import 'vue-json-viewer/style.css';
    import table from '@src/mixins/table.js';
    import RoleTag from '../components/role-tag';
    import jsonView from 'vue-json-viewer';

    export default {
        components: {
            RoleTag,
            jsonView,
        },
        mixins: [table],
        data() {
            return {
                algorithmOptions: [
                    { value: 'LogisticRegression',label: '逻辑回归' },
                    { value: 'XGBoost',label: '安全树' },
                ],

                search: {
                    seq_no:    '',
                    member_id: '',
                    model_id:  '',
                    algorithm: '',
                    fl_type:   '',
                    my_role:   '',
                },

                flTypeOptions: [
                    { value: 'horizontal',label: '横向' },
                    { value: 'vertical',label: '纵向' },
                ],

                getListApi: '/log/query',

                title:     '',
                logDialog: false,
                jsonData:  '',
            };
        },
        methods: {
            // 删除
            deleteData(row) {
                this.$confirm('此操作将永久删除该条目, 是否继续?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/job/action/delete',
                        data: {
                            id: row.id,
                        },
                    });

                    if(code === 0) {
                        this.$message.success('删除成功!');
                        this.getList();
                    }
                });
            },
            showResquest (data) {
                this.logDialog = true;
                this.title = '请求体';
                setTimeout(() => {
                    this.jsonData = JSON.parse(data);
                });
            },
            showResponse (data) {
                this.logDialog = true;
                this.title = '响应体';
                setTimeout(() => {
                    this.jsonData = JSON.parse(data);
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
