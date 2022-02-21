<template>
    <el-card>
        <el-form :model="vData.form" inline>
            <el-form-item label="选择模型：">
                <el-select v-model="vData.form.model" placeholder="请选择模型">
                    <el-option v-for="item in vData.modelList" :key="item.task_id" :label="item.flow_name" :value="item.task_id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <div class="opearate_box">
            <div class="upload_box">
                <uploader
                    ref="imgUploaderRef"
                    :options="vData.img_upload_options"
                    :file-status-text="vData.fileStatusText"
                    :list="vData.files"
                    @file-complete="methods.fileUploadCompleteImage"
                    @file-removed="methods.fileRemovedImage"
                    @file-added="methods.fileAddedImage"
                    @file-progress="methods.fileProgress"
                >
                    <uploader-unsupport />
                    <uploader-drop v-if="vData.img_upload_options.files.length === 0">
                        <p><el-icon class="el-icon--upload" style="color: #bfbfbf; font-size: 70px;"><elicon-upload-filled /></el-icon></p>
                        <uploader-btn
                            :attrs="vData.img_upload_attrs"
                            :single="true"
                            class="upload_btn"
                        >
                            点击上传文件
                        </uploader-btn>
                        <p class="mb10">或将文件 (.zip .tar .tgz .7z .png .jpg .jpeg) 拖到此处</p>
                    </uploader-drop>
                    <div v-if="vData.img_upload_options.files.length" class="predict_box">
                        <p v-if="vData.http_upload_filename.length" class="predict_tips">{{vData.http_upload_filename ? '预测中...' : '上传中...'}}</p>
                        <uploader-list v-else :file-list="vData.img_upload_options.files.length" />
                    </div>
                    <!-- 预测结果出来后可显示上传文件按钮 -->
                    <uploader-btn
                        v-if="vData.img_upload_options.files.length && vData.http_upload_filename.length"
                        :attrs="vData.img_upload_attrs"
                        :single="true"
                        class="upload_btn"
                    >
                        点击上传文件
                    </uploader-btn>
                </uploader>
            </div>
            <div class="show_box">
                模型预测展示区域
            </div>
        </div>
    </el-card>
</template>

<script>
    import { reactive, getCurrentInstance, nextTick, onBeforeMount } from 'vue';
    import { useRoute } from 'vue-router';
    export default {
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const route = useRoute();
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
                    accept: 'application/zip, application/x-rar-compressed, application/x-tar, application/x-7z-compressed, application/image', // zip, rar, tar, 7z
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
            });
            const methods = {
                async getModelList() {
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
                            }
                        });
                    }
                },
                // Image
                fileAddedImage(file) {
                    vData.img_upload_options.files = [file];
                },
                fileRemovedImage() {
                    vData.img_upload_options.files = [];
                },
                fileProgress(file) {
                    vData.progress = Number((file._prevProgress * 100).toFixed(1));
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
                        console.log(vData.http_upload_filename);
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

                    if(code === 0) {
                        nextTick(_=> {
                            if (data.file_count) {
                                vData.isStartPredict = true;
                                methods.getPredictDetail();
                            }
                        });
                    }
                },
                async getPredictDetail() {
                    // 获取预测结果 flow/job/task/detail
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
                            console.log(data);
                            console.log(data.task_view.results.length);
                            // if (data.task_view.results)
                            // setTimeout(() => {
                            //     methods.getPredictDetail();
                            // }, 1000);
                        });
                    }
                },
                // 下载单张原始图片 /model/deep_learning/call/download/image
            };

            onBeforeMount(()=> {
                methods.getModelList();
            });

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
.opearate_box {
    display: flex;
    .upload_box {
        width: 700px;
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
            margin: 10px 0;
            padding: 6px 14px;
            border-radius: 2px;
            font-size: 14px;
        }
        .predict_box {
            width: 700px;
            height: 400px;
            background: #f5f5f5;
            .predict_tips {
                height: 100%;
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
