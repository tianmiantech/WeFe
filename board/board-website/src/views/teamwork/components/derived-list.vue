<template>
    <el-card
        name="衍生数据资源"
        class="nav-title mb30"
        shadow="never"
        :show="project_type !== 'DeepLearning'"
        :idx="sortIndex"
        style="background: white"
    >
        <template #header>
            <div class="mb10 flex-row">
                <h3 class="card-title f19">
                    <el-icon :class="['board-icon-document-copy', 'mr10', 'ml10']" style="font-size: xx-large; top:8px; right: -3px; color: dodgerblue"><elicon-document-copy />
                    </el-icon>
                    衍生数据资源
                </h3>
                <div v-if="form.is_project_admin" class="right-sort-area">
                    <el-icon v-if="sortIndex !== 0" :sidx="sortIndex" :midx="maxIndex" :class="['board-icon-top', {'mr10': maxIndex === sortIndex}]" @click="moveUp" title="向上" style="color: lightgray"><elicon-top /></el-icon>
                    <el-icon v-if="maxIndex !== sortIndex" :class="['board-icon-bottom', 'mr10', 'ml10']" @click="moveDown" title="向下" style="color: lightgray"><elicon-bottom /></el-icon>
                    <el-icon v-if="sortIndex !== 0" :sidx="sortIndex" :midx="maxIndex" :class="['board-icon-caret-top', {'mr10': maxIndex === sortIndex}]" @click="toTop" title="置顶" style="color: lightgray"><elicon-caret-top /></el-icon>
                    <el-icon v-if="maxIndex !== sortIndex" :class="['board-icon-caret-bottom', 'mr10', 'ml10']" @click="toBottom" title="置底" style="color: lightgray"><elicon-caret-bottom /></el-icon>
                </div>
            </div>
        </template>
        <el-form inline @submit.prevent>
            <el-form-item label="来源：">
                <el-select
                    v-model="derived.name"
                    clearable
                >
                    <el-option
                        v-for="item in derived.types"
                        :key="item.value"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="来源流程 id：">
                <el-input
                    v-model="derived.sourceFlowId"
                    placeholder="选填"
                />
            </el-form-item>
            <el-form-item label="任务 id：">
                <el-input
                    v-model="derived.sourceJobId"
                    placeholder="选填"
                />
            </el-form-item>
            <el-button
                class="mb20"
                type="primary"
                @click="searchDeriveData"
            >
                搜索
            </el-button>
        </el-form>
        <el-table
            :data="derived.list"
            max-height="520px"
            stripe
            border
        >
            <el-table-column type="index" />
            <el-table-column
                label="数据资源名称"
                min-width="150"
            >
                <template v-slot="scope">
                    <router-link :to="{ name: 'data-view', query: { id: scope.row.data_set_id } }">
                        {{ scope.row.data_resource ? scope.row.data_resource.name : '' }}
                        <el-tag v-if="scope.row.data_resource ? scope.row.data_resource.contains_y : false" class="ml5">
                            Y
                        </el-tag>
                    </router-link>
                    <br>
                    <span class="p-id">{{ scope.row.data_set_id }}</span>
                </template>
            </el-table-column>
            <el-table-column
                label="数据资源来源"
                width="100"
            >
                <template v-slot="scope">
                    {{ scope.row.data_resource ? scope.row.data_resource.derived_from_cn : '' }}
                </template>
            </el-table-column>
            <el-table-column
                label="成员列表"
                min-width="130"
                prop="name"
            >
                <template v-slot="scope">
                    <template
                        v-for="(item) in scope.row.members"
                        :key="`${item.member_id}-${item.member_role}`"
                    >
                        <el-tag
                            type="info"
                            class="mr10"
                            effect="plain"
                        >
                            {{ item.member_name }}
                        </el-tag>
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="数据量"
                width="100"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.data_resource">
                        特征量：{{ scope.row.data_resource.feature_count }}
                        <br>
                        样本量：{{ scope.row.data_resource.total_data_count }}
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="使用次数"
                width="80"
            >
                <template v-slot="scope">
                    {{ scope.row.data_resource ? scope.row.data_resource.usage_count_in_job : '' }}
                </template>
            </el-table-column>
            <el-table-column
                label="创建时间"
                min-width="140"
            >
                <template v-slot="scope">
                    {{ scope.row.data_resource ? dateFormat(scope.row.data_resource.created_time) : '' }}
                </template>
            </el-table-column>
            <el-table-column label="查看任务">
                <template v-slot="scope">
                    <router-link v-if="scope.row.data_resource" :to="{ name: 'project-job-detail', query: { job_id: scope.row.data_resource.derived_from_job_id, flow_id: scope.row.data_resource.derived_from_flow_id, project_id, member_role: scope.row.member_role }}">
                        查看任务
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.is_project_admin"
                label="操作"
                width="100"
            >
                <template v-slot="scope">
                    <el-button
                        type="danger"
                        @click="removeDataSet(scope.row)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <div
            v-if="derived.total"
            class="mt20 text-r"
        >
            <el-pagination
                :total="derived.total"
                :page-size="derived.page_size"
                :page-sizes="[10, 20, 30, 40, 50]"
                :current-page="derived.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="derivedPageChange"
                @size-change="derivedPageSizeChange"
            />
        </div>
    </el-card>
