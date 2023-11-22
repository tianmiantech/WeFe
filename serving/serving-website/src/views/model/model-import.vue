<template>
    <el-card
        v-loading="loading"
        shadow="never"
        class="page"
    >
        <el-form
            :model="form"
            label-position="top"
            style="width: 400px;"
        >
            <el-form-item
                prop="name"
                label="模型名称："
                :rules="[{required: true, message: '请输入模型名称'}]"
            >
                <el-input
                    v-model="form.name"
                    placeholder="请输入模型名称"
                    clearable
                />
            </el-form-item>
            <el-form-item label="模型类型：">
                <el-select v-model="file_upload_options.query.fileType">
                    <el-option
                        value="MachineLearningModelFile"
                        label="机器学习模型"
                    />
                    <el-option
                        value="DeepLearningModelFile"
                        label="深度学习模型"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                label="选择文件："
                required
            >
                <uploader
                    ref="uploaderRef"
                    :options="file_upload_options"
                    :list="file_upload_options.files"
                    :file-status-text="fileStatusText"
                    @file-complete="fileUploadComplete"
                    @file-removed="fileRemoved"
                    @file-added="fileAdded"
                >
                    <uploader-unsupport />
                    <uploader-drop v-if="file_upload_options.files.length === 0">
                        <p class="mb10">将文件（.json）拖到此处</p>或
                        <uploader-btn
                            :attrs="{accept: '.json'}"
                            :single="true"
                        >
                            点击上传
                        </uploader-btn>
                    </uploader-drop>
                    <uploader-list :file-list="file_upload_options.files.length" />
                </uploader>
            </el-form-item>

            <el-button
                :disabled="!form.name || !form.filename"
                type="primary"
                @click="submit"
            >
                提交
            </el-button>
        </el-form>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        data() {
            return {
                loading: false,
                form:    {
                    name:     '',
                    filename: '',
                },
                file_upload_options: {
                    files:               [],
                    target:              window.api.baseUrl + '/file/upload',
                    singleFile:          true,
                    // chunks check
                    testChunks:          true,
                    chunkSize:           8 * 1024 * 1024,
                    simultaneousUploads: 4,
                    headers:             {
                        token: '',
                    },
                    query: {
                        fileType: 'MachineLearningModelFile',
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
                fileStatusText: {
                    success:   '成功',
                    error:     '错误',
                    uploading: '上传中',
                    paused:    '已暂停',
                    waiting:   '等待中',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.file_upload_options.headers.token = this.userInfo.token;
        },
        methods: {
            fileAdded(file) {
                this.file_upload_options.files = [file];
            },
            fileRemoved() {
                this.file_upload_options.files = [];
            },
            async fileUploadComplete(e) {
                this.loading = true;

                const { code, data } = await this.$http.get({
                    url:     '/file/merge',
                    timeout: 1000 * 60 * 2,
                    params:  {
                        filename:         e.file.name,
                        uniqueIdentifier: e.uniqueIdentifier,
                        fileType:         this.file_upload_options.query.fileType,
                    },
                });

                this.loading = false;
                if (code === 0) {
                    this.form.filename = data.filename;
                } else {
                    this.fileRemoved();
                    this.$refs.uploaderRef.uploader.cancel();
                }
            },
            async submit(ev) {
                    const { code } = await this.$http.post({
                        url:  '/model/import',
                        data: {
                            name:       this.form.name,
                            filename:   this.form.filename,
                            model_type: this.file_upload_options.query.fileType === 'MachineLearningModelFile' ? 'MachineLearning' : 'DeepLearning',
                        },
                        btnState: {
                            target: ev,
                        },
                    });

                    if (code === 0) {
                        this.$message.success('模型导入成功!');
                        this.$router.replace({ name: 'index' });
                    }
            },
        },
    };
</script>

<style lang="scss">
    .uploader {
        position: relative;
        min-width: 380px;
    }
    .uploader-drop {border-radius: 5px;}
    .uploader-btn {background: #fff;}
    .uploader-list{
        .uploader-file-actions {
            min-width: 60px;
            .uploader-file-remove{display: block !important;}
        }
        .uploader-file-status{
            font-size: 12px;
            text-indent: 2px;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
        }
        .uploader-file-meta {display: none;}
    }
</style>
