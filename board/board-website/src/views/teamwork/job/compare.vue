<template>
    <el-card
        v-loading="loading"
        shadow="never"
        class="page"
    >
        <h2 class="title">任务对比</h2>

        <el-table
            v-if="baseInfoList.length"
            class="compare-head"
            :data="baseInfoList[0]"
        >
            <el-table-column
                prop="$title"
                width="120"
            />
            <el-table-column
                v-for="member in members"
                :key="member.id"
                :label="member.name"
            >
                <template v-slot:header>
                    {{ member.name }}
                    <p class="head-id">
                        {{ member.id }}
                    </p>
                </template>
            </el-table-column>
        </el-table>
        <el-collapse v-model="activeName">
            <div class="nav-title" name="基础信息">
                <el-collapse-item
                    title="基础信息"
                    name="baseInfo"
                >
                    <el-table
                        v-for="(list, index) in baseInfoList"
                        :key="index"
                        :data="list"
                        :show-header="false"
                        stripe
                    >
                        <el-table-column
                            prop="$title"
                            width="120"
                        />
                        <el-table-column
                            v-for="member in members"
                            :key="member.id"
                            :label="member.name"
                        >
                            <template v-slot="scope">
                                {{ scope.row[member.id] }}
                            </template>
                        </el-table-column>
                    </el-table>
                </el-collapse-item>
            </div>
            <div class="nav-title" name="模型概要">
                <el-collapse-item
                    title="模型概要"
                    name="modelInfo"
                >
                    <el-table
                        :data="[{}]"
                        :show-header="false"
                    >
                        <el-table-column
                            v-for="chart in chartsList"
                            :key="chart.flow_id"
                        >
                            <ChartsWithTabs
                                result-api="/project/modeling/detail"
                                :flow-node-id="chart.flow_node_id"
                                :flow-id="chart.flow_id"
                                :job-id="chart.job_id"
                                :show-topn="true"
                            />
                        </el-table-column>
                    </el-table>
                </el-collapse-item>
            </div>
            <div class="nav-title" name="组件详情">
                <el-collapse-item
                    title="组件详情"
                    name="detail"
                >
                    <el-table
                        :data="detailList"
                        :show-header="false"
                        stripe
                    >
                        <el-table-column
                            prop="$title"
                            width="120"
                        />
                        <!-- Used to add page navigation -->
                        <el-table-column width="1">
                            <template v-slot="scope">
                                <div
                                    class="nav-title"
                                    :name="scope.row.$title"
                                />
                            </template>
                        </el-table-column>
                        <el-table-column
                            v-for="member in members"
                            :key="member.id"
                            :label="member.name"
                        >
                            <template v-slot="scope">
                                <template v-if="scope.row[member.id] && scope.row[member.id].task">
                                    <h4 class="f14 mb10">
                                        <strong>{{ scope.row[member.id].task.component_name }}:</strong>
                                    </h4>
                                    <el-collapse v-model="scope.row[member.id].componentParaActive">
                                        <el-collapse-item title="组件参数" name="1">
                                            <el-descriptions :column="scope.row[member.id].task.task_type === 'DataIO' || scope.row[member.id].task.task_type === 'FillMissingValue' || scope.row[member.id].task.task_type === 'VertNN' || scope.row[member.id].task.task_type === 'VertPearson' || scope.row[member.id].task.task_type === 'HorzFeatureBinning' || scope.row[member.id].task.task_type === 'Binning' || scope.row[member.id].task.task_type === 'MixFeatureBinning' ? 1 : 2">
                                                <el-descriptions-item v-for="(value, key) in scope.row[member.id].task.task_conf.Chinese_params.length !==0 ? scope.row[member.id].task.task_conf.Chinese_params : scope.row[member.id].task.task_conf.params" :key="key" :label="key"> {{value}} </el-descriptions-item>
                                            </el-descriptions>
                                        </el-collapse-item>
                                    </el-collapse>
                                    <template v-if="componentsList[scope.row[member.id].task.task_type]">
                                        <component
                                            :autoReadResult="true"
                                            :is="`${scope.row[member.id].task.task_type}-result`"
                                            :flow-node-id="scope.row[member.id].task.flow_node_id"
                                            :flow-id="scope.row[member.id].task.flow_id"
                                            :job-id="scope.row[member.id].task.job_id"
                                            :my-role="member.my_role"
                                        />
                                    </template>
                                </template>
                                <p v-else>暂无结果</p>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-collapse-item>
            </div>
        </el-collapse>
    </el-card>