</template>

<script>
    import { template } from 'lodash';

    export default {
        props: {
            projectType: String,
            sortIndex:   Number,
            maxIndex:    Number,
            form:        Object,
        },
        emits: ['move-up', 'move-down', 'to-top', 'to-bottom'],
        data() {
            return {
                derived: {
                    name:         '',
                    sourceJobId:  '',
                    sourceFlowId: '',
                    types:        [{
                        label: '样本对齐',
                        value: 'Intersection',
                    }, {
                        label: '分箱',
                        value: 'Binning',
                    }, {
                        label: '特征筛选',
                        value: 'FeatureSelection',
                    }, {
                        label: '特征标准化',
                        value: 'FeatureStandardized',
                    }, {
                        label: '分箱并编码',
                        value: 'HorzFeatureBinning',
                    }, {
                        label: '缺失值填充',
                        value: 'FillMissingValue',
                    }, {
                        label: '混合分箱',
                        value: 'MixBinning',
                    }],
                    list:       [],
                    total:      0,
                    page_index: 1,
                    page_size:  10,
                },
                project_id: '',
            };
        },
        created() {
            this.project_id = this.$route.query.project_id;
            this.project_type = this.$route.query.project_type;
            this.getDeriveData();
        },
        methods: {
            async getDeriveData($event) {
                const params = {
                    url:    '/project/derived_data_set/query',
                    params: {
                        sourceType:         this.derived.name,
                        project_id:         this.project_id,
                        sourceJobId:        this.derived.sourceJobId,
                        page_index:         this.derived.page_index - 1,
                        page_size:          this.derived.page_size,
                        data_resource_type: this.projectType === 'DeepLearning' ? 'ImageDataSet' : this.projectType === 'MachineLearning' ? 'TableDataSet' : '',
                    },
                };

                if ($event) {
                    params.btnState = {
                        target: $event,
                    };
                }
                const { code, data } = await this.$http.get(params);

                if (code === 0) {
                    this.derived.list = data.list;
                    this.derived.total = data.total;
                }
            },
            searchDeriveData() {
                this.derived.page_index = 1;
                this.getDeriveData();
            },

            derivedPageChange(val) {
                this.derived.page_index = val;
                this.getDeriveData();
            },

            derivedPageSizeChange(val) {
                console.log('pageSize change');
                this.derived.page_size = val;
                this.getDeriveData();
            },

            removeDataSet(row) {
                this.$confirm('删除后将不再使用当前数据样本', '警告', {
                    type: 'warning',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await this.$http.post({
                                url:  '/project/data_resource/remove',
                                data: {
                                    project_id:  this.project_id,
                                    data_set_id: row.data_set_id,
                                    member_role: row.member_role,
                                },
                            });

                            if(code === 0) {
                                this.getDeriveData();
                                this.$message.success('操作成功!');
                            }
                        }
                    });
            },
            
            moveUp() {
                this.$emit('move-up', this.sortIndex);
            },
            moveDown() {
                this.$emit('move-down', this.sortIndex);
            },
            toTop() {
                this.$emit('to-top', this.sortIndex);
            },
            toBottom() {
                this.$emit('to-bottom', this.sortIndex);
            },
        },
        components: { template },
    };
</script>
