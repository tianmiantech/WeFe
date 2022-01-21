<template>
    <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
    >
        <template #empty>
            <div class="empty f14">
                您当前没有数据资源，请前往
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
            min-width="160"
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
                <router-link :to="{ name: 'union-data-view', query: { id: scope.row.data_resource_id, type: dataResourceTypeMap[scope.row.data_resource_type], data_resource_type: scope.row.data_resource_type }}">
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
                <span v-if="scope.row.public_level === 'Public'">
                    所有成员可见
                </span>
                <span v-else-if="scope.row.public_level === 'OnlyMyself'">
                    仅自己可见
                </span>
                <span v-else>
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
            label="数据信息"
            width="160"
        >
            <template v-slot="scope">
                <p v-if="scope.row.data_resource_type === 'ImageDataSet'">
                    样本量/已标注：{{scope.row.total_data_count}}/{{scope.row.labeled_count}}
                    <br>
                    标注进度：{{ (scope.row.labeled_count / scope.row.total_data_count).toFixed(2) * 100 }}%
                    <br>
                    样本分类：{{scope.row.for_job_type === 'detection' ? '目标检测' : '图像分类'}}
                </p>
                <p v-else>
                    特征量：{{ scope.row.feature_count }}
                    <br>
                    样本量：{{ scope.row.total_data_count }}
                    <br>
                    <span v-if="scope.row.data_resource_type === 'TableDataSet'">
                        <el-tag v-if="scope.row.contains_y" type="success" class="mr5">包含Y</el-tag>
                        <el-tag v-else type="danger" class="mr5">不包含Y</el-tag>
                    </span>
                </p>
            </template>
        </el-table-column>
        <el-table-column
            label="参与项目数"
            prop="usage_count_in_project"
            width="100"
            align="center"
        />
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
            @current-change="currentPageChange"
            @size-change="pageSizeChange"
        />
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
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
        data() {
            return {
                getListApi:          '/union/data_resource/query',
                defaultSearch:       false,
                watchRoute:          true,
                turnPageRoute:       false,
                requestMethod:       'post',
                dataResourceTypeMap: {
                    BloomFilter:  'BloomFilter',
                    ImageDataSet: 'img',
                    TableDataSet: 'csv',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            getDataList(opt) {
                this.search = this.searchField;
                this.pagination.page_index = +this.$route.query.page_index || 1;
                this.pagination.page_size = +this.$route.query.page_size || 20;
                this.getList(opt);
            },
            addDataSet(ev, item) {
                this.$emit('add-data-set', ev, item);
            },
            checkCard(id) {
                this.$emit('check-card', id);
            },
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