</template>

<script>
    import {
        componentsList,
        resultComponents,
    } from '../visual/component-list/component-map';

    export default {
        components: {
            ...componentsList,
            ...resultComponents,
        },
        data() {
            return {
                componentsList,
                loading:     false,
                flow_id:     '',
                ids:         [],
                members:     [],
                member_role: [],
                activeName:  ['baseInfo', 'modelInfo', 'detail'],
                jobStatus:   {
                    created:          '已创建',
                    wait_run:         '等待运行',
                    running:          '运行中',
                    stop:             '终止',
                    wait_stop:        '等待结束',
                    stop_on_running:  '人为关闭',
                    error_on_running: '程序异常关闭',
                    error:            '执行失败',
                    success:          '成功(正常结束)',
                },
                baseInfoList: [],
                detailList:   [],
                chartsList:   [],
            };
        },
        created() {
            const { flow_id, ids, member_role } = this.$route.query;

            this.flow_id = flow_id;
            this.member_role = member_role.split(',');
            if(ids) {
                this.ids = ids.split(',');
                this.getResult();
            }
        },
        methods: {
            async getResult() {
                const Queue = [];

                this.loading = true;
                this.ids.forEach((job_id, index) => {
                    Queue.push(this.$http.get({
                        url:    '/flow/job/detail',
                        params: {
                            flow_id:     this.flow_id,
                            member_role: this.member_role[index],
                            needResult:  true,
                            job_id,
                        },
                    }));
                });

                const res = await Promise.all(Queue);

                this.loading = false;
                if(res) {
                    const types = {
                        vertical:   '纵向',
                        horizontal: '横向',
                        mix:        '混合',
                    };
                    const baseInfoKey = {
                        status:   '运行状态',
                        my_role:  '我的角色',
                        during:   '执行时长',
                        job_type: '任务类型',
                        progress: '任务进度',
                    };

                    this.members = [];
                    this.baseInfoList = [];
                    for(const key in baseInfoKey) {
                        const val = baseInfoKey[key];

                        this.baseInfoList.push([{
                            $key:   key,
                            $title: val,
                        }]);
                    }
                    this.detailList = [];
                    res.forEach(({ code, data }) => {
                        if(code === 0) {
                            const { job, task_views } = data;

                            this.members.push(job);
                            this.baseInfoList.forEach(list => {
                                const row = list[0];

                                switch(row.$key) {
                                case 'during':
                                    row[job.id] = job.finish_time > job.start_time ? this.timeFormat(job.finish_time / 1000 - job.start_time / 1000) : '--';
                                    break;
                                case 'job_type':
                                    row[job.id] = types[job.federated_learning_type];
                                    break;
                                case 'status':
                                    row[job.id] = this.jobStatus[job[row.$key]];
                                    break;
                                case 'progress':
                                    row[job.id] = `${job[row.$key]}%`;
                                    break;
                                default:
                                    row[job.id] = job[row.$key];
                                    break;
                                }
                            });

                            task_views.forEach((task, index) => {
                                task.task.task_conf.Chinese_params = {};
                                switch (task.task.task_conf.module) {
                                case 'DataIO':
                                    for (const i in task.task.task_conf.params) {
                                        task.task.task_conf.Chinese_params.数据资源id = task.task.task_conf.params.data_set_id;
                                        task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.need_features;
                                        task.task.task_conf.Chinese_params.包含Y = !!task.task.task_conf.params.with_label;
                                    }
                                    break;
                                case 'Intersection':
                                    for (const i in task.task.task_conf.params) {
                                        task.task.task_conf.Chinese_params.对齐算法 = task.task.task_conf.params.intersect_method;
                                        task.task.task_conf.Chinese_params.是否保存对齐后数据 = task.task.task_conf.params.save_dataset ? '是' : '否';
                                    }
                                    break;
                                case 'Segment':
                                    for (const i in task.task.task_conf.params) {
                                        task.task.task_conf.Chinese_params.数据切分随机数 = task.task.task_conf.params.random_num;
                                        task.task.task_conf.Chinese_params.训练与验证数据比例 = task.task.task_conf.params.train_ratio;
                                    }
                                    break;
                                case 'FeatureStatistic':
                                    for (const i in task.task.task_conf.params) {
                                        task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.features ? task.task.task_conf.params.features : [];
                                    }
                                    break;
                                case 'FillMissingValue':
                                    for (const i in task.task.task_conf.params) {
                                        const featureslist = [];
                                        const feature = JSON.parse(JSON.stringify(JSON.parse(task.task.task_conf.params.features)));

                                        let class_type = '';

                                        for (const key in feature) {
                                            featureslist.push(key);
                                            class_type = feature[key].method;
                                        }
                                        task.task.task_conf.Chinese_params.特征 = featureslist;
                                        task.task.task_conf.Chinese_params.填充策略 = class_type;
                                    }
                                    break;
                                case 'VertLR':
                                    task.task.task_conf.Chinese_params.LR方法 = task.task.task_conf.params.lr_method;
                                    task.task.task_conf.Chinese_params.惩罚方法 = task.task.task_conf.params.penalty;
                                    task.task.task_conf.Chinese_params.收敛容忍度 = task.task.task_conf.params.tol;
                                    task.task.task_conf.Chinese_params.惩罚项系数 = task.task.task_conf.params.alpha;
                                    task.task.task_conf.Chinese_params.优化算法 = task.task.task_conf.params.optimizer;
                                    task.task.task_conf.Chinese_params.批量大小 = task.task.task_conf.params.batch_size;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.最大迭代次数 = task.task.task_conf.params.max_iter;
                                    task.task.task_conf.Chinese_params.判断收敛性与否的方法 = task.task.task_conf.params.early_stop;
                                    task.task.task_conf.Chinese_params.学习率的衰减率 = task.task.task_conf.params.decay;
                                    task.task.task_conf.Chinese_params.衰减率是否开平方 = task.task.task_conf.params.decay_sqrt ? '是' : '否';
                                    task.task.task_conf.Chinese_params.多分类策略 = task.task.task_conf.params.multi_class;
                                    task.task.task_conf.Chinese_params.验证频次 = task.task.task_conf.params.validation_freqs;
                                    task.task.task_conf.Chinese_params.提前结束的迭代次数 = task.task.task_conf.params.early_stopping_rounds;
                                    task.task.task_conf.Chinese_params.模型初始化方式 = task.task.task_conf.params.init_method;
                                    task.task.task_conf.Chinese_params.是否需要偏置系数 = task.task.task_conf.params.fit_intercept ? '是' : '否';
                                    task.task.task_conf.Chinese_params.同态加密方法 = task.task.task_conf.params.method;
                                    task.task.task_conf.Chinese_params.KFold分割次数 = task.task.task_conf.params.n_splits;
                                    task.task.task_conf.Chinese_params.KFold之前洗牌 = task.task.task_conf.params.shuffle ? '是' : '否';
                                    task.task.task_conf.Chinese_params.是否执行cv = task.task.task_conf.params.need_cv ? '是' : '否';
                                    break;
                                case 'HorzLR':
                                    task.task.task_conf.Chinese_params.LR方法 = task.task.task_conf.params.lr_method || '';
                                    task.task.task_conf.Chinese_params.惩罚方法 = task.task.task_conf.params.penalty || '';
                                    task.task.task_conf.Chinese_params.收敛容忍度 = task.task.task_conf.params.tol || '';
                                    task.task.task_conf.Chinese_params.惩罚项系数 = task.task.task_conf.params.alpha || '';
                                    task.task.task_conf.Chinese_params.优化算法 = task.task.task_conf.params.optimizer || '';
                                    task.task.task_conf.Chinese_params.批量大小 = task.task.task_conf.params.batch_size || '';
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate || '';
                                    task.task.task_conf.Chinese_params.最大迭代次数 = task.task.task_conf.params.max_iter || '';
                                    task.task.task_conf.Chinese_params.判断收敛性与否的方法 = task.task.task_conf.params.early_stop || '';
                                    task.task.task_conf.Chinese_params.学习率的衰减率 = task.task.task_conf.params.decay || '';
                                    task.task.task_conf.Chinese_params.衰减率是否开平方 = task.task.task_conf.params.decay_sqrt ? '是' : '否';
                                    task.task.task_conf.Chinese_params.多分类策略 = task.task.task_conf.params.multi_class || '';
                                    task.task.task_conf.Chinese_params.模型初始化方式 = task.task.task_conf.params.init_method || '';
                                    task.task.task_conf.Chinese_params.是否需要偏置系数 = task.task.task_conf.params.fit_intercept ? '是' : '否';
                                    task.task.task_conf.Chinese_params.KFold分割次数 = task.task.task_conf.params.n_splits || '';
                                    task.task.task_conf.Chinese_params.KFold之前洗牌 = task.task.task_conf.params.shuffle ? '是' : '否';
                                    task.task.task_conf.Chinese_params.是否执行cv = task.task.task_conf.params.need_cv ? '是' : '否';
                                    break;
                                case 'MixLR':
                                    task.task.task_conf.Chinese_params.LR方法 = task.task.task_conf.params.lr_method;
                                    task.task.task_conf.Chinese_params.惩罚方法 = task.task.task_conf.params.penalty;
                                    task.task.task_conf.Chinese_params.收敛容忍度 = task.task.task_conf.params.tol;
                                    task.task.task_conf.Chinese_params.惩罚项系数 = task.task.task_conf.params.alpha;
                                    task.task.task_conf.Chinese_params.优化算法 = task.task.task_conf.params.optimizer;
                                    task.task.task_conf.Chinese_params.批量大小 = task.task.task_conf.params.batch_size;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.最大迭代次数 = task.task.task_conf.params.max_iter;
                                    task.task.task_conf.Chinese_params.判断收敛性与否的方法 = task.task.task_conf.params.early_stop;
                                    task.task.task_conf.Chinese_params.学习率的衰减率 = task.task.task_conf.params.decay;
                                    task.task.task_conf.Chinese_params.衰减率是否开平方 = task.task.task_conf.params.decay_sqrt ? '是' : '否';
                                    task.task.task_conf.Chinese_params.多分类策略 = task.task.task_conf.params.multi_class;
                                    task.task.task_conf.Chinese_params.验证频次 = task.task.task_conf.params.validation_freqs;
                                    task.task.task_conf.Chinese_params.提前结束的迭代次数 = task.task.task_conf.params.early_stopping_rounds;
                                    task.task.task_conf.Chinese_params.模型初始化方式 = task.task.task_conf.params.init_method;
                                    task.task.task_conf.Chinese_params.是否需要偏置系数 = task.task.task_conf.params.fit_intercept ? '是' : '否';
                                    task.task.task_conf.Chinese_params.同态加密方法 = task.task.task_conf.params.encrypt_param.method;
                                    task.task.task_conf.Chinese_params.KFold分割次数 = task.task.task_conf.params.cv_param.n_splits;
                                    task.task.task_conf.Chinese_params.KFold之前洗牌 = task.task.task_conf.params.cv_param.shuffle ? '是' : '否';
                                    task.task.task_conf.Chinese_params.是否执行cv = task.task.task_conf.params.need_cv ? '是' : '否';
                                    break;
                                case 'VertNN':
                                    task.task.task_conf.Chinese_params.最大迭代次数 = task.task.task_conf.params.epochs;
                                    task.task.task_conf.Chinese_params.交互层学习率 = task.task.task_conf.params.interactive_layer_lr;
                                    task.task.task_conf.Chinese_params.批量大小 = task.task.task_conf.params.batch_size;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.优化算法 = task.task.task_conf.params.optimizer;
                                    task.task.task_conf.Chinese_params.损失函数 = task.task.task_conf.params.loss;
                                    task.task.task_conf.Chinese_params.底层参数 = task.task.task_conf.params.bottom_nn_define;
                                    task.task.task_conf.Chinese_params.中层参数 = task.task.task_conf.params.interactive_layer_define;
                                    task.task.task_conf.Chinese_params.顶层参 = task.task.task_conf.params.top_nn_define;
                                    break;
                                case 'HorzNN':
                                    task.task.task_conf.Chinese_params.最大迭代次数 = task.task.task_conf.params.max_iter;
                                    task.task.task_conf.Chinese_params.批量大小 = task.task.task_conf.params.batch_size;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.优化算法 = task.task.task_conf.params.optimizer;
                                    task.task.task_conf.Chinese_params.损失函数 = task.task.task_conf.params.loss;
                                    task.task.task_conf.Chinese_params.每层参数 = task.task.task_conf.params.nn_define;
                                    break;
                                case 'VertSecureBoost':
                                    task.task.task_conf.Chinese_params.任务类型 = task.task.task_conf.params.task_type;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.最大树数量 = task.task.task_conf.params.num_trees;
                                    task.task.task_conf.Chinese_params.树的最大深度 = task.task.task_conf.params.tree_param.max_depth;
                                    task.task.task_conf.Chinese_params.特征随机采样比率 = task.task.task_conf.params.subsample_feature_rate;
                                    task.task.task_conf.Chinese_params.n次迭代没变化是否停止 = task.task.task_conf.params.n_iter_no_change ? '是' : 'false';
                                    task.task.task_conf.Chinese_params.收敛阈值 = task.task.task_conf.params.tol;
                                    task.task.task_conf.Chinese_params.最大桶数量 = task.task.task_conf.params.bin_num;
                                    task.task.task_conf.Chinese_params.验证频次 = task.task.task_conf.params.validation_freqs;
                                    task.task.task_conf.Chinese_params.提前结束的迭代次数 = task.task.task_conf.params.early_stopping_rounds;
                                    task.task.task_conf.Chinese_params.工作模式 = task.task.task_conf.params.work_mode;
                                    task.task.task_conf.Chinese_params.L2正则项系数 = task.task.task_conf.params.tree_param.criterion_method;
                                    task.task.task_conf.Chinese_params.标准参数 = task.task.task_conf.params.tree_param.criterion_params[0];
                                    task.task.task_conf.Chinese_params.分裂一个内部节点需要的最小样本 = task.task.task_conf.params.tree_param.min_sample_split;
                                    task.task.task_conf.Chinese_params.每个叶子节点包含的最小样本数 = task.task.task_conf.params.tree_param.min_sample_split;
                                    task.task.task_conf.Chinese_params.单个拆分要达到的最小增益 = task.task.task_conf.params.tree_param.min_impurity_split;
                                    task.task.task_conf.Chinese_params.目标函数 = task.task.task_conf.params.objective_param.params[0];
                                    task.task.task_conf.Chinese_params.学习目标参数 = task.task.task_conf.params.objective_param.params[0];
                                    task.task.task_conf.Chinese_params.同态加密方法 = task.task.task_conf.params.encrypt_param.method;
                                    task.task.task_conf.Chinese_params.KFold分割次数 = task.task.task_conf.params.cv_param.n_splits;
                                    task.task.task_conf.Chinese_params.KFold之前洗牌 = task.task.task_conf.params.cv_param.shuffle ? '是' : '否';
                                    task.task.task_conf.Chinese_params.是否执行cv = task.task.task_conf.params.cv_param.need_cv ? '是' : '否';
                                    break;
                                case 'HorzSecureBoost':
                                    task.task.task_conf.Chinese_params.任务类型 = task.task.task_conf.params.task_type;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.最大树数量 = task.task.task_conf.params.num_trees;
                                    task.task.task_conf.Chinese_params.树的最大深度 = task.task.task_conf.params.tree_param.max_depth;
                                    task.task.task_conf.Chinese_params.特征随机采样比率 = task.task.task_conf.params.subsample_feature_rate;
                                    task.task.task_conf.Chinese_params.n次迭代没变化是否停止 = task.task.task_conf.params.n_iter_no_change ? '是' : 'false';
                                    task.task.task_conf.Chinese_params.收敛阈值 = task.task.task_conf.params.tol;
                                    task.task.task_conf.Chinese_params.最大桶数量 = task.task.task_conf.params.bin_num;
                                    task.task.task_conf.Chinese_params.验证频次 = task.task.task_conf.params.validation_freqs;
                                    task.task.task_conf.Chinese_params.提前结束的迭代次数 = task.task.task_conf.params.early_stopping_rounds;
                                    task.task.task_conf.Chinese_params.工作模式 = task.task.task_conf.params.work_mode;
                                    task.task.task_conf.Chinese_params.L2正则项系数 = task.task.task_conf.params.tree_param.tree_param.criterion_method;
                                    task.task.task_conf.Chinese_params.标准参数 = task.task.task_conf.params.tree_param.tree_param.criterion_params[0];
                                    task.task.task_conf.Chinese_params.每个叶子节点包含的最小样本数 = task.task.task_conf.params.tree_param.min_sample_split;
                                    task.task.task_conf.Chinese_params.单个拆分要达到的最小增益 = task.task.task_conf.params.tree_param.min_impurity_split;
                                    task.task.task_conf.Chinese_params.目标函数 = task.task.task_conf.params.objective_param.objective_param.objective;
                                    task.task.task_conf.Chinese_params.学习目标参数 = task.task.task_conf.params.objective_param.params[0];
                                    task.task.task_conf.Chinese_params.KFold分割次数 = task.task.task_conf.params.cv_param.cv_param.n_splits;
                                    task.task.task_conf.Chinese_params.KFold之前洗牌 = task.task.task_conf.params.cv_param.shuffle ? '是' : '否';
                                    task.task.task_conf.Chinese_params.是否执行cv = task.task.task_conf.params.cv_param.need_cv ? '是' : '否';
                                    break;
                                case 'MixSecureBoost':
                                    task.task.task_conf.Chinese_params.任务类型 = task.task.task_conf.params.task_type;
                                    task.task.task_conf.Chinese_params.学习率 = task.task.task_conf.params.learning_rate;
                                    task.task.task_conf.Chinese_params.最大树数量 = task.task.task_conf.params.num_trees;
                                    task.task.task_conf.Chinese_params.树的最大深度 = task.task.task_conf.params.tree_param.max_depth;
                                    task.task.task_conf.Chinese_params.特征随机采样比率 = task.task.task_conf.params.subsample_feature_rate;
                                    task.task.task_conf.Chinese_params.n次迭代没变化是否停止 = task.task.task_conf.params.n_iter_no_change ? '是' : 'false';
                                    task.task.task_conf.Chinese_params.收敛阈值 = task.task.task_conf.params.tol;
                                    task.task.task_conf.Chinese_params.最大桶数量 = task.task.task_conf.params.bin_num;
                                    task.task.task_conf.Chinese_params.验证频次 = task.task.task_conf.params.validation_freqs;
                                    task.task.task_conf.Chinese_params.提前结束的迭代次数 = task.task.task_conf.params.early_stopping_rounds;
                                    task.task.task_conf.Chinese_params.工作模式 = task.task.task_conf.params.work_mode;
                                    task.task.task_conf.Chinese_params.L2正则项系数 = task.task.task_conf.params.tree_param.criterion_method;
                                    task.task.task_conf.Chinese_params.标准参数 = task.task.task_conf.params.tree_param.criterion_params[0];
                                    task.task.task_conf.Chinese_params.分裂一个内部节点需要的最小样本 = task.task.task_conf.params.min_sample_split;
                                    task.task.task_conf.Chinese_params.每个叶子节点包含的最小样本数 = task.task.task_conf.params.tree_param.min_sample_split;
                                    task.task.task_conf.Chinese_params.单个拆分要达到的最小增益 = task.task.task_conf.params.min_impurity_split;
                                    task.task.task_conf.Chinese_params.目标函数 = task.task.task_conf.params.objective_param.params[0];
                                    task.task.task_conf.Chinese_params.学习目标参数 = task.task.task_conf.params.objective_param.params[0];
                                    task.task.task_conf.Chinese_params.同态加密方法 = task.task.task_conf.params.encrypt_param.method;
                                    task.task.task_conf.Chinese_params.KFold分割次数 = task.task.task_conf.params.cv_param.n_splits;
                                    task.task.task_conf.Chinese_params.KFold之前洗牌 = task.task.task_conf.params.cv_param.shuffle ? '是' : '否';
                                    task.task.task_conf.Chinese_params.是否执行cv = task.task.task_conf.params.need_cv ? '是' : '否';
                                    break;
                                case 'Evaluation':
                                    task.task.task_conf.Chinese_params.评估类别 = task.task.task_conf.params.eval_type;
                                    task.task.task_conf.Chinese_params.正标签类型 = task.task.task_conf.params.pos_label;
                                    break;
                                case 'VertPearson':
                                    task.task.task_conf.Chinese_params.是否联合计算相关性 = task.task.task_conf.params.cross_parties ? '是' : '否';
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.column_names;
                                    break;
                                case 'FeatureTransform':
                                    task.task.task_conf.Chinese_params.转换规则 = task.task.task_conf.params.transform_rules;
                                    break;
                                case 'HorzOneHot':
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.transform_col_names;
                                    break;
                                case 'HorzStatistic':
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.col_names;
                                    break;
                                case 'FeatureStandardized':
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.fields;
                                    break;
                                case 'HorzFeatureBinning':
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.bin_names;
                                    task.task.task_conf.Chinese_params.分箱数量 = task.task.task_conf.params.bin_num;
                                    break;
                                case 'Binning':
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.bin_names;
                                    task.task.task_conf.Chinese_params.分箱数量 = task.task.task_conf.params.bin_num;
                                    break;
                                case 'MixFeatureBinning':
                                    task.task.task_conf.Chinese_params.特征 = task.task.task_conf.params.bin_names;
                                    task.task.task_conf.Chinese_params.分箱数量 = task.task.task_conf.params.bin_num;
                                    break;
                                }
                                const value = {
                                    results: task.results || [],
                                    task:    task.task || {
                                        status: 'wait_run',
                                    },
                                    componentParaActive: '1',
                                };


                                if (task.task.task_conf.module === 'Evaluation') {
                                    if(!this.chartsList.find(each => each.job_id === task.task.job_id))
                                        this.chartsList.push({
                                            flow_id:      value.task.flow_id,
                                            flow_node_id: value.task.flow_node_id,
                                            job_id:       value.task.job_id,
                                        });
                                }

                                if(this.detailList[index]) {
                                    this.detailList[index][job.id] = value;
                                } else {
                                    this.detailList[index] = {
                                        $title:   task.task.component_name,
                                        [job.id]: value,
                                    };
                                }
                            });
                        }
                    });
                    this.$nextTick(_ => {
                        this.$bus.$emit('update-title-navigator');
                    });
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .page{margin-right: 100px;}
    .title{
        margin:0 0 20px;
        font-size: 18px;
    }
    .compare-head{
        :deep(.el-table__body-wrapper){display: none;}
    }
    .head-id{
        font-weight: normal;
        line-height: 14px;
        margin-top: 10px;
        font-size: 12px;
    }
    .el-collapse-item{
        :deep(.el-collapse-item__header):hover{background: #F5F7FA;}
    }
    .el-collapse{
        :deep(.el-collapse-item__content){padding-bottom: 0;}
    }
    .el-table{
        border-top:1px solid #EBEEF5;
        :deep(td){vertical-align: top;}
        :deep(.result){
            max-height: 1000px;
            overflow: auto;
        }
        :deep(.history-btn){display:none;}
    }
</style>
