<template>
    <el-card
        v-loading="vData.loading"
        class="page-card model-compare-page"
        shadow="never"
        name="模型列表"
    >
        <div class="model-list-filter">
            <el-form inline @submit.prevent>
                <!-- <el-form-item label="训练类型：">
                    <el-select v-model="vData.search.federated_learning_type" clearable>
                        <el-option
                            v-for="item in vData.federatedLearningTypeList"
                            :key="item.value"
                            :value="item.value"
                            :label="item.label"
                        />
                    </el-select>
                </el-form-item> -->
                <el-form-item label="模型类型：">
                    <el-select v-model="vData.search.component_type" clearable>
                        <el-option
                            v-for="item in vData.modelTypeList"
                            :key="item.value"
                            :value="item.value"
                            :label="item.label"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="训练来源：">
                    <el-select v-model="vData.search.flow_id" clearable>
                        <el-option
                            v-for="item in vData.flowList"
                            :key="item.flow_id"
                            :value="item.flow_id"
                            :label="item.flow_name"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button
                        type="primary"
                        @click="methods.searchEvent"
                    >
                        查询
                    </el-button>
                </el-form-item>
            </el-form>
            <div class="model-list-box">
                <div class="model-check-tips f14">勾选模型进行对比（<span>{{vData.checkedModelList.length}}</span>/3）</div>
                <div v-if="vData.modelList.length" v-loading="vData.modelLoading">
                    <div v-infinite-scroll="methods.getSearchModelList" infinite-scroll-delay="100" class="model-scroll-box">
                        <div v-for="(item,index) in vData.modelList" :key="item.id" class="model-scroll-item">
                            <el-popover
                                placement="right"
                                :width="500"
                                :height="500"
                                trigger="hover"
                                :open-delay="500"
                                :offset="44"
                                @after-enter="methods.popoverAfterEnter(index)"
                                @hide="methods.popoverHide(index)"
                            >
                                <div class="popover-content">
                                    <h4 class="mt10 mb10">{{ item.flow_name }}-{{ item.name }}</h4>
                                    <div class="echarts-box" style="min-height: 500px;">
                                        <ChartsWithTabs
                                            v-if="item.isPopoverShow"
                                            ref="ChartsWithTabsPopoverRef"
                                            result-api="/project/modeling/detail"
                                            :flow-node-id="item.flow_node_id"
                                            :flow-id="item.flow_id"
                                            :job-id="item.job_id"
                                            :show-topn="isShowTopN"
                                        />
                                    </div>
                                    <div class="jump-btns">
                                        <router-link class="mr10" target="_blank" :to="{ name: 'project-flow', query: { flow_id: item.flow_id }}">
                                            查看流程
                                        </router-link>
                                        <router-link class="mr10" target="_blank" :to="{ name: 'project-job-detail', query: { job_id: item.job_id, project_id: vData.search.project_id, member_role: item.role }}">
                                            查看任务
                                        </router-link>
                                    </div>
                                </div>
                                <template #reference>
                                    <div>
                                        <div class="div-p div-name-ks">
                                            <p class="p-model-name">[{{index +1}}] {{ item.flow_name }}-{{ item.name }}</p>
                                            <!-- <p v-if="item.result.validate.data.auc && item.result.validate.data.ks" class="p-model-ks"><span>auc: {{item.result.validate.data.auc.value}}</span><span>ks: {{item.result.validate.data.ks.value}}</span></p> -->
                                        </div>
                                        <div class="div-p div-type-time">
                                            <p class="p-type">{{item.component_type}}</p>
                                            <p class="f12">{{ dateFormat(item.created_time) }}</p>
                                        </div>
                                    </div>
                                </template>
                            </el-popover>
                            <el-checkbox v-model="item.ischecked" :disabled="!item.job_id || (vData.checkedModelList.length === 3 && !item.ischecked)" @change="methods.modelCheckboxChange($event, item, index)" />
                        </div>
                    </div>
                </div>
                <div v-else class="p10"><EmptyData /></div>
            </div>
        </div>
        <div class="model-compare-result-list">
            <el-row v-if="vData.checkedModelList.length">
                <el-col v-for="item in vData.checkedModelList" :key="item.id" :span="24/vData.checkedModelList.length">
                    <h4>{{ item.flow_name }}-{{ item.name }}</h4>
                    <div class="model-params-box">
                        <div class="mb20">
                            <div class="model-param">
                                <el-descriptions title="模型参数" :column="1" border>
                                    <!-- HorzSecureBoost -->
                                    <div v-if="item.component_type === 'HorzSecureBoost'">
                                        <!-- cv_param -->
                                        <el-descriptions-item label="KFold分割次数"> {{item.modeling_params.cv_param.n_splits}} </el-descriptions-item>
                                        <el-descriptions-item label="是否执行cv"> {{item.modeling_params.cv_param.need_cv}} </el-descriptions-item>
                                        <el-descriptions-item label="KFold之前洗牌"> {{item.modeling_params.cv_param.shuffle}} </el-descriptions-item>
                                        <!-- encrypt_param -->
                                        <!-- <el-descriptions-item label="同态加密方法"> {{item.modeling_params.encrypt_param.method}} </el-descriptions-item> -->
                                        <!-- objective_param -->
                                        <el-descriptions-item label="目标函数"> {{item.modeling_params.objective_param.objective}} </el-descriptions-item>
                                        <el-descriptions-item label="学习目标参数"> {{item.modeling_params.objective_param.params}} </el-descriptions-item>
                                        <!-- other_param -->
                                        <el-descriptions-item label="最大桶数量"> {{item.modeling_params.bin_num}} </el-descriptions-item>
                                        <!-- <el-descriptions-item label="提前结束的迭代次数"> {{item.modeling_params.early_stopping_rounds}} </el-descriptions-item> -->
                                        <el-descriptions-item label="学习率"> {{item.modeling_params.learning_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="n次迭代没变化是否停止"> {{item.modeling_params.n_iter_no_change}} </el-descriptions-item>
                                        <el-descriptions-item label="最大树数量"> {{item.modeling_params.num_trees}} </el-descriptions-item>
                                        <el-descriptions-item label="特征随机采样比率"> {{item.modeling_params.subsample_feature_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="任务类型"> {{item.modeling_params.task_type}} </el-descriptions-item>
                                        <el-descriptions-item label="收敛阈值"> {{item.modeling_params.tol}} </el-descriptions-item>
                                        <el-descriptions-item label="验证频次"> {{item.modeling_params.validation_freqs}} </el-descriptions-item>
                                        <!-- tree_param -->
                                        <el-descriptions-item label="L2 正则项系数"> {{item.modeling_params.tree_param.criterion_params}} </el-descriptions-item>
                                        <el-descriptions-item label="最大树深度"> {{item.modeling_params.tree_param.max_depth}} </el-descriptions-item>
                                        <el-descriptions-item label="单个拆分要达到的最小增益"> {{item.modeling_params.tree_param.min_impurity_split}} </el-descriptions-item>
                                        <el-descriptions-item label="每个叶子节点包含的最小样本数"> {{item.modeling_params.tree_param.min_leaf_node}} </el-descriptions-item>
                                        <el-descriptions-item label="分裂一个内部节点(非叶子节点)需要的最小样本"> {{item.modeling_params.tree_param.min_sample_split}} </el-descriptions-item>
                                    </div>
                                    <!-- HorzLR -->
                                    <div v-if="item.component_type === 'HorzLR'">
                                        <!-- cv_param -->
                                        <el-descriptions-item label="KFold分割次数"> {{item.modeling_params.n_splits}} </el-descriptions-item>
                                        <el-descriptions-item label="是否执行cv"> {{item.modeling_params.need_cv}} </el-descriptions-item>
                                        <el-descriptions-item label="KFold之前洗牌"> {{item.modeling_params.shuffle}} </el-descriptions-item>
                                        <!-- other_param -->
                                        <el-descriptions-item label="惩罚项系数"> {{item.modeling_params.alpha}} </el-descriptions-item>
                                        <el-descriptions-item label="批量大小"> {{item.modeling_params.batch_size}} </el-descriptions-item>
                                        <el-descriptions-item label="学习速率的衰减率"> {{item.modeling_params.decay}} </el-descriptions-item>
                                        <el-descriptions-item label="衰减率是否开平方"> {{item.modeling_params.decay_sqrt}} </el-descriptions-item>
                                        <el-descriptions-item label="判断收敛性与否的方法"> {{item.modeling_params.early_stop}} </el-descriptions-item>
                                        <!-- <el-descriptions-item label="提前结束的迭代次数"> {{item.modeling_params.early_stopping_rounds}} </el-descriptions-item> -->
                                        <el-descriptions-item label="学习率"> {{item.modeling_params.learning_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="最大迭代次数"> {{item.modeling_params.max_iter}} </el-descriptions-item>
                                        <el-descriptions-item label="多分类策略"> {{item.modeling_params.multi_class}} </el-descriptions-item>
                                        <el-descriptions-item label="优化算法"> {{item.modeling_params.optimizer}} </el-descriptions-item>
                                        <el-descriptions-item label="惩罚方式"> {{item.modeling_params.penalty}} </el-descriptions-item>
                                        <el-descriptions-item label="收敛容忍度"> {{item.modeling_params.tol}} </el-descriptions-item>
                                        <!-- <el-descriptions-item label="验证频次"> {{item.modeling_params.validation_freqs}} </el-descriptions-item> -->
                                        <!-- init_param -->
                                        <el-descriptions-item label="是否需要偏置系数"> {{item.modeling_params.fit_intercept}} </el-descriptions-item>
                                        <el-descriptions-item label="模型初始化方式"> {{item.modeling_params.init_method}} </el-descriptions-item>
                                    </div>
                                    <!-- HorzNN -->
                                    <div v-if="item.component_type === 'HorzNN'">
                                        <!-- other_param -->
                                        <el-descriptions-item label="最大迭代次数"> {{item.modeling_params.batch_size}} </el-descriptions-item>
                                        <el-descriptions-item label="批量大小"> {{item.modeling_params.batch_size}} </el-descriptions-item>
                                        <el-descriptions-item label="学习率"> {{item.modeling_params.optimizer.learning_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="优化算法"> {{item.modeling_params.optimizer.optimizer}} </el-descriptions-item>
                                        <el-descriptions-item label="损失函数"> {{item.modeling_params.loss}} </el-descriptions-item>
                                        <el-descriptions-item label="每层参数"> {{item.modeling_params.nn_define}} </el-descriptions-item>
                                    </div>
                                    <!-- VertSecureBoost -->
                                    <div v-if="item.component_type === 'VertSecureBoost'">
                                        <!-- cv_param -->
                                        <el-descriptions-item label="KFold分割次数"> {{item.modeling_params.cv_param.n_splits}} </el-descriptions-item>
                                        <el-descriptions-item label="是否执行cv"> {{item.modeling_params.cv_param.need_cv}} </el-descriptions-item>
                                        <el-descriptions-item label="KFold之前洗牌"> {{item.modeling_params.cv_param.shuffle}} </el-descriptions-item>
                                        <!-- encrypt_param -->
                                        <el-descriptions-item label="同态加密方法"> {{item.modeling_params.encrypt_param.method}} </el-descriptions-item>
                                        <!-- objective_param -->
                                        <el-descriptions-item label="目标函数"> {{item.modeling_params.objective_param.objective}} </el-descriptions-item>
                                        <el-descriptions-item label="学习目标参数"> {{item.modeling_params.objective_param.params}} </el-descriptions-item>
                                        <!-- other_param -->
                                        <el-descriptions-item label="最大桶数量"> {{item.modeling_params.bin_num}} </el-descriptions-item>
                                        <el-descriptions-item label="提前结束的迭代次数"> {{item.modeling_params.early_stopping_rounds}} </el-descriptions-item>
                                        <el-descriptions-item v-if="item.modeling_params.work_mode === 'dp'" label="隐私预算"> {{item.modeling_params.epsilon}} </el-descriptions-item>
                                        <el-descriptions-item label="学习率"> {{item.modeling_params.learning_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="n次迭代没变化是否停止"> {{item.modeling_params.n_iter_no_change}} </el-descriptions-item>
                                        <el-descriptions-item label="最大树数量"> {{item.modeling_params.num_trees}} </el-descriptions-item>
                                        <el-descriptions-item label="特征随机采样比"> {{item.modeling_params.subsample_feature_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="任务类型"> {{item.modeling_params.task_type}} </el-descriptions-item>
                                        <el-descriptions-item label="收敛阈值"> {{item.modeling_params.tol}} </el-descriptions-item>
                                        <el-descriptions-item label="验证频次"> {{item.modeling_params.validation_freqs}} </el-descriptions-item>
                                        <el-descriptions-item label="工作模式"> {{item.modeling_params.work_mode}} </el-descriptions-item>
                                        <el-descriptions-item v-if="item.modeling_params.work_mode === 'layered'" label="promoter深度"> {{item.modeling_params.promoter_depth}} </el-descriptions-item>
                                        <el-descriptions-item v-if="item.modeling_params.work_mode === 'layered'" label="provider深度"> {{item.modeling_params.provider_depth}} </el-descriptions-item>
                                        <el-descriptions-item v-if="item.modeling_params.work_mode === 'skip'" label="单方每次构建树的数量"> {{item.modeling_params.tree_num_per_member}} </el-descriptions-item>
                                        <!-- tree_param -->
                                        <el-descriptions-item label="L2 正则项系数"> {{item.modeling_params.tree_param.criterion_params}} </el-descriptions-item>
                                        <el-descriptions-item label="树的最大深度"> {{item.modeling_params.tree_param.max_depth}} </el-descriptions-item>
                                        <el-descriptions-item label="单个拆分要达到的最小增益"> {{item.modeling_params.tree_param.min_impurity_split}} </el-descriptions-item>
                                        <el-descriptions-item label="每个叶子节点包含的最小样本数"> {{item.modeling_params.tree_param.min_leaf_node}} </el-descriptions-item>
                                        <el-descriptions-item label="分裂一个内部节点(非叶子节点)需要的最小样本"> {{item.modeling_params.tree_param.min_sample_split}} </el-descriptions-item>
                                    </div>
                                    <!-- VertLR -->
                                    <div v-if="item.component_type === 'VertLR'">
                                        <!-- cv_param -->
                                        <el-descriptions-item label="KFold分割次数"> {{item.modeling_params.n_splits}} </el-descriptions-item>
                                        <el-descriptions-item label="是否执行cv"> {{item.modeling_params.need_cv}} </el-descriptions-item>
                                        <el-descriptions-item label="KFold之前洗牌"> {{item.modeling_params.shuffle}} </el-descriptions-item>
                                        <!-- encrypt_param -->
                                        <el-descriptions-item label="同态加密方法"> {{item.modeling_params.method}} </el-descriptions-item>
                                        <!-- other_param -->
                                        <el-descriptions-item label="惩罚项系数"> {{item.modeling_params.alpha}} </el-descriptions-item>
                                        <el-descriptions-item label="批量大小"> {{item.modeling_params.batch_size}} </el-descriptions-item>
                                        <el-descriptions-item label="学习速率的衰减率"> {{item.modeling_params.decay}} </el-descriptions-item>
                                        <el-descriptions-item label="衰减率是否开平方"> {{item.modeling_params.decay_sqrt}} </el-descriptions-item>
                                        <el-descriptions-item label="判断收敛性与否的方法"> {{item.modeling_params.early_stop}} </el-descriptions-item>
                                        <el-descriptions-item label="提前结束的迭代次数"> {{item.modeling_params.early_stopping_rounds}} </el-descriptions-item>
                                        <el-descriptions-item label="学习率"> {{item.modeling_params.learning_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="最大迭代次数"> {{item.modeling_params.max_iter}} </el-descriptions-item>
                                        <el-descriptions-item label="多分类策略"> {{item.modeling_params.multi_class}} </el-descriptions-item>
                                        <el-descriptions-item label="优化算法"> {{item.modeling_params.optimizer}} </el-descriptions-item>
                                        <el-descriptions-item label="惩罚方式"> {{item.modeling_params.penalty}} </el-descriptions-item>
                                        <el-descriptions-item label="收敛容忍度"> {{item.modeling_params.tol}} </el-descriptions-item>
                                        <el-descriptions-item label="验证频次"> {{item.modeling_params.validation_freqs}} </el-descriptions-item>
                                        <!-- init_param -->
                                        <el-descriptions-item label="是否需要偏置系数"> {{item.modeling_params.fit_intercept}} </el-descriptions-item>
                                        <el-descriptions-item label="模型初始化方式"> {{item.modeling_params.init_method}} </el-descriptions-item>
                                    </div>
                                    <!-- VertNN -->
                                    <div v-if="item.component_type === 'VertNN'">
                                        <!-- other_param -->
                                        <el-descriptions-item label="最大迭代次数"> {{item.modeling_params.epochs}} </el-descriptions-item>
                                        <el-descriptions-item label="交互层学习率"> {{item.modeling_params.interactive_layer_lr}} </el-descriptions-item>
                                        <el-descriptions-item label="批量大小"> {{item.modeling_params.batch_size}} </el-descriptions-item>
                                        <el-descriptions-item label="学习率"> {{item.modeling_params.optimizer.learning_rate}} </el-descriptions-item>
                                        <el-descriptions-item label="优化算法"> {{item.modeling_params.optimizer.optimizer}} </el-descriptions-item>
                                        <el-descriptions-item label="损失函数"> {{item.modeling_params.loss}} </el-descriptions-item>
                                        <el-descriptions-item label="底层参数"> {{item.modeling_params.bottom_nn_define}} </el-descriptions-item>
                                        <el-descriptions-item label="中层参数"> {{item.modeling_params.interactive_layer_define}} </el-descriptions-item>
                                        <el-descriptions-item label="顶层参数"> {{item.modeling_params.top_nn_define}} </el-descriptions-item>
                                    </div>
                                </el-descriptions>
                            </div>
                        </div>
                    </div>
                </el-col>
                <el-divider />
                <el-col v-for="item in vData.checkedModelList" :key="item.id" :span="24/vData.checkedModelList.length">
                    <el-descriptions title="网格搜索参数" :column="2" border v-if="item.modeling_params?.grid_search_param?.need_grid_search">
                        <el-descriptions-item
                            v-for="(value, keyName) in item.modeling_params.grid_search_param.params_list"
                            :key="keyName"
                            :label="mapGridName(keyName)"
                        > {{ value
                        }} </el-descriptions-item>
                        <el-divider />
                    </el-descriptions>
                </el-col>
                <el-col v-for="(item, index) in vData.checkedModelList" :key="item.id" :span="24/vData.checkedModelList.length">
                    <ChartsWithTabs
                        :ref="setChartsWithTabsRef"
                        result-api="/project/modeling/detail"
                        :flow-node-id="item.flow_node_id"
                        :flow-id="item.flow_id"
                        :job-id="item.job_id"
                        :show-topn="vData.isShowTopN"
                        :is-tab-linkage="vData.isTabLinkage"
                        @change-tabname="methods.changeTabName($event, index)"
                    />
                </el-col>
            </el-row>
            <div v-else class="p10 model-result-empty"><EmptyData msg="勾选左侧模型进行对比吧～" /></div>
        </div>
    </el-card>
</template>

<script>
    import { ref, reactive, getCurrentInstance, nextTick, onBeforeMount } from 'vue';
    import { useRoute } from 'vue-router';
    import table from '@src/mixins/table';
    import { mapGridName } from '../../utils';

    export default {
        mixins: [table],
        setup(props, context) {
            const route = useRoute();
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const ChartsWithTabsRef = ref([]);
            const setChartsWithTabsRef = (el) => {
                ChartsWithTabsRef.value.push(el);
            };
            const ChartsWithTabsPopoverRef = ref();
            const vData = reactive({
                loading:      false,
                modelLoading: false,
                search:       {
                    // federated_learning_type: '',
                    component_type: '',
                    flow_id:        '',
                    project_id:     route.query.project_id,
                    page_index:     0,
                    page_size:      20,
                    noMore:         false,
                },
                federatedLearningTypeList: [{
                    label: '横向',
                    value: 'horizontal',
                },{
                    label: '纵向',
                    value: 'vertical',
                },{
                    label: '混合',
                    value: 'mix',
                }],
                modelTypeList: [{
                    label: '横向 XGB',
                    value: 'HorzSecureBoost',
                },{
                    label: '横向 LR',
                    value: 'HorzLR',
                },{
                    label: '横向深度学习',
                    value: 'HorzNN',
                },{
                    label: '纵向 XGB',
                    value: 'VertSecureBoost',
                },{
                    label: '纵向 LR',
                    value: 'VertLR',
                },{
                    label: '纵向深度学习',
                    value: 'VertNN',
                },
                // {
                //     label: '混合 XGB',
                //     value: 'MixSecureBoost',
                // },{
                //     label: '混合 LR',
                //     value: 'MixLR',
                // },{
                //     label: '混合深度学习',
                //     value: 'MixNN',
                // },
                ],
                list:             [],
                isShowTopN:       true, // show topn
                flowList:         [],
                modelList:        [],
                checkedModelList: [],
                isTabLinkage:     true,
            });
            const methods = {
                popoverAfterEnter(idx) {
                    vData.modelList[idx].isPopoverShow = true;
                },
                popoverHide(idx) {
                    vData.modelList[idx].isPopoverShow = false;
                },
                async getFlowList() {
                    const { code, data } = await $http.post({
                        url:  '/project/flow/query',
                        data: {
                            project_id: vData.search.project_id,
                        },
                    });

                    nextTick(_=> {
                        if (code === 0 && data && data.list) {
                            vData.flowList = data.list;
                        }
                    });
                },
                searchEvent() {
                    vData.search.page_index = 0;
                    vData.checkedModelList = [];
                    vData.modelList = [];
                    vData.search.noMore = false;
                    methods.getSearchModelList();
                },
                async getSearchModelList() {
                    if(vData.search.noMore) return;
                    vData.modelLoading = true;
                    const { code, data } = await $http.post({
                        url:  '/project/modeling/query',
                        data: Object.assign({ withModelingResult: true, withModelingParams: true }, vData.search),
                    });

                    if(code === 0) {
                        if (data.list && data.list.length) {
                            vData.search.noMore = data.list.length < 20;
                            for(const i in data.list) {
                                methods.getModelDetail(data.list[i]);
                                data.list[i].ischecked = false;
                                data.list[i].isPopoverShow = false;
                                vData.modelList.push(data.list[i]);
                            }
                            vData.search.page_index++;
                        } else {
                            vData.modelList = [];
                            vData.search.noMore = false;
                        }
                    }
                    vData.modelLoading = false;
                },
                async getModelDetail(item) {
                    const { code, data } = await $http.post({
                        url:  '/project/modeling/detail',
                        data: {
                            flowId:     item.flow_id,
                            flowNodeId: item.flow_node_id,
                            jobId:      item.job_id,
                            type:       'ks',
                        },
                    });

                    if(code === 0 && data) {
                        for (let i=0; i<vData.modelList.length; i++) {
                            if (vData.modelList[i].job_id === item.job_id) vData.modelList[i].result = data.result;
                        }
                    }
                },
                modelCheckboxChange(val, row, idx) {
                    const { job_id,  flow_node_id } = row;
                    const item = vData.modelList.find(x => x.job_id === job_id && x.flow_node_id === flow_node_id);

                    if(val) {
                        vData.checkedModelList.push({ ...item });
                    } else {
                        const i = vData.checkedModelList.findIndex(x => x.job_id === job_id);

                        vData.checkedModelList.splice(i, 1);
                    }
                    ChartsWithTabsRef.value = [];
                    // console.log(vData.checkedModelList);
                    // methods.showResult(item, idx);
                },
                async showResult(item, idx) {
                    setTimeout(_ => {
                        ChartsWithTabsRef.value && ChartsWithTabsRef.value.readResult();
                    }, 300);
                },
                // getDetail(data) {
                //     vData.checkedModelList.forEach(item => {
                //         if (item.job_id === data.job_id) {
                //             item.result = data.result;
                //             item.task_config = data.task_config;
                //         }
                //     });
                //     console.log('vData.checkedModelList-----', vData.checkedModelList);
                //     console.log('data-----',data);
                // },
                changeTabName(tabName, index) {
                    nextTick(() => {
                        // console.log(ChartsWithTabsRef.value)
                        if (ChartsWithTabsRef.value && ChartsWithTabsRef.value.length > 1) {
                            const idx = index === 0 ? 1 : 0;

                            ChartsWithTabsRef.value.forEach((item, ins) => {
                                if(ins !== index && item ){
                                     ChartsWithTabsRef.value[ins].tabName = ChartsWithTabsRef.value[index].tabName
                                }
                            })
                            // ChartsWithTabsRef.value[idx].tabName = ChartsWithTabsRef.value[index].tabName;
                            console.log("ChartsWithTabsRef.value",ChartsWithTabsRef.value)
                        }
                    });
                },
            };

            onBeforeMount(()=> {
                methods.getFlowList();
                methods.getSearchModelList();
            });

            return {
                vData,
                methods,
                setChartsWithTabsRef,
                ChartsWithTabsPopoverRef: (el) => ChartsWithTabsPopoverRef.value = el,
                mapGridName,
            };
        },
    };
</script>

<style lang="scss" scoped>
$border-default: 1px solid #f5f5f7;
.page-card{min-height: calc(100vh - 40px);}
.model-compare-page {
    :deep(.board-card__body) {
        display: flex;
        .model-list-filter {
            width: 352px;
            height: calc(100vh - 40px);
            border-right: $border-default;
            display: flex;
            flex-direction: column;
            .board-form--inline .board-form-item {
                margin-right: 10px;
            }
            .board-form {
                border-bottom: $border-default;
            }
            .model-list-box {
                border-bottom: $border-default;
                .model-check-tips {
                    height: 40px;
                    line-height: 40px;
                    border-bottom: $border-default;
                    // span {
                    //     color: $color-text-selected;
                    // }
                    color: $color-text-selected;
                }
                .model-scroll-box {
                    overflow: auto;
                    height: calc(100vh - 186px);
                    .model-scroll-item {
                        font-size: 14px;
                        border-bottom: $border-default;
                        padding: 4px 10px 4px 0;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        .board-tooltip__trigger {
                            display: flex;
                            justify-content: space-between;
                            cursor: pointer;
                        }
                        .div-p {
                            display: flex;
                            flex-direction: column;
                        }
                        .div-name-ks {
                            .p-model-name {
                                width: 194px;
                                overflow:hidden;
                                white-space: nowrap;
                                text-overflow: ellipsis;
                            }
                            .p-model-ks {
                                span:first-child {
                                    padding-right: 6px;
                                }
                            }
                        }
                        .div-type-time {
                            .p-type {
                                text-align: right;
                            }
                        }
                    }
                    .model-scroll-item:last-child {
                        border: unset;
                    }
                }
            }
        }
        .model-compare-result-list {
            flex: 1;
            height: calc(100vh - 40px);
            overflow: auto;
            .result-box {
                display: flex;
                justify-content: space-between;
                padding: 0 0 20px 20px;
                .result-item {
                    .board-descriptions__body .board-descriptions__table.is-bordered .board-descriptions__cell {
                        word-break: break-all;
                    }
                    h4 {
                        font-size: 16px;
                        word-break: break-all;
                        margin-bottom: 10px;
                        color: $--color-primary;
                        font-weight: bold;
                    }
                }
            }
            .model-result-empty {
                background: #fefefe;
                width: 100%;
                height: 100%;
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 22px;
                color: #999;
                img {
                    max-height: 220px;
                }
            }
        }
    }
}
.popover-content {
    >h4 {
        font-weight: bold;
    }
    .jump-btns {
        height: 30px;
        display: flex;
        justify-content: flex-end;
        align-items: flex-end;
    }
    .echarts-box {
        display: flex;
        justify-content: center;
        align-items: center;
        border: 1px solid #f5f5f7;
    }
}
.model-param{
    display: flex;
    flex-direction: column;
    row-gap: 20px;
}
</style>
