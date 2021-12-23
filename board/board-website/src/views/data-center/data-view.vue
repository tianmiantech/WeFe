<template>
    <el-card v-loading="loading">
        <el-divider content-position="left">
            数据集简介
        </el-divider>
        <h3 class="mb10"><strong>{{ dataInfo.name }}</strong></h3>
        <el-descriptions :column="2">
            <template #extra>
                <p class="data-set-meta">
                    <strong class="strong">{{ dataInfo.creator_nickname }}</strong> 上传于 {{ dateFormat(dataInfo.created_time) }}，在
                    <el-popover
                        v-if="dataInfo.usage_count_in_project"
                        trigger="hover"
                    >
                        <template #reference>
                            <el-link class="strong" type="primary">{{ dataInfo.usage_count_in_project }}</el-link>
                        </template>
                        <p class="f12">参与的合作:</p>
                        <p v-for="item in projects" :key="item.project_id">
                            <router-link :to="{name: 'project-detail', query: { project_id: item.project_id }}">
                                <el-link type="primary" :underline="false">
                                    {{ item.name }}
                                    <el-icon>
                                        <elicon-right />
                                    </el-icon>
                                </el-link>
                            </router-link>
                        </p>
                    </el-popover>
                    <strong v-else class="strong">0</strong> 个合作项目中，
                    参与了 <strong class="strong">{{ dataInfo.usage_count_in_job > 0 ? dataInfo.usage_count_in_job : 0 }}</strong> 次任务。
                </p>
            </template>
            <el-descriptions-item v-if="dataInfo.description" label="描述：">
                {{ dataInfo.description }}
            </el-descriptions-item>
            <el-descriptions-item label="关键字：">
                <span v-if="dataInfo.tags">
                    <template v-for="(tag, index) in dataInfo.tags.split(',')">
                        <el-tag
                            v-if="tag"
                            :key="index"
                        >
                            {{ tag }}
                        </el-tag>
                    </template>
                </span>
            </el-descriptions-item>
            <template v-if="addDataType === 'csv' && dataInfo.contains_y">
                <el-descriptions-item label="正例样本数量：">
                    {{ dataInfo.y_positive_sample_count }}
                </el-descriptions-item>
                <el-descriptions-item label="正例样本比例：">
                    {{ (dataInfo.y_positive_sample_ratio * 100).toFixed(1) }}%
                </el-descriptions-item>
            </template>
            <el-descriptions-item v-if="addDataType === 'csv'" label="样本量/特征量：">
                {{ dataInfo.total_data_count }} / {{ dataInfo.feature_count }}
            </el-descriptions-item>
            <template v-if="addDataType === 'img'">
                <el-descriptions-item label="数据总量：">
                    {{ dataInfo.total_data_count }}
                </el-descriptions-item>
                <el-descriptions-item v-if="dataInfo.label_list" label="标签个数：">
                    {{ dataInfo.label_list.split(',').length }}
                </el-descriptions-item>
                <el-descriptions-item v-if="dataInfo.label_list" label="标签分布：">
                    <template v-for="item in dataInfo.$label_list" :key="item.name">
                        {{item.name}} ( {{item.count}} )
                    </template>
                </el-descriptions-item>
                <el-descriptions-item label="标注状态：">
                    {{ completedStatus(dataInfo.label_completed) }}
                </el-descriptions-item>
                <el-descriptions-item label="样本分类：">
                    {{ dataInfo.for_job_type === 'detection' ? '目标检测' : dataInfo.for_job_type === 'classify' ? '图像分类' : '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="数据大小：">
                    {{ (dataInfo.files_size / 1024 /1024).toFixed(2) }}M
                </el-descriptions-item>
                <el-descriptions-item label="标注进度：">
                    {{dataInfo.labeled_count}} ({{ (dataInfo.labeled_count / dataInfo.total_data_count).toFixed(2) * 100 }}%)
                    <el-button type="primary" style="margin-left: 20px;" @click="jumpToLabel">
                        去标注 <i class="el-icon-right"></i>
                    </el-button>
                    <el-button hidden type="primary">
                        导入标注数据包
                    </el-button>
                </el-descriptions-item>
            </template>
        </el-descriptions>

        <el-divider content-position="left">
            数据集信息
        </el-divider>

        <preview-image-list v-if="addDataType === 'img'" ref="PreviewImageListRef" />

        <el-tabs
            v-if="addDataType === 'csv'"
            class="mt20"
            type="border-card"
            @tab-click="tabChange"
        >
            <el-tab-pane label="特征列表">
                <div class="el-descriptions">
                    <EmptyData v-if="data_list.length === 0" />
                    <DataSetPreview v-else ref="DataSetFeatures" />
                </div>
            </el-tab-pane>

            <el-tab-pane name="preview" label="数据预览">
                <h4 v-if="!dataInfo.source_type" class="mb10">主键已被 hash</h4>
                <DataSetPreview ref="DataSetPreview" />
            </el-tab-pane>
        </el-tabs>
    </el-card>
</template>

<script>
    import DataSetPreview from '@comp/views/data_set-preview';
    import PreviewImageList from './components/preview-image-list.vue';

    export default {
        components: {
            DataSetPreview,
            PreviewImageList,
        },
        data() {
            return {
                loading:     false,
                previewed:   false,
                id:          this.$route.query.id,
                data_list:   [],
                dataInfo:    {},
                addDataType: 'csv',
                projects:    [],
                search:      {
                    page_index: 1,
                    page_size:  30,
                    label:      '',
                    labeled:    '',
                    total:      1,
                },
            };
        },
        computed: {
            completedStatus(val) {
                return function(val) {
                    return val ? '标注完成' : '进行中';
                };
            },
        },
        created() {
            this.addDataType = this.$route.query.type || 'csv';
            this.getData();
            this.getRelativeProjects();
        },
        mounted() {
            if (this.addDataType === 'img') {
                this.$refs['PreviewImageListRef'].methods.getSampleList(this.id);
            }
        },
        methods: {
            async getRelativeProjects() {
                const { code, data } = await this.$http.get({
                    url:    '/data_resource/usage_in_project_list',
                    params: {
                        dataResourceId: this.id,
                    },
                });

                if(code === 0 && data && data.length) {
                    this.projects = data;
                }
            },
            async loadDataSetColumnList(){
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    '/table_data_set/column/list',
                    params: {
                        data_set_id: this.id,
                    },
                });

                if (code === 0) {
                    this.data_list = data.list;

                    this.$nextTick(_ => {
                        let { length } = data.list;

                        if(length >= 15) length = 15;

                        const featuresRef = this.$refs['DataSetFeatures'];

                        if(featuresRef) {
                            featuresRef.resize(length);
                            featuresRef.loading = true;
                            featuresRef.table_data.header = ['特征名称', '数据类型', '注释'];
                            featuresRef.table_data.rows = data.list.map(row => {
                                let name = row.name;

                                if(row.name === this.dataInfo.primary_key_column) {
                                    name = `${row.name}（主键）`;
                                }

                                return {
                                    ...row,
                                    特征名称: name,
                                    数据类型: row.data_type,
                                    注释:   row.comment,
                                };
                            });
                            featuresRef.loading = false;
                        }
                    });
                }
                this.loading = false;
            },

            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    this.addDataType === 'csv' ? '/table_data_set/detail' : '/image_data_set/detail',
                    params: {
                        id: this.id,
                    },
                });

                if(code === 0) {
                    if (data) {
                        if (this.addDataType === 'img') {
                            const labelList = [];

                            data.label_list.split(',').forEach(item => {
                                labelList.push({
                                    name:  item,
                                    count: 0,
                                });
                                this.getLabelListDistributed(item);
                            });
                            data.$label_list = labelList;
                        }
                        this.dataInfo = data;
                    }

                }
                this.loading = false;
                if (this.addDataType === 'csv') this.loadDataSetColumnList();
            },
            async getLabelListDistributed(label) {
                const params = {
                    page_index:  this.search.page_index - 1,
                    page_size:   this.search.page_size,
                    label,
                    data_set_id: this.id,
                    labeled:     this.search.labeled,
                };
                const { code, data } = await this.$http.post({
                    url:  '/image_data_set_sample/query',
                    data: params,
                });

                if(code === 0) {
                    if (data && data.list) {
                        this.dataInfo.$label_list.forEach(item => {
                            if (item.name === label) {
                                item.count = data.total;
                            }
                        });
                    }
                }
            },

            tabChange(ref) {
                if(ref.paneName === 'preview' && !this.previewed) {
                    this.$refs['DataSetPreview'].loadData(this.id);
                    this.previewed = true;
                }
            },

            jumpToLabel() {
                this.$router.push({
                    name:  'data-check-label',
                    query: { id: this.id },
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
@mixin flex_box {
    display: flex;
}
.el-tab-pane{min-height: 500px;}
.el-tag {margin-right: 10px;}
.strong{font-weight: bold;}
.data-set-meta{
    font-family: Menlo,Monaco,Consolas,Courier,monospace;
    font-size: 14px;
    margin-top: 15px;
}
.el-descriptions{
    max-width: 700px;
    :deep(.el-descriptions__header) {display: block;}
    :deep(.is-bordered-label){width: 30px;}
}
</style>
