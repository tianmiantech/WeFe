<template>
    <el-card v-loading="vData.pageLoading">
        <el-form :model="vData.form" inline>
            <el-form-item label="选择模型：">
                <el-select v-model="vData.form.model" placeholder="请选择模型" :disabled="!vData.isCanUpload">
                    <el-option v-for="item in vData.modelList" :key="item.task_id" :label="item.flow_name" :value="item.task_id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
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
                    <!-- <div v-if="vData.isUploading || vData.isCheckFinished || vData.isUploadedOk" class="predict_box" :style="{width: vData.width+'px', height: vData.sampleList.length ? 490 : 400}+'px'">
                        <p v-if="vData.isUploadedOk" class="predict_tips">{{vData.http_upload_filename ? '预测中...' : '上传中...'}}</p>
                        <div v-if="vData.isCheckFinished">
                            <label-system ref="labelSystemRef" :currentImage="vData.currentImage" :labelList="vData.count_by_sample" :for-job-type="vData.forJobType" @save-label="methods.saveCurrentLabel" />
                            <image-thumbnail-list ref="imgThumbnailListRef" :sampleList="vData.sampleList" :width="700" @select-image="methods.selectImage" />
                        </div>
                        <uploader-list v-if="vData.isUploading" :file-list="vData.img_upload_options.files.length" />
                    </div> -->
                    <div v-if="vData.isUploading" class="predict_box" :style="{width: vData.width+'px', height: vData.sampleList.length ? 490 : 400}+'px'">
                        <uploader-list :file-list="vData.img_upload_options.files.length" />
                    </div>

                    <div v-if="vData.isUploadedOk" class="predict_box" style="height: 400px;">
                        <p class="predict_tips">{{vData.http_upload_filename ? '预测中...' : '上传中...'}}</p>
                    </div>

                    <div v-if="vData.isCheckFinished" class="predict_box" :style="{width: vData.width+'px', height: vData.sampleList.length ? 490 : 400}+'px'">
                        <label-system ref="labelSystemRef" :currentImage="vData.currentImage" :labelList="vData.count_by_sample" :for-job-type="vData.forJobType" @save-label="methods.saveCurrentLabel" />
                        <image-thumbnail-list ref="imgThumbnailListRef" :sampleList="vData.sampleList" :width="700" @select-image="methods.selectImage" />
                    </div>
                    <!-- 预测结果出来后可显示上传文件按钮 -->
                    <div class="mt10">
                        <uploader-btn
                            v-if="vData.isCheckFinished"
                            :attrs="vData.img_upload_attrs"
                            :single="true"
                            class="upload_btn"
                        >
                            点击上传文件
                        </uploader-btn>
                        <el-button type="primary" class="ml10" @click="methods.downloadModel">模型下载</el-button>
                        <el-button type="primary" class="ml10" @click="methods.downloadModelFile">模型文件下载</el-button>
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
                        <el-table-column prop="category_name" label="预测标签" width="90" />
                        <el-table-column prop="score" label="得分" width="90">
                            <template v-slot="scope">
                                {{scope.row.score.toFixed(5)}}
                            </template>
                        </el-table-column>
                        <el-table-column prop="bbox" label="标注位置">
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
    import { ref, computed, reactive, getCurrentInstance, nextTick, onBeforeMount, onBeforeUnmount } from 'vue';
    import { useStore } from 'vuex';
    import { useRoute, useRouter } from 'vue-router';
    import LabelSystem from './components/model-show.vue';
    import ImageThumbnailList from '../../data-center/components/image-thumbnail-list.vue';
    export default {
        components: { LabelSystem, ImageThumbnailList },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http, $bus, $message } = appContext.config.globalProperties;
            const route = useRoute();
            const router = useRouter();
            const labelSystemRef = ref();
            const imgThumbnailListRef = ref();
            const imgUploaderRef = ref();
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
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
                isStartPredict:       false,
                sampleList:           [],
                forJobType:           'detection',
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
                isCanUpload:     true, // 是否可上传文件
                isUploading:     false, // 文件上传中
                isUploadedOk:    false, // 文件上传完成
                isCheckFinished: false, // 模型校验完成
                runningTimer:    null,
                resquestCount:   0, // 获取预测详情时result为null后继续获取的次数
                resultNullTimer: null,
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
                            }
                            vData.pageLoading = false;
                        });
                    }
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
                        vData.isUploadedOk = true;
                        vData.isUploading = false;
                        vData.isCheckFinished = false;
                        vData.isCanUpload = false;
                        methods.startPredict();
                    }
                },
                async startPredict() {
                    const { code, data } = await $http.post({
                        url:  '/model/deep_learning/call/start',
                        data: {
                            taskId:   vData.form.model,
                            filename: vData.http_upload_filename,
                        },
                    });

                    nextTick(_=> {
                        if(code === 0) {
                            if (data.file_count) {
                                vData.isStartPredict = true;
                                vData.resquestCount = 0;
                                methods.getPredictDetail();
                            }
                        } else {
                            vData.isUploadedOk = false;
                            vData.isCanUpload = true;
                        }
                    });
                },
                async getPredictDetail() {
                    // 获取预测结果 flow/job/task/detail
                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/detail',
                        data: {
                            taskId:      vData.form.model,
                            // taskId:      '822d4e06ea0346e5a3582e0a5f87ddb7_provider_PaddleDetection_16452526379674439',
                            result_type: 'infer',
                            need_result: true,
                        },
                    });

                    if(code === 0) {
                        nextTick(_=> {
                            if (data.task_view.results[0] === null) {
                                vData.isCanUpload = false;
                                vData.isUploading = false;
                                vData.isCheckFinished = false;
                                vData.isUploadedOk = true;
                                vData.http_upload_filename = 'http_upload_filename';
                                if (vData.resquestCount < 9) {
                                    clearTimeout(vData.resultNullTimer);
                                    vData.resultNullTimer = setTimeout(() => {
                                        methods.getPredictDetail();
                                        vData.resquestCount++;
                                    }, 3000);
                                } else {
                                    $message.error('预测服务异常：无预测记录，请联系管理员检查预测服务是否正常。');
                                    vData.isCanUpload = true;
                                    vData.isUploading = false;
                                    vData.isCheckFinished = false;
                                    vData.isUploadedOk = false;
                                }
                            }
                            if (data.task_view.results[0] !== null && data.task_view.results[0].result.status === 'finish' && data.task_view.results[0].result.result.length) {
                                vData.totalResultCount = data.task_view.results[0].result.result.length;
                                const list = data.task_view.results[0].result.result;

                                for (let i=0; i<list.length; i++) {
                                    list[i].bbox_results = list[i].bbox_results.filter(item => {
                                        if (item.score > 0.5) {
                                            return item;
                                        }
                                    });
                                }
                                vData.sampleList = [];
                                vData.isCheckFinished = false;
                                vData.isCanUpload = false;
                                list.forEach((item, idx) => {
                                    methods.downSingleImage(item.image, idx, item);
                                });
                            }
                            if (data.task_view.results[0] !== null && data.task_view.results[0].result.status === 'running') {
                                vData.runningTimer = setTimeout(() => {
                                    methods.getPredictDetail();
                                }, 3000);
                            }
                        });
                    }
                },
                async downSingleImage(img, idx, item) {
                    vData.pageLoading = true;
                    const { code, data } = await $http.get({
                        url:          '/model/deep_learning/call/download/image',
                        params:       { filename: img,  task_id: vData.form.model },
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
                                    vData.pageLoading = false;
                                }, 1000);
                                methods.resetWidth();
                            }
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

                        console.log(document.getElementsByClassName('upload_box')[0].offsetWidth);
                        console.log(maxWidth);
                        labelSystemRef.value.vData.width = maxWidth;
                        vData.width = maxWidth;
                        imgThumbnailListRef.value.vData.width = document.getElementsByClassName('upload_box')[0].offsetWidth;
                        labelSystemRef.value.methods.createStage();
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
                async downloadModelFile(){
                    const api = `${window.api.baseUrl}/model/deep_learning/call/download/zip?task_id=${vData.form.model}&token=${userInfo.value.token}`;
                    const link = document.createElement('a');

                    link.href = api;
                    link.target = '_blank';
                    link.style.display = 'none';
                    document.body.appendChild(link);
                    link.click();
                },
            };

            onBeforeMount(()=> {
                methods.getModelList();
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
                clearTimeout(vData.timer);
                clearTimeout(vData.resetWidthTimer);
                clearTimeout(vData.runningTimer);
                clearTimeout(vData.resultNullTimer);
                window.onresize = null;
            });

            return {
                vData,
                methods,
                labelSystemRef,
                imgThumbnailListRef,
                imgUploaderRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
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
