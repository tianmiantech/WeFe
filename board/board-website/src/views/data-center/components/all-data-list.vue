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
                <router-link :to="{ name: 'data-view', query: { id: scope.row.id }}">
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
        <el-table-column label="可见性">
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
            label="数据量"
            prop="row_count"
            width="140"
        >
            <template v-slot="scope">
                特征量：{{ scope.row.feature_count }}
                <br>
                样本量：{{ scope.row.row_count }}
                <br>
                正例样本数量：{{ scope.row.y_positive_sample_count }}
                <br>
                正例样本比例：{{(scope.row.y_positive_sample_ratio * 100).toFixed(1)}}%
            </template>
        </el-table-column>
        <el-table-column
            label="参与项目数"
            prop="usage_count_in_project"
            width="100"
        />
        <el-table-column
            label="包含Y"
            width="100"
        >
            <template v-slot="scope">
                <el-icon v-if="scope.row.contains_y" class="board-icon-check">
                    <elicon-check />
                </el-icon>
                <el-icon v-else class="board-icon-close">
                    <elicon-close />
                </el-icon>
            </template>
        </el-table-column>
        <el-table-column
            label="上传者"
            prop="creator_nickname"
            min-width="160"
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
            min-width="160"
        >
            <template v-slot="scope">
                <router-link
                    :to="{
                        name: 'data-update',
                        query: { id: scope.row.id }
                    }"
                >
                    <el-button type="primary">
                        编辑
                    </el-button>
                </router-link>
                <el-button
                    type="danger"
                    class="ml10"
                    @click="deleteData(scope.row)"
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
                getListApi:    '/table_data_set/query',
                defaultSearch: false,
                watchRoute:    false,
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
                        type:                     'warning',
                        dangerouslyUseHTMLString: true,
                        message,
                    }).then(async () => {
                        const { code } = await this.$http.post({
                            url:  '/table_data_set/delete',
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
