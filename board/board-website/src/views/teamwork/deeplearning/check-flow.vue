<template>
    <el-card v-loading="vData.pageLoading">
        <el-form :model="vData.form" inline>
            <el-form-item label="选择模型：">
                <el-select v-model="vData.form.model" placeholder="请选择模型" :disabled="!vData.isCanUpload" @change="methods.modelChange">
                    <el-option v-for="item in vData.modelList" :key="item.task_id" :label="item.flow_name" :value="item.task_id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <div class="model_info">
            <div class="model_params mr10">
                <el-divider content-position="center">模型参数</el-divider>
                <div v-if="vData.algorithm_config" class="model_params_box">
                    <h3>算法参数信息：</h3>
                    <el-row>
                        <el-col :span="8"><span>算法类型：{{vData.algorithm_config.program}}</span></el-col>
                        <el-col :span="8"><span>模型名称：{{vData.algorithm_config.architecture}}</span></el-col>
                        <el-col :span="8"><span>迭代次数：{{vData.algorithm_config.max_iter}}</span></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="8"><span>聚合步长：{{vData.algorithm_config.inner_step}}</span></el-col>
                        <el-col :span="8"><span>学习率：{{vData.algorithm_config.base_lr}}</span></el-col>
                        <el-col :span="8"><span>图片通道数：{{vData.algorithm_config.image_shape[0] === 3 ? '彩色' : '黑色'}}（{{vData.algorithm_config.image_shape[0]}}）</span></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="8"><span>图片宽度( px )：{{vData.algorithm_config.image_shape[1]}}</span></el-col>
                        <el-col :span="8"><span>图片高度( px )：{{vData.algorithm_config.image_shape[2]}}</span></el-col>
                        <el-col :span="8"><span>批量大小：{{vData.algorithm_config.batch_size}}</span></el-col>
                    </el-row>
                </div>
                <div v-if="vData.data_set" class="model_params_box">
                    <h3>我方数据集信息：</h3>
                    <el-row>
                        <el-col :span="12"><span>数据集名称：{{vData.data_set.name}}</span></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="24"><span>数据集ID：{{vData.data_set.data_resource_id}}</span></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12"><span>已标注/样本量：{{ vData.data_set.labeled_count }} / {{ vData.data_set.total_data_count }}</span></el-col>
                        <el-col :span="12"><span>标签个数：{{ vData.data_set.label_list.split(',').length }}</span></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12"><span>已标注/样本量：{{ vData.data_set.labeled_count }} / {{ vData.data_set.total_data_count }}</span></el-col>
                        <el-col :span="12"><span>样本分类：{{ vData.data_set.for_job_type === 'classify' ? '图像分类' : vData.data_set.for_job_type === 'detection' ? '目标检测' : '-' }}</span></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="24">标签列表：
                            <template v-for="item in vData.data_set.label_list.split(',')" :key="item">
                                <el-tag
                                    v-show="item"
                                    class="mr5 mb10"
                                    type="info"
                                    effect="plain"
                                >
                                    {{ item }}
                                </el-tag>
                            </template>
                        </el-col>
                    </el-row>
                </div>
                <div v-if="vData.members" class="model_params_box">
                    <h3>成员信息：</h3>
                    <el-row v-for="item in vData.members" :key="item.member_id">
                        <el-row>
                            <el-col :span="24"><span>成员ID：{{item.member_id}}</span></el-col>
                        </el-row>
                        <el-col :span="12"><span>成员名称：<el-tag>{{item.member_name}}</el-tag></span></el-col>
                        <el-col :span="12"><span>成员角色：{{item.member_role === 'promoter' ? '发起方' : '协作方'}}</span></el-col>
                    </el-row>
                </div>
            </div>
            <div class="model_result" v-loading="vData.lossLoading">
                <el-divider content-position="center">模型结果</el-divider>
                <div class="model_result_box" style="height: 400px;">
                    <el-tabs v-model="vData.activeTab" @tab-click="methods.tabChange">
                        <el-tab-pane label="Loss" name="loss"></el-tab-pane>
                        <el-tab-pane v-if="vData.forJobType === 'PaddleClassify'" label="Accuracy" name="accuracy"></el-tab-pane>
                    </el-tabs>
                    <LineChart
                        v-if="vData.isshow"
                        ref="LineChart"
                        :config="vData.train_loss"
                    />
                </div>
            </div>
        </div>
        <el-divider content-position="center">
            模型校验
        </el-divider>
        <div class="opearate_box">
            <div class="upload_box" :style="{width: vData.width+'px'}">
                <uploader
                    ref="imgUploaderRef"
                    :options="vData.img_upload_options"
                    :file-status-text="vData.fileStatusText"
                    :list="vData.files"
                    :show-file-list="false"
                    @file-complete="methods.fileUploadCompleteImage"
                    @file-removed="methods.fileRemovedImage"
                    @file-added="methods.fileAddedImage"
                    @file-progress="methods.fileProgress"
                >
                    <uploader-unsupport />
                    <uploader-drop v-if="vData.isCanUpload">
                        <p><el-icon class="el-icon--upload" style="color: #bfbfbf; font-size: 70px;"><elicon-upload-filled /></el-icon></p>
                        <uploader-btn
                            :attrs="vData.img_upload_attrs"
                            :single="true"
                            class="upload_btn mt10 mb10"
                        >
                            点击上传文件
                        </uploader-btn>
                        <p class="mb10">或将文件 (.zip .tar .tgz .7z .png .jpg .jpeg) 拖到此处</p>
                    </uploader-drop>
                    <div v-if="vData.isUploading" class="predict_box" :style="{width: vData.width+'px', height: vData.sampleList.length ? 490 : 400}+'px'">
                        <uploader-list :file-list="vData.img_upload_options.files.length" />
                    </div>

                    <div v-if="vData.isUploadedOk" class="predict_box" style="height: 400px;">
                        <p class="predict_tips">{{vData.isChecking ? '检测中...' : vData.isPredicting ? '预测中...' : '上传中...'}}</p>
                    </div>

                    <div v-if="vData.isCheckFinished" class="predict_box" :style="{width: vData.width+'px', height: vData.sampleList.length ? 490 : 400}+'px'">
                        <label-system ref="labelSystemRef" :currentImage="vData.currentImage" :labelList="vData.count_by_sample" :for-job-type="vData.forJobType === 'PaddleDetection' ? 'detection' : 'classify'" @save-label="methods.saveCurrentLabel" />
                        <image-thumbnail-list ref="imgThumbnailListRef" :sampleList="vData.sampleList" :width="700" @select-image="methods.selectImage" />
                    </div>
                    <!-- 预测结果出来后可显示上传文件按钮 -->
                    <div class="mt10">
                        <uploader-btn
                            v-if="vData.isCheckFinished"
                            :attrs="vData.img_upload_attrs"
                            :single="true"
                            class="upload_btn mr10"
                        >
                            点击上传文件
                        </uploader-btn>
                        <el-button type="primary" @click="methods.downloadModel">模型下载</el-button>
                        <el-button v-if="vData.isPredicting" type="primary" @click="methods.interruptPrediect">中断当前预测</el-button>
                        <router-link
                            :to="{ name: 'project-deeplearning-flow', query: {project_id: vData.projectId, flow_id: vData.flowId, training_type: vData.forJobType === 'PaddleDetection' ? 'detection' : vData.forJobType === 'PaddleClassify' ? 'paddle_clas' : '' } }"
                        >
                            <el-button plain class="ml10">查看流程</el-button>
                        </router-link>
                    </div>
                </uploader>
            </div>
            <div class="show_box ml10" style="width: 430px;">
                <div class="result_table">
                    <el-table
                        :data="vData.currentImage.item.bbox_results"
                        border
                        style="width: 100%;"
                        :max-height="488"
                        :header-cell-style="{background:'#f7f7f7',color:'#606266'}"
                    >
                        <template #empty>
                            <div class="empty f14">没有满足条件的识别结果</div>
                        </template>
                        <el-table-column prop="category_name" label="预测标签" />
                        <el-table-column prop="score" label="置信度">
                            <template v-slot="scope">
                                {{scope.row.score.toFixed(5)}}
                            </template>
                        </el-table-column>
                        <el-table-column v-if="vData.forJobType==='PaddleDetection'" prop="bbox" label="标注位置" width="240">
                            <template v-slot="scope">
                                <template
                                    v-for="(item, index) in scope.row.bbox"
                                    :key="index"
                                >
                                    <span v-if="index ===0">x1:{{item.toFixed(2)}}, </span>
                                    <span v-if="index ===1">y1:{{item.toFixed(2)}}; </span>
                                    <span v-if="index ===2">x2:{{item.toFixed(2)}}, </span>
                                    <span v-if="index ===3">x2:{{item.toFixed(2)}}</span>
                                </template>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </div>
    </el-card>
