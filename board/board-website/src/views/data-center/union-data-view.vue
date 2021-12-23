<template>
    <el-card v-loading="loading">
        <h4 class="mb10">数据集简介</h4>
        <h3 class="mb10"><strong>{{ dataInfo.name }}</strong></h3>
        <el-descriptions class="dataset-desc" :column="2">
            <template #extra>
                <p class="data-set-meta">
                    <strong class="strong">{{ dataInfo.creator_nickname }}</strong> 上传于 {{ dateFormat(dataInfo.created_time) }}，在 <strong class="strong">{{ dataInfo.usage_count_in_project > 0 ? dataInfo.usage_count_in_project : 0 }}</strong> 个合作项目中，
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
            <template v-if="addDataType === 'csv'">
                <template v-if="dataInfo.contains_y">
                    <el-descriptions-item label="正例样本数量：">
                        {{ dataInfo.y_positive_example_count }}
                    </el-descriptions-item>
                    <el-descriptions-item label="正例样本比例：">
                        {{ (dataInfo.y_positive_example_ratio * 100).toFixed(1) }}%
                    </el-descriptions-item>
                </template>
                <el-descriptions-item label="样本量/特征量：">
                    {{ dataInfo.total_data_count }} / {{ dataInfo.feature_count }}
                </el-descriptions-item>
            </template>
            <template v-if="addDataType === 'img'">
                <el-descriptions-item label="样本分类：">
                    {{ dataInfo.for_job_type === 'classify' ? '图像分类' : dataInfo.for_job_type === 'detection' ? '目标检测' : '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="标注状态：">
                    {{ dataInfo.label_completed ? '已完成' : '标注中' }}
                </el-descriptions-item>
                <el-descriptions-item label="样本数量：">
                    {{ dataInfo.total_data_count }}
                </el-descriptions-item>
            </template>
        </el-descriptions>

        <template v-if="addDataType === 'csv'">
            <el-divider></el-divider>
            <h3 class="mb10">数据信息</h3>
            <div class="dataset-desc">
                <EmptyData v-if="data_list.length === 0" />
                <DataSetPreview v-else ref="DataSetFeatures" />
            </div>
        </template>
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
                loading:     false,
                dataInfo:    {},
                data_list:   [],
                addDataType: 'csv',
            };
        },
        created() {
            this.addDataType = this.$route.query.type || 'csv';
            this.getData();
        },
        methods: {
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    'union/data_resource/detail',
                    params: {
                        dataResourceId:   this.$route.query.id,
                        dataResourceType: this.$route.query.data_resource_type,
                    },
                });

                if(code === 0 && data) {
                    this.dataInfo = data;
                    if (this.addDataType === 'csv') {
                        if (data.feature_name_list && data.feature_name_list.length) {
                            this.data_list = data.feature_name_list.split(',').map((name, index) => {
                                return {
                                    序号:   String(index),
                                    特征名称: name,
                                };
                            });
                    
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
                    }
                }
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-tab-pane{min-height: 500px;}
    .el-tag {margin-right: 10px;}
    .strong{font-weight: bold;}
    .data-set-meta{
        font-family: Menlo,Monaco,Consolas,Courier,monospace;
        font-size: 14px;
        margin-top: 15px;
    }
    .dataset-desc{max-width: 700px;}
    .el-descriptions{
        :deep(.el-descriptions__header) {display: block;}
        :deep(.is-bordered-label){width: 100px;}
    }
</style>
