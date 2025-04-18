<template>
    <el-card v-loading="loading">
        <h4 class="mb10">资源简介</h4>
        <h3 class="mb10"><strong>{{ dataInfo.name }}</strong></h3>
        <el-descriptions class="dataset-desc" :column="2">
            <template #extra>
                <p class="data-set-meta">
                    <strong class="strong">{{ dataInfo.creator_realname }}</strong> 上传于 {{ dateFormat(dataInfo.created_time) }}，在 <strong class="strong">{{ dataInfo.usage_count_in_project > 0 ? dataInfo.usage_count_in_project : 0 }}</strong> 个合作项目中，
                    参与了 <strong class="strong">{{ dataInfo.usage_count_in_job > 0 ? dataInfo.usage_count_in_job : 0 }}</strong> 次任务。
                </p>
            </template>
            <el-descriptions-item v-if="dataInfo.description" label="描述：">
                {{ dataInfo.description }}
            </el-descriptions-item>
            <el-descriptions-item label="标签">
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
            <template v-if="dataInfo.contains_y">
                <el-descriptions-item label="正例样本数量：">
                    {{ dataInfo.y_positive_example_count }}
                </el-descriptions-item>
                <el-descriptions-item label="正例样本比例：">
                    {{ (dataInfo.y_positive_example_ratio * 100).toFixed(1) }}%
                </el-descriptions-item>
            </template>
            <el-descriptions-item v-if="dataInfo.data_resource_type === 'TableDataSet'" label="样本量/特征量：">
                {{ dataInfo.total_data_count }} / {{ dataInfo.extra_data.feature_count }}
            </el-descriptions-item>
            <template v-if="dataInfo.data_resource_type === 'ImageDataSet'">
                <el-descriptions-item label="样本量/已标注：">
                    {{ dataInfo.total_data_count }}/{{dataInfo.extra_data.labeled_count}}
                </el-descriptions-item>
                <el-descriptions-item v-if="dataInfo.label_list" label="标签个数：">
                    {{ dataInfo.extra_data.label_list.split(',').length }}
                </el-descriptions-item>
                <el-descriptions-item label="标注状态：">
                    {{ completedStatus(dataInfo.extra_data.label_completed) }}
                </el-descriptions-item>
                <el-descriptions-item label="样本分类：">
                    {{ dataInfo.extra_data.for_job_type === 'detection' ? '目标检测' : dataInfo.extra_data.for_job_type === 'classify' ? '图像分类' : '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="数据大小：">
                    {{ dataInfo.extra_data.files_size ? (dataInfo.extra_data.files_size / 1024 /1024).toFixed(2) : 0 }}M
                </el-descriptions-item>
                <el-descriptions-item label="标注进度：">
                    {{dataInfo.extra_data.labeled_count}} ({{ ((dataInfo.extra_data.labeled_count / dataInfo.total_data_count)*100).toFixed(2) }}%)
                </el-descriptions-item>
            </template>
        </el-descriptions>

        <el-divider></el-divider>

        <h3 class="mb10">数据信息</h3>
        <div class="dataset-desc">
            <EmptyData v-if="data_list.length === 0" />
            <DataSetPreview v-else ref="DataSetFeatures" />
        </div>
    </el-card>
</template>

<script>
    import DataSetPreview from '@comp/views/data_set-preview';

    export default {
        components: {
            DataSetPreview,
        },
        data() {
            return {
                loading:   false,
                dataInfo:  {},
                data_list: [],
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
            this.getData();
        },
        methods: {
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    '/data_resource/detail',
                    params: {
                        dataResourceId:   this.$route.query.dataResourceId,
                        dataResourceType: this.$route.query.dataResourceType,
                    },
                });

                if(code === 0 && data) {
                    this.dataInfo = data;

                    if (data.data_resource_type === 'TableDataSet') {
                        this.data_list = data.extra_data.feature_name_list.split(',').map((name, index) => {
                            return {
                                序号:   String(index),
                                特征名称: name,
                            };
                        });
                    }
                    this.$nextTick(_ => {
                        const featuresRef = this.$refs['DataSetFeatures'];

                        let { length } = this.data_list;

                        if(length >= 15) length = 15;

                        featuresRef.resize(length);
                        featuresRef.loading = true;
                        featuresRef.table_data.header = ['序号', '特征名称'];
                        featuresRef.table_data.rows = this.data_list;
                        featuresRef.loading = false;
                    });
                }
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .manager-tab-pane{min-height: 500px;}
    .manager-tag {margin-right: 10px;}
    .strong{font-weight: bold;}
    .data-set-meta{
        font-family: Menlo,Monaco,Consolas,Courier,monospace;
        font-size: 14px;
        margin-top: 15px;
    }
    .dataset-desc{max-width: 700px;}
    .manager-descriptions{
        :deep(.manager-descriptions__header) {display: block;}
        :deep(.is-bordered-label){width: 100px;}
    }
</style>