</template>

<script>
    import { ref, computed, reactive, getCurrentInstance, nextTick, onBeforeMount, onBeforeUnmount, onMounted } from 'vue';
    import { useStore } from 'vuex';
    import { useRoute, useRouter } from 'vue-router';
    import LabelSystem from './components/model-show.vue';
    import ImageThumbnailList from '../../data-center/components/image-thumbnail-list.vue';
    export default {
        components: { LabelSystem, ImageThumbnailList },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http, $bus, $message, $confirm } = appContext.config.globalProperties;
            const route = useRoute();
            const router = useRouter();
            const labelSystemRef = ref();
            const imgThumbnailListRef = ref();
            const imgUploaderRef = ref();
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const LineChart = ref();
            const vData = reactive({
                projectId: route.query.project_id,
                flowId:    route.query.flow_id,
                form:      {
                    model: '',
                },
                modelList:          [],
                img_upload_options: {
                    files:               [],
                    target:              window.api.baseUrl + '/file/upload?uploadFileUseType=CallDeepLearningModel',
                    singleFile:          true,
                    // chunks check
                    testChunks:          true,
                    chunkSize:           8 * 1024 * 1024,
                    simultaneousUploads: 4,
                    headers:             {
                        token: JSON.parse(localStorage.getItem(window.api.baseUrl + '_userInfo')).token,
                    },
                    parseTimeRemaining (timeRemaining, parsedTimeRemaining) {
                        return parsedTimeRemaining
                            .replace(/\syears?/, '年')
                            .replace(/\days?/, '天')
                            .replace(/\shours?/, '小时')
                            .replace(/\sminutes?/, '分钟')
                            .replace(/\sseconds?/, '秒');
                    },
                },
                img_upload_attrs: {
                    accept: 'application/zip, application/x-rar-compressed, application/x-tar, application/x-7z-compressed, .jpg,.png,.jpeg', // zip, rar, tar, 7z
                },
                fileStatusText: {
                    success:   '成功',
                    error:     '错误',
                    uploading: '上传中',
                    paused:    '已暂停',
                    waiting:   '等待中',
                },
                loading:              false,
                files:                [],
                http_upload_filename: '',
                sampleList:           [],
                forJobType:           'PaddleDetection',
                timer:                null,
                resetWidthTimer:      null,
                width:                700,
                pageLoading:          false,
                totalResultCount:     0,
                currentImage:         {
                    item: {
                        bbox_results: [],
                    },
                },
                isCanUpload:      true, // 是否可上传文件
                isUploading:      false, // 文件上传中
                isUploadedOk:     false, // 文件上传完成
                isCheckFinished:  false, // 模型校验完成
                runningTimer:     null,
                resquestCount:    0, // 获取预测详情时result为null后继续获取的次数
                resultNullTimer:  null,
                isPredicting:     false, // 是否处于预测中
                isChecking:       false, // 是否处于检测是否有正在预测的任务中
                infer_session_id: '',
                algorithm_config: null,
                activeTab:        'loss',
                train_loss:       {
                    xAxis:  [],
                    series: [[]],
                },
                isshow:      false,
                lossLoading: false,
                data_set:    null,
                members:     null,
            });
            const methods = {
                async getModelList() {
                    vData.pageLoading = true;
                    const { code, data } = await $http.post({
                        url:  '/project/modeling/query',
                        data: {
                            projectId: vData.projectId,
                            flowId:    vData.flowId,
                        },
                    });

                    if(code === 0) {
                        nextTick(_=> {
                            if (data && data.list.length) {
                                vData.modelList = data.list;
                                vData.form.model = data.list[0].task_id;
                                methods.getPredictDetail();
                                methods.getModelResult();
                            }
                            vData.pageLoading = false;
                        });
                    }
                },
                async getModelResult() {
                    vData.lossLoading = true;
                    const params = {
                        task_id: vData.form.model,
                        type:    vData.activeTab,
                    };

                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/result',
                        data: params,
                    });

                    nextTick(_=> {
                        if (code === 0 && data) {
                            vData.forJobType = data[0].component_type;
                            vData.algorithm_config = data[0].task_config.algorithm_config;
                            vData.data_set = data[0].task_config.data_set;
                            vData.members = data[0].task_config.members;
                            methods.showResult(data);
                        }
                        vData.lossLoading = false;
                    });
                },
                showResult(data) {
                    vData.train_loss = {
                        xAxis:  [],
                        series: [[]],
                    };
                    if(data[0].result) {
                        vData.isshow = true;
                        const train_loss = data[0].result.data;

                        for (const key in train_loss) {
                            vData.train_loss.xAxis.push(key);
                            vData.train_loss.series[0].push(train_loss[key].value);
                        }
                    }
                },
                modelChange() {
                    methods.getModelResult();
                    vData.isshow = false;
                },
                tabChange(val) {
                    vData.activeTab = val.paneName;
                    methods.getModelResult();
                    vData.isshow = false;
                },
                fileAddedImage(file) {
                    // split考虑文件名中有.，随机数文件名以清除文件缓存
                    vData.img_upload_options.files = [file];
                    vData.isCanUpload = false;
                    vData.isCheckFinished = false;
                    vData.isUploadedOk = false;
                    vData.isUploading = true;
                    vData.currentImage = {
                        item: {
                            bbox_results: [],
                        },
                    };
                },
                fileRemovedImage() {
                    vData.img_upload_options.files = [];
                    vData.isCheckFinished = false;
                    vData.isUploadedOk = false;
                    vData.isCanUpload = true;
                },
                fileProgress(file) {
                    // vData.progress = Number((file._prevProgress * 100).toFixed(1));
                    vData.isCanUpload = false;
                },
                async fileUploadCompleteImage() {
                    vData.loading = true;
                    const file = arguments[0].file;

                    vData.img_upload_options.headers.token = JSON.parse(localStorage.getItem(window.api.baseUrl + '_userInfo')).token;
                    const { code, data } = await $http.get({
                        url:     '/file/merge',
                        timeout: 1000 * 60 * 2,
                        params:  {
                            filename:          file.name,
                            uniqueIdentifier:  arguments[0].uniqueIdentifier,
                            uploadFileUseType: 'CallDeepLearningModel',
                        },
                    })
                        .catch(err => {
                            console.log(err);
                        });

                    vData.loading = false;
                    if (code === 0) {
                        vData.http_upload_filename = data.filename;
                        vData.isUploading = false;
                        vData.isCheckFinished = false;
                        vData.isCanUpload = false;
                        vData.isUploadedOk = true;
                        methods.startPredict();
                    }
                },
                async startPredict() {
                    const { code } = await $http.post({
                        url:  '/model/deep_learning/infer/start',
                        data: {
                            taskId:   vData.form.model,
                            filename: vData.http_upload_filename,
                        },
                    });

                    nextTick(_=> {
                        if(code === 0) {
                            vData.resquestCount = 0;
                            methods.getPredictDetail();
                        } else {
                            vData.isUploadedOk = false;
                            vData.isCanUpload = true;
                        }
                    });
                },
                async getPredictDetail() {
                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/detail',
                        data: {
                            taskId:      vData.form.model,
                            result_type: 'infer',
                            need_result: true,
                        },
                    });

                    if(code === 0) {
                        nextTick(_=> {
                            vData.infer_session_id = data.task_view.results[0] ? data.task_view.results[0].result.infer_session_id : '';
                            // 需考虑中断预测任务之后的状态 || data.task_view.results[0].result.status === 'stopped'
                            if (data.task_view.results[0] === null) {
                                vData.isCanUpload = false;
                                vData.isUploading = false;
                                vData.isCheckFinished = false;
                                vData.isUploadedOk = true;
                                vData.isChecking = true;
                                if (vData.resquestCount < 2) {
                                    clearTimeout(vData.resultNullTimer);
                                    vData.resultNullTimer = setTimeout(() => {
                                        methods.getPredictDetail();
                                        vData.resquestCount++;
                                    }, 1000);
                                } else {
                                    $message.error('当前没有预测中的推理任务，请上传文件开始推理。');
                                    vData.isCanUpload = true;
                                    vData.isUploading = false;
                                    vData.isCheckFinished = false;
                                    vData.isUploadedOk = false;
                                    vData.isChecking = false;
                                }
                            }
                            if (data.task_view.results[0] !== null && data.task_view.results[0].result.status === 'finish' && data.task_view.results[0].result.result.length) {
                                vData.totalResultCount = data.task_view.results[0].result.result.length;
                                const list = data.task_view.results[0].result.result;

                                if (vData.forJobType === 'PaddleDetection') { // 目标检测
                                    for (let i=0; i<list.length; i++) {
                                        list[i].bbox_results = list[i].bbox_results.filter(item=>item.score > 0.5);
                                    }
                                } else if (vData.forJobType === 'PaddleClassify') { // 图像分类
                                    for (let i=0; i<list.length; i++) {
                                        // list[i].bbox_results = list[i].infer_probs.filter(item=>item.prob > 0.5);
                                        list[i].bbox_results = list[i].infer_probs;
                                        list[i].bbox_results = list[i].bbox_results.map(item => {
                                            return {
                                                category_id:   item.class_id,
                                                category_name: item.class_name,
                                                score:         item.prob,
                                            };
                                        });
                                    }
                                }
                                vData.sampleList = [];
                                vData.isCheckFinished = false;
                                vData.isCanUpload = false;
                                list.forEach((item, idx) => {
                                    methods.downSingleImage(item.image, idx, item);
                                });
                                vData.isPredicting = false;
                            }
                            if (data.task_view.results[0] !== null && (data.task_view.results[0].result.status === 'running' || data.task_view.results[0].result.status === 'wait_run')) {
                                vData.isCanUpload = false;
                                vData.isUploading = false;
                                vData.isCheckFinished = false;
                                vData.isUploadedOk = true;
                                vData.isPredicting = true;
                                vData.runningTimer = setTimeout(() => {
                                    methods.getPredictDetail();
                                }, 3000);
                            }
                            console.log(vData.forJobType);
                        });
                    }
                },
                async downSingleImage(img, idx, item) {
                    vData.pageLoading = true;
                    const { code, data } = await $http.get({
                        url:          '/model/deep_learning/call/download/image',
                        params:       { filename: img,  task_id: vData.form.model, infer_session_id: vData.infer_session_id },
                        responseType: 'blob',
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            const url = window.URL.createObjectURL(data);

                            if (img === item.image) {
                                item.img_src = url;
                                item.$isselected = false;
                            }
                            vData.sampleList.push(item);
                            vData.sampleList[0].$isselected = true;
                            vData.currentImage = { item: vData.sampleList[0], idx: 0 };
                            vData.isCheckFinished = true;
                            vData.isUploadedOk = false;
                            vData.isUploading = false;
                            if (vData.sampleList.length === vData.totalResultCount) {
                                setTimeout(_=> {
                                    labelSystemRef.value.methods.createStage();
                                }, 1000);
                                methods.resetWidth();
                            }
                            vData.pageLoading = false;
                        }
                    });
                },
                selectImage(item, idx) {
                    vData.currentImage = { item, idx };
                    nextTick(_=> {
                        labelSystemRef.value.methods.createStage();
                        vData.sampleList.forEach(i => {
                            i.$isselected = false;
                        });
                        vData.sampleList[idx].$isselected = true;
                    });
                },
                resetWidth() {
                    if (vData.sampleList.length) {
                        const maxWidth = document.getElementsByClassName('opearate_box')[0].offsetWidth - 500;

                        vData.width = maxWidth;
                        setTimeout(_=> {
                            labelSystemRef.value.vData.width = maxWidth;
                            imgThumbnailListRef.value.vData.width = document.getElementsByClassName('upload_box')[0].offsetWidth;
                            labelSystemRef.value.methods.createStage();
                        }, 1000);
                    }
                },
                debounce(){
                    if(vData.timer) clearTimeout(vData.timer);
                    vData.timer = setTimeout(() => {
                        methods.resetWidth();
                    }, 300);
                },
                async downloadModel(){
                    const api = `${window.api.baseUrl}/model/deep_learning/download?task_id=${vData.form.model}&token=${userInfo.value.token}`;
                    const link = document.createElement('a');

                    link.href = api;
                    link.target = '_blank';
                    link.style.display = 'none';
                    document.body.appendChild(link);
                    link.click();
                },
                async interruptPrediect() {
                    $confirm('确定中断当前预测？', '警告', {
                        type: 'warning',
                    })
                        .then(async action => {
                            if(action === 'confirm') {
                                const { code } = await $http.post({
                                    url:  '/model/deep_learning/infer/stop',
                                    data: {
                                        taskId: vData.form.model,
                                    },
                                });

                                nextTick(_=> {
                                    if(code === 0) {
                                        $message.success('操作成功！');
                                        vData.isCanUpload = true;
                                        vData.isUploading = false;
                                        vData.isUploadedOk = false;
                                        vData.isCheckFinished = false;
                                        vData.isPredicting = false;
                                    }
                                });
                            }
                        });


                },
                changeHeaderTitle() {
                    if(route.meta.titleParams) {
                        const htmlTitle = `<strong>${route.query.project_name}</strong> - ${route.query.flow_name} (${vData.forJobType === 'PaddleDetection' ? '目标检测' : vData.forJobType === 'PaddleClassify' ? '图像分类' : ''})`;

                        $bus.$emit('change-layout-header-title', { meta: htmlTitle });
                    }
                },
            };

            onBeforeMount(()=> {
                methods.getModelList();
                methods.changeHeaderTitle();
                if (vData.resetWidthTimer) clearTimeout(vData.resetWidthTimer);
                vData.resetWidthTimer = setTimeout(_=> {
                    methods.resetWidth();
                }, 200);
                window.onresize = () => {
                    methods.debounce();
                };
                $bus.$on('history-backward', () => {
                    router.push({
                        name:  'project-detail',
                        query: {
                            project_id: vData.projectId,
                        },
                    });
                });
            });

            onBeforeUnmount(_ => {
                $bus.$off('history-backward');
                $bus.$off('change-layout-header-title');
                clearTimeout(vData.timer);
                clearTimeout(vData.resetWidthTimer);
                clearTimeout(vData.runningTimer);
                clearTimeout(vData.resultNullTimer);
                window.onresize = null;
            });

            onMounted(_=> {
                window.onresize = () => {
                    LineChart.value && LineChart.value.chartResize();
                };
            });

            return {
                vData,
                methods,
                labelSystemRef,
                imgThumbnailListRef,
                imgUploaderRef,
                LineChart,
            };
        },
    };
