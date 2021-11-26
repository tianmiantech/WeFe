<template>
    <el-table
        v-loading="vData.loading"
        :data="vData.list"
        stripe
        border
    >
        <el-table-column label="添加" width="60" v-slot="scope">
            <i
                title="快捷创建项目"
                class="el-icon-folder-add"
                @click="addDataSet($event, scope.row)"
            ></i>
        </el-table-column>
        <el-table-column
            label="成员"
            min-width="100"
        >
            <template v-slot="scope">
                <span
                    class="p-name"
                    @click="checkCard(scope.row.member_id)"
                >
                    <i class="iconfont icon-visiting-card" />
                    {{ scope.row.member_name }}
                </span>
                <span class="p-id">{{ scope.row.member_id }}</span>
            </template>
        </el-table-column>
        <el-table-column
            label="数据集"
            min-width="100"
        >
            <template v-slot="scope">
                <router-link :to="{ name: 'union-data-view', query: { id: scope.row.id }}">
                    {{ scope.row.name }}
                </router-link>
                <br>
                <span class="p-id">{{ scope.row.id }}</span>
            </template>
        </el-table-column>
        <el-table-column label="关键词">
            <template v-slot="scope">
                <template
                    v-for="(item, index) in scope.row.tags.split(',')"
                    :key="index"
                >
                    <el-tag
                        v-show="item"
                        class="mr10"
                    >
                        {{ item }}
                    </el-tag>
                </template>
            </template>
        </el-table-column>
        <el-table-column
            label="参与项目数"
            prop="usage_count_in_project"
            width="100"
        />
        <el-table-column
            label="上传时间"
            min-width="120"
        >
            <template v-slot="scope">
                {{ dateFormat(scope.row.created_time) }}
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

<script>
    import { reactive, getCurrentInstance } from 'vue';
    import table from '@src/mixins/table';
    export default {
        mixins: [table],
        props:  {
            tableLoading: Boolean,
            sourceType:   String,
            searchField:  {
                type:    Object,
                default: _ => {},
            },
        },
        setup(props, context) {
            const { ctx } = getCurrentInstance();
            const vData = reactive({
                getListApi:    '/union/image_data_set/query',
                defaultSearch: false,
                watchRoute:    false,
            });
            const methods = {
                async getDataList(opt) {
                    ctx.search = props.searchField;
                    // ctx.pagination.page_index = + props.$route.query.page_index || 1;
                    // ctx.pagination.page_size = + props.$route.query.page_size || 20;
                    await ctx.getList(opt);
                    console.log();
                },
            };

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .empty {
        flex: 1;
        height: 260px;
        line-height: 30px;
        padding:100px 0;
    }
</style>
