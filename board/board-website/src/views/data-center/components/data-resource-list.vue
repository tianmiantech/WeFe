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

        <el-table-column label="名称 / Id" min-width="160">
            <template v-slot="scope">
                <router-link :to="{ name: 'data-view', query: { id: scope.row.id, type: dataResourceTypeMap[scope.row.data_resource_type].type }}">
                    {{ scope.row.name }}
                </router-link>
                <el-tag v-if="scope.row.data_resource_type === 'BloomFilter'" class="ml5" size="mini">
                    bf
                </el-tag>
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
            align="center"
            width="130"
        >
            <template v-slot="scope">
                {{ dataResourceTypeMap[scope.row.data_resource_type].label }}
            </template>
        </el-table-column>
        <el-table-column
            v-if="search.dataResourceType === 'ImageDataSet'"
            label="任务类型"
            align="center"
            width="100"
        >
            <template v-slot="scope">
                <p v-if="scope.row.data_resource_type === 'ImageDataSet'">
                    {{scope.row.for_job_type === 'detection' ? '目标检测' : '图像分类'}}
                </p>
                <p v-else>-</p>
            </template>
        </el-table-column>
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
                <p v-else-if="scope.row.data_resource_type === 'BloomFilter'">
                    样本量：{{ scope.row.total_data_count }}
                    <br>
                    主键组合方式: {{ scope.row.hash_function }}
                </p>
                <p v-else>
                    特征量：{{ scope.row.feature_count }}
                    <br>
                    样本量：{{ scope.row.total_data_count }}
                    <br>
                    <span v-if="scope.row.data_resource_type === 'TableDataSet'">
                        正例样本数量：{{ scope.row.y_positive_sample_count || 0 }}
                        <br>
                        正例样本比例：{{(scope.row.y_positive_sample_ratio * 100).toFixed(1)}}%
                        <br>
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
        <el-table-column
            label="操作"
            fixed="right"
            align="center"
            min-width="120"
        >
            <template v-slot="scope">
                <router-link
                    :to="{
                        name: 'data-update',
                        query: { id: scope.row.id, type: dataResourceTypeMap[scope.row.data_resource_type].type }
                    }"
                >
                    <el-tooltip
                        class="item"
                        effect="light"
                        content="编辑"
                        placement="top"
                    >
                        <el-icon class="el-icon-edit-outline" style="font-size:16px;">
                            <elicon-edit />
                        </el-icon>
                    </el-tooltip>
                </router-link>
                <el-tooltip
                    class="item"
                    effect="light"
                    content="删除"
                    placement="top"
                >
                    <el-icon class="el-icon-delete ml10 mr10" style="color: red;cursor:pointer;font-size:16px;" @click="deleteData(scope.row)">
                        <elicon-delete />
                    </el-icon>
                </el-tooltip>
                <router-link
                    v-if="scope.row.data_resource_type === 'ImageDataSet'"
                    :to="{
                        name: 'data-check-label',
                        query: { id: scope.row.id, type: 'img' }
                    }"
                >
                    <el-tooltip
                        class="item"
                        effect="light"
                        content="查看与标注"
                        placement="top"
                    >
                        <el-icon>
                            <i
                                title="查看与标注"
                                class="iconfont icon-mark"
                            />
                        </el-icon>

                    </el-tooltip>
                </router-link>
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
        data() {
            return {
                getListApi:          '/data_resource/query',
                defaultSearch:       false,
                watchRoute:          false,
                dataResourceTypeMap: {
                    BloomFilter: {
                        label: '布隆过滤器',
                        type:  'BloomFilter',
                    },
                    ImageDataSet: {
                        label: 'ImageDataSet',
                        type:  'img',
                    },
                    TableDataSet: {
                        label: 'TableDataSet',
                        type:  'csv',
                    },
                },
                requestMethod: 'post',
            };
        },
        methods: {
            getDataList(opt) {
                this.search = this.searchField;
                this.pagination.page_index = +this.$route.query.page_index || 1;
                this.pagination.page_size = +this.$route.query.page_size || 20;
                this.getList(opt);
            },
            async deleteData(row) {
                let message = '此操作将永久删除该条目, 是否继续?';

                const res = await this.$http.get({
                    url:    '/data_resource/usage_in_project_list',
                    params: {
                        dataResourceId: row.id,
                    },
                });

                if(res.code === 0) {
                    if(res.data && res.data.length) {
                        const list = res.data.map(row => {
                            const path = this.$router.resolve({
                                name:  'project-detail',
                                query: {
                                    project_id: row.project_id,
                                },
                            });

                            return `<a href="${path.href}" target="_blank">${row.name}</a>`;
                        });

                        message = `该数据资源在 ${list.join(', ')}, 共 ${res.data.length} 个项目中被使用，您确定要删除吗？`;
                    } else if (row.usage_count_in_project > 0) {
                        message = `该数据资源在 ${row.usage_count_in_project} 个项目中被使用，您确定要删除吗？`;
                    }

                    this.$confirm('警告', {
                        title:                    '警告',
                        type:                     'warning',
                        dangerouslyUseHTMLString: true,
                        message,
                    }).then(async () => {
                        const map = {
                            TableDataSet: '/table_data_set/delete',
                            ImageDataSet: '/image_data_set/delete',
                            BloomFilter:  '/bloom_filter/delete',
                        };
                        const { code } = await this.$http.post({
                            url:  map[row.data_resource_type],
                            data: {
                                id: row.id,
                            },
                        });

                        if (code === 0) {
                            this.$message.success('删除成功!');
                            this.getList({ resetPagination: true });
                        }
                    });
                }
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
</style>
