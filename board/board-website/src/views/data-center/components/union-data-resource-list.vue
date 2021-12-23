<template>
    <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
    >
        <template #empty>
            <div class="empty f14">
                您当前没有数据集，请前往
                <router-link
                    :to="{ path: 'data-add' }"
                    class="ml10"
                >
                    添加资源
                    <el-icon class="f12">
                        <elicon-top-right />
                    </el-icon>
                </router-link>
            </div>
        </template>
        <el-table-column label="添加" width="60" v-slot="scope">
            <el-icon title="快捷创建项目" class="el-icon-folder-add" @click="addDataSet($event, scope.row)">
                <elicon-folder-add />
            </el-icon>
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
        <el-table-column label="名称 / Id" min-width="160">
            <template v-slot="scope">
                <router-link :to="{ name: 'data-view', query: { id: scope.row.data_resource_id, type: scope.row.data_resource_type === 'ImageDataSet' ? 'img' : 'csv' }}">
                    {{ scope.row.name }}
                </router-link>
                <br>
                <span class="p-id">{{ scope.row.data_resource_id }}</span>
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
        <el-table-column label="可见性" align="center">
            <template v-slot="scope">
                <span
                    v-if="scope.row.public_level === 'Public'"
                >
                    所有成员可见
                </span>
                <span
                    v-else-if="scope.row.public_level === 'OnlyMyself'"
                >
                    仅自己可见
                </span>
                <span
                    v-else
                >
                    指定成员可见
                </span>
            </template>
        </el-table-column>
        <el-table-column
            label="资源类型"
            prop="data_resource_type"
            width="130"
            align="center"
        />
        <el-table-column
            label="任务类型"
            width="100"
            v-if="search.dataResourceType === 'ImageDataSet'"
            align="center"
        >
            <template v-slot="scope">
                <p v-if="scope.row.data_resource_type === 'ImageDataSet'">
                    {{scope.row.for_job_type === 'detection' ? '目标检测' : '图像分类'}}
                </p>
                <p v-else>-</p>
            </template>
        </el-table-column>
        <el-table-column
            label="数据量"
            width="140"
        >
            <template v-slot="scope">
                <p v-if="scope.row.data_resource_type === 'TableDataSet'">
                    特征量：{{ scope.row.feature_count }}
                    <br>
                    样本量：{{ scope.row.total_data_count }}
                </p>
                <p v-else>{{scope.row.total_data_count}}</p>
            </template>
        </el-table-column>
        <el-table-column
            label="参与项目数"
            prop="usage_count_in_project"
            width="100"
            align="center"
        />
        <el-table-column
            label="包含Y"
            width="100"
            align="center"
            v-if="search.dataResourceType !== 'ImageDataSet'"
        >
            <template v-slot="scope">
                <p v-if="scope.row.data_resource_type === 'TableDataSet'">
                    <el-icon v-if="scope.row.contains_y" class="el-icon-check" style="color: #67C23A">
                        <elicon-check />
                    </el-icon>
                    <el-icon v-else class="el-icon-close">
                        <elicon-close />
                    </el-icon>
                </p>
                <p v-else>-</p>
            </template>
        </el-table-column>
        <el-table-column
            label="上传者"
            prop="creator_nickname"
            min-width="160"
            align="center"
        >
            <template v-slot="scope">
                {{ scope.row.creator_nickname }}
                <br>
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
            @current-change="methods.currentPageChange"
            @size-change="pageSizeChange"
        />
    </div>
</template>

<script>
    import table from '@src/mixins/table';
    import { reactive, getCurrentInstance } from 'vue';
    import { useRoute, useRouter } from 'vue-router';

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
            const route = useRoute();
            const router = useRouter();
            const vData = reactive({
                getListApi:    '/union/data_resource/query',
                defaultSearch: false,
                watchRoute:    false,
            });
            const methods = {
                getDataList(opt) {
                    ctx.search = props.searchField;
                    ctx.getListApi = vData.getListApi;
                    ctx.pagination.page_index =+route.query.page_index || 1;
                    ctx.pagination.page_size =+route.query.page_size || 20;
                    ctx.getList(opt);
                },
                addDataSet(ev, item) {
                    context.emit('add-data-set', ev, item);
                },
                checkCard(id) {
                    context.emit('check-card', id);
                },
                currentPageChange (val) {
                    if (ctx.watchRoute) {
                        router.push({
                            query: {
                                ...ctx.search,
                                page_index: val,
                            },
                        });
                    } else {
                        ctx.pagination.page_index = val;
                        ctx.getList();
                    }
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
</style>