</script>

<style lang="scss" scoped>
.model_info {
    // border: 1px dashed #e4e7ed;
    display: flex;
    justify-content: space-between;
    >div {
        flex: 1;
    }
    .model_params_box {
        padding: 0 20px;
        font-size: 14px;
        h3 {
            font-size: 14px;
            font-weight: 600;
            margin: 5px 0;
        }
        .el-row {
            line-height: 28px;
        }
    }
    .model_result_box {
        .el-tabs {
            margin-bottom: -50px;
        }
    }
}
.opearate_box {
    display: flex;
    .upload_box {
        // width: 700px;
        flex: 1;
        .uploader-drop {
            height: 400px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .upload_btn {
            background: #438bff;
            color: #fff;
            border: none;
            padding: 6px 14px;
            border-radius: 2px;
            font-size: 14px;
        }
        .predict_box {
            // width: 700px;
            // height: 490px;
            overflow-y: hidden;
            border: 1px dashed #ccc;
            background: #f5f5f5;
            position: relative;
            .predict_tips {
                width: 100%;
                height: 100%;
                background: #f5f5f5;
                position: absolute;
                z-index: 2;
                display: flex;
                justify-content: center;
                align-items: center;
            }
            :deep(.uploader-list) {
                .uploader-file {
                    height: 400px;
                    line-height: 400px;
                    .uploader-file-size,
                    .uploader-file-icon,
                    .uploader-file-status,
                    .uploader-file-actions {
                        vertical-align: middle;
                        >span {
                            margin-top: 190px;
                        }
                    }
                }
            }
        }
    }
}
</style>
