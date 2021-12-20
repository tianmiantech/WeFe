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
                @click="methods.addDataSet($event, scope.row)"
            ></i>
        </el-table-column>
        <el-table-column
            label="成员"
            min-width="100"
        >
            <template v-slot="scope">
                <span
                    class="p-name"
                    @click="methods.checkCard(scope.row.member_id)"
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
                <router-link :to="{ name: 'union-data-view', query: { id: scope.row.data_set_id, type: 'img' }}">
                    {{ scope.row.name }}
                </router-link>
                <br>
                <span class="p-id">{{ scope.row.data_set_id }}</span>
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
            label="数据量"
            prop="total_data_count"
            width="100"
        />
        <el-table-column
            label="样本分类"
            prop="for_job_type"
            width="100"
        >
            <template v-slot="scope">
                {{scope.row.for_job_type === 'classify' ? '图像分类' : scope.row.for_job_type === 'detection' ? '目标检测' : '-'}}
            </template>
        </el-table-column>
        <el-table-column
            label="标注状态"
            prop="label_completed"
        >
            <template v-slot="scope">
                {{ scope.row.label_completed === '1' ? '已完成' : '标注中' }}
            </template>
        </el-table-column>
        <el-table-column
            label="参与项目数"
            prop="usage_count_in_project"
            width="100"
        />
        <el-table-column
            label="上传时间"
            min-width="100"
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
        emits: ['add-data-set', 'check-card'],
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
                    await ctx.getList(opt);
                },
                addDataSet(ev, item) {
                    context.emit('add-data-set', ev, item);
                },
                checkCard(id) {
                    context.emit('check-card', id);
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
    .page{
        :deep(.card-dialog) {
            .el-dialog__body {
                overflow: hidden;
                display: flex;
                justify-content: center;
                padding: 20px 20px 40px;
            }
        }
    }
    .el-icon-folder-add{
        cursor: pointer;
        font-size: 16px;
        color: $color-link-base;
    }
    .p-name {
        color: $color-link-base;
        cursor: pointer;
        display: flex;
        align-items: center;
        i {padding-right: 5px;}
    }
    .horiz-ball{
        position: fixed;
        z-index: 11;
        transition: 0.7s all linear;
    }
    .ball-icon{
        transition: 0.7s all cubic-bezier(0.49, -0.29, 0.75, 0.41);
    }
</style>
