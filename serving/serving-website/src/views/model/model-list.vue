<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item
                label="模型ID："
                label-width="80px"
            >
                <el-input
                    v-model="search.model_id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="算法类型："
                label-width="100px"
            >
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
            <el-form-item
                label="训练类型："
                label-width="100px"
            >
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

            <el-button
                type="primary"
                @click="getList({ to: true })"
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
            <div slot="empty">
                <TableEmptyData />
            </div>
            <el-table-column
                label="模型ID"
                min-width="150"
            >
                <template slot-scope="scope">
                    <RoleTag :role="scope.row.my_role" />
                    {{ scope.row.name }}
                    <br>
                    <p class="id">{{ scope.row.model_id }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="算法类型"
                min-width="100"
            >
                <template slot-scope="scope">
                    <div v-if="scope.row.algorithm === 'LogisticRegression'">
                        逻辑回归<br>
                        <span class="id">{{ scope.row.algorithm }}</span>
                    </div>
                    <div v-else>
                        安全树<br>
                        <span class="id">{{ scope.row.algorithm }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column
                label="联邦类型"
                min-width="50"
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
                label="是否在线"
                min-width="50"
            >
                <template slot-scope="scope">
                    <div v-if="scope.row.enable === true">
                        是
                    </div>
                    <div v-else>
                        否
                    </div>
                </template>
            </el-table-column>

            <el-table-column
                label="创建时间"
                min-width="140"
            >
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>

            <el-table-column
                label="更新时间"
                min-width="140"
            >
                <template slot-scope="scope">
                    {{ scope.row.updated_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                align="center"
                min-width="160"
            >
                <template slot-scope="scope">
                    <el-button
                        :type="scope.row.enable === true ? 'warning' : 'success'"
                        @click="changeEnable(scope.row)"
                    >
                        <div v-if="scope.row.enable === true">
                            下线
                        </div>
                        <div v-else>
                            上线
                        </div>
                    </el-button>

                    <el-button
                        type="primary"
                        @click="predict(scope.row)"
                    >
                        配置
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
    </el-card>
</template>

<script>

    import table from '@src/mixins/table.js';
    import RoleTag from '../components/role-tag';

    export default {
        components: {
            RoleTag,
        },
        mixins: [table],
        data() {
            return {
                algorithmOptions: [
                    { value: 'LogisticRegression', label: '逻辑回归' },
                    { value: 'XGBoost', label: '安全树' },
                ],

                search: {
                    model_id:  '',
                    algorithm: '',
                    flType:    '',
                    creator:   '',
                },

                getListApi: '/model/query',

                flTypeOptions: [
                    { value: 'horizontal', label: '横向' },
                    { value: 'vertical', label: '纵向' },
                ],

                modelingResult: [],
                form:           {
                    model_id:       '',
                    algorithm:      '',
                    fl_type:        '',
                    model_param:    '',
                    feature_source: '',
                    my_role:        '',
                },
            };
        },
        methods: {
            // 获取标签列表
            async getTags() {
                const { code, data } = await this.$http.get('/data_set/tags');

                if (code === 0) {
                    this.tagList = data;
                }
            },
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

                    if (code === 0) {
                        this.$message.success('删除成功!');
                        this.getList();
                    }
                });
            },
            predict(row) {

               const my_role = row.my_role;

                localStorage.setItem('my_role', my_role);
                this.$router.push({ name: 'model-view', query: { id: row.id } });
            },
            changeEnable(row) {
                const str = row.enable ? '下线' : '上线';

                this.$confirm('确定对此模型做' + str + '操作?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/model/enable',
                        data: {
                            id:     row.id,
                            enable: !row.enable,
                        },
                    });

                    if (code === 0) {
                        this.$message.success('操作成功!');
                        this.getList();
                    }
                });
            },
        },
    };
</script>

<style lang="scss">
    .structure-table {
        .ant-table-title {
            font-weight: bold;
            text-align: center;
            padding: 10px;
            font-size: 16px;
        }
    }

    .radio-group {
        margin-top: 10px;
        .el-radio {
            width: 90px;
            margin-bottom: 10px;
        }
        .el-radio__label {
            padding-left: 10px;
        }
    }
</style>
