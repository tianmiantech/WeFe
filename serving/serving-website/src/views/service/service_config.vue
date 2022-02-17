<template>
    <el-dialog
        :visible.sync="visible"
        title="添加服务"
        width="700px"
    >
        <el-form inline>
            <el-form-item label="id:">
                <el-input
                    v-model="search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item label="服务名称:">
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>
            <el-button
                type="primary"
                @click="getList(false)"
            >
                搜索
            </el-button>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            border
            stripe
        >
            <el-table-column
                label="id"
                prop="id"
            />
            <el-table-column
                label="商户名称"
                prop="supplier_name"
            />
            <el-table-column
                label="服务名称"
                prop="name"
            />
            <el-table-column label="服务类型">
                <template slot-scope="scope">
                    {{ serviceTypeMap[scope.row.service_type] }}
                </template>
            </el-table-column>
            <el-table-column label="创建时间">
                <template slot-scope="scope">
                    {{ scope.row.created_time | dateFormat }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                width="70px"
            >
                <template slot-scope="scope">
                    <el-switch
                        v-model="scope.row.$checked"
                        :active-value="!checkedIds.includes(scope.row.id)"
                        :disabled="checkedIds.includes(scope.row.id)"
                        @change="rowChange(scope.row)"
                    />
                </template>
            </el-table-column>
        </el-table>

        <div
            v-if="pagination.total"
            class="mt20 flexbox"
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
            <el-button
                type="primary"
                @click="confirm"
            >
                确定
            </el-button>
        </div>
    </el-dialog>
</template>

<script>
    import table from '@src/mixins/table.js';

    export default {
        mixins: [table],
        props:  {
            serviceType: String,
        },
        data() {
            return {
                visible: false,
                search:  {
                    id:           '',
                    name:         '',
                    service_type: 3,
                },
                fillUrlQuery:   false,
                checkedIds:     [],
                checkedRows:    [],
                getListApi:     '/service/union/query',
                serviceTypeMap: {
                    1: '两方匿踪查询',
                    2: '两方交集查询',
                    3: '多方安全统计(被查询方)',
                    4: '多方安全统计(查询方)',
                    5: '多方交集查询',
                    6: '多方匿踪查询',
                },
            };
        },
        methods: {
            show(checkedIds) {
                this.visible = true;
                const type = +this.serviceType;

                if(type === 6) {
                    this.search.service_type = 1;
                } else if(type === 5) {
                    this.search.service_type = 2;
                }
                this.checkedIds = checkedIds;
                this.checkedRows = [];
                this.getList();
            },
            rowChange(row) {
                const index = this.checkedRows.findIndex(x => x.id === row.id);

                if(row.$checked && index < 0) {
                    this.checkedRows.push(row);
                } else if(!row.$checked && index >= 0) {
                    this.checkedRows.splice(index, 1);
                }
            },
            confirm() {
                this.visible = false;
                this.$emit('confirm-checked-rows', this.checkedRows);
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-pagination{flex:1;}
</style>
