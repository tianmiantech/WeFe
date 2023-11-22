<template>
    <div v-loading="loading" class="page">
        <el-card shadow="never">
            <h3>企业实名认证 <span v-if="form.realNameAuth === 1" class="f14 color-danger">(审核中)</span></h3>
            <el-form
                class="mt20"
                :model="form"
                @submit.prevent
            >
                <!-- :disabled="!userInfo.super_admin_role" -->
                <el-form-item
                    prop="principalName"
                    :rules="[{required: true, message: '名称必填!'}]"
                    label="企业名称及类型："
                >
                    <el-input
                        v-model.trim="form.principalName"
                        :rules="[{required: true, message: '类型必选!'}]"
                        placeholder="请填写企业名称"
                    >
                        <template #append>
                            <el-select
                                v-model="form.authType"
                                style="width: 130px;"
                                placeholder="选择企业类型"
                            >
                                <el-option
                                    v-for="item in options"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
                                />
                            </el-select>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="企业简介：">
                    <el-input
                        v-model="form.description"
                        type="textarea"
                        show-word-limit
                        maxlength="300"
                        :rows="4"
                    />
                </el-form-item>
                <el-form-item :rules="[{required: true, message: '认证文件必传!'}]">
                    <template #label>
                        上传认证文件：
                        <el-alert
                            class="text-l"
                            :closable="false"
                            type="info"
                        >
                            1. 请上传已盖贵司公章的认证文件。（<el-link type="primary" @click="downloadFile">下载认证文件 </el-link>）
                            <p>2. 请上传贵司的营业执照、组织机构代码证等相关证明文件。</p>
                            文件格式支持：图片、pdf （大小10MB内）
                        </el-alert>
                    </template>

                    <el-upload
                        ref="uploader"
                        class="el-uploader"
                        v-loading="pending"
                        :file-list="fileList"
                        :on-remove="onRemove"
                        :on-preview="onPreview"
                        :http-request="() => {}"
                        :before-upload="beforeUpload"
                        :headers="{ token: userInfo.token }"
                        accept=".png,.jpg,.pdf"
                        action="#"
                        drag
                    >
                        <el-icon class="el-icon--upload">
                            <elicon-upload-filled />
                        </el-icon>
                        <div>
                            将文件 (.jpg/png/PDF) 拖拽到此处或<p><el-button type="primary">点此上传</el-button>
                            </p>
                            <div class="el-upload__tip">jpg/png 文件最大 5M, PDF 最大 10M</div>
                        </div>
                    </el-upload>
                </el-form-item>
            </el-form>

            <el-button
                v-loading="submitting"
                v-if="userInfo.super_admin_role"
                :disabled="!form.principalName || !form.authType || !form.fileIdList.length || !!uploading"
                style="width: 120px;"
                class="save-btn"
                type="primary"
                @click="submit"
            >
                {{ form.realNameAuth ? '重新' : '' }}提交
            </el-button>
        </el-card>

        <el-dialog
            width="80%"
            title="文件预览:"
            v-model="preview.visible"
        >
            <div :class="['preview-box', { fullscreen: preview.fullscreen }]">
                <!-- <el-icon @click="preview.fullscreen = !preview.fullscreen">
                    <elicon-full-screen />
                </el-icon> -->
                <embed
                    v-if="preview.visible"
                    :src="preview.fileData"
                    :alt="preview.fileName"
                    :style="`width: 100%;${preview.fileType.includes('image') ? 'height:auto;' : 'min-height:calc(100vh - 50px);' }display:block;`"
                    @click="preview.fullscreen = !preview.fullscreen"
                >
                <el-divider></el-divider>
                <span class="color-danger">无法查看?</span> 下载附件:
                <p>
                    <el-link type="primary" :underline="false" @click="downloadFile($event, preview)">{{preview.fileName}}</el-link>
                </p>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        inject: ['refresh'],
        data() {
            return {
                loading:     false,
                pending:     false,
                submitting:  false,
                agreementId: '',
                options:     [],
                fileList:    [],
                form:        {
                    realNameAuth:  0,
                    fileIdList:    [],
                    auditComment:  '',
                    principalName: '',
                    description:   '',
                    authType:      '',
                },
                uploading: 0,
                preview:   {
                    visible:    false,
                    fileName:   '',
                    fileData:   '',
                    fileType:   '',
                    fileId:     '',
                    fullscreen: false,
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.getAuthStatus();
            this.getAgreementId();
        },
        methods: {
            async getAuthStatus() {
                this.loading = true;
                this.pending = true;
                const { code, data } = await this.$http.get('/union/member/realname/authInfo/query');

                this.loading = false;
                this.pending = false;
                if(code === 0) {
                    const { file_info_list } = data;

                    this.form.realNameAuth = data.real_name_auth_status;
                    this.form.principalName = data.principal_name;
                    this.form.auditComment = data.audit_comment;
                    this.form.description = data.description;
                    this.form.fileIdList = file_info_list;
                    this.form.authType = data.auth_type;

                    if(file_info_list.length) {
                        file_info_list.forEach(({ file_id, filename }) => {
                            this.getFile(file_id, filename, data.file_info_list.length);
                        });
                    }
                    this.getAuthType();
                }
            },

            async getAuthType() {
                const { code, data } = await this.$http.get('/union/member/authtype/query');

                if(code === 0) {
                    const index = data.list.findIndex(x => x.type_name === this.form.authType);

                    if(index < 0) {
                        this.form.authType = '';
                    }

                    data.list.forEach(item => {
                        this.options.push({
                            label: item.type_name,
                            value: item.type_name,
                        });
                    });
                }
            },

            async getAgreementId() {
                const { code, data } = await this.$http.get('/union/realname/auth/agreement/template/query');

                if(code === 0) {
                    this.agreementId = data.template_file_id;
                }
            },

            downloadFile(event, file) {
                if(this.loading || !this.agreementId) return;
                this.loading = true;

                const api = `${window.api.baseUrl}/union/download/file?fileId=${file? file.fileId : this.agreementId}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = api;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();

                setTimeout(() => {
                    this.loading = false;
                }, 300);
            },

            blobToDataURI(blob, callback) {
                const reader = new FileReader();

                reader.onload = function (e) {
                    callback(e.target.result);
                };
                reader.readAsDataURL(blob);
            },

            async getFile(fileId, fileName, files) {
                const { code, data } = await this.$http.post({
                    url:          '/union/download/file',
                    responseType: 'blob',
                    data:         {
                        fileId,
                    },
                });

                if(code === 0) {
                    this.blobToDataURI(data, result => {
                        this.fileList.push({
                            name: window.decodeURIComponent(fileName),
                            url:  result,
                            fileId,
                        });

                        if(this.fileList.length === files) {
                            setTimeout(() => {
                                this.pending = false;
                            }, 1000);
                        }
                    });
                }
            },

            onRemove(file, list) {
                const index = this.form.fileIdList.findIndex(({ file_id }) => file_id === file.fileId);
                const i = this.fileList.findIndex(item => item.fileId === file.fileId);

                console.log(file, this.form.fileIdList);
                if(~index) {
                    this.form.fileIdList.splice(index, 1);
                }
                if(~i) {
                    this.fileList.splice(index, 1);
                }
            },
            onPreview(file) {
                this.preview.fileType = file.name.endsWith('.pdf') ? 'application/pdf' : 'image/jpg';
                this.preview.fileId = file.fileId;
                this.preview.fileName = file.name;
                this.preview.fileData = file.url;
                this.preview.fullscreen = false;
                this.preview.visible = true;
            },
            beforeUpload(file) {
                const isImg = file.type === 'image/jpeg' || file.type === 'image/jpg' || file.type === 'image/png';
                const isWord = file.name.endsWith('.doc') || file.name.endsWith('.docx');
                const isPdf = file.type === 'application/pdf';

                if(!isImg && !isWord && !isPdf) {
                    this.$message.error('文件格式不支持!');
                    return false;
                }

                if(isImg || isWord) {
                    if(file.size / 1024 / 1024 > 5) {
                        this.$message.error('文件大小不能超过 5M !');
                        return false;
                    }
                }
                if(isPdf) {
                    if(file.size / 1024 / 1024 > 10) {
                        this.$message.error('文件大小不能超过 10M !');
                        return false;
                    }
                }

                this.uploading++;

                this.upload(file);

                return false;
            },
            async upload(file) {
                this.pending = true;

                const formData = new FormData();

                formData.append('file', file);
                formData.append('filename', file.name.toLowerCase());
                formData.append('purpose', 'RealnameAuth');

                const { code, data } = await this.$http.post({
                    url:  '/union/member/file/upload',
                    data: formData,
                });

                if(code === 0) {
                    const index = this.form.fileIdList.findIndex(({ file_id }) => file_id === data.file_id);

                    if(~index) {
                        this.$message.error('文件已在列表中!');
                    } else {
                        this.form.fileIdList.push({
                            file_id: data.file_id,
                        });
                        this.blobToDataURI(file, result => {
                            this.fileList.push({
                                name:   window.decodeURIComponent(file.name),
                                fileId: data.file_id,
                                url:    result,
                            });
                        });
                    }
                }
                this.uploading--;
                if(this.uploading === 0) {
                    this.pending = false;
                }

            },
            async submit($event) {
                const { code } = await this.$http.post({
                    url:  '/union/member/realname/auth',
                    data: {
                        memberId:      this.userInfo.member_id,
                        fileIdList:    this.form.fileIdList.map(x => x.file_id),
                        principalName: this.form.principalName,
                        description:   this.form.description,
                        authType:      this.form.authType,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.$message.success('已提交成功，请等待审核');
                    setTimeout(() => {
                        this.$router.replace('member-view');
                    }, 500);
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form{max-width: 500px;}
    .el-link{line-height: 1;}
    .el-uploader{
        :deep(.el-upload-dragger){width:500px;}
        :deep(.el-upload-list__item-thumbnail){display: none;}
        :deep(.el-upload-list__item-name){
            line-height: 30px !important;
            margin:0;
            .el-icon{display:none;}
        }
        :deep(.el-upload-list__item){
            padding:10px;
            height: 50px;
        }
    }
    .el-upload__tip{
        padding:20px;
        line-height: 16px;
    }
    .el-icon--upload{
        display: block;
        margin:20px auto 0;
    }
    .el-select{
        height:32px;
        :deep(.el-input) {
            .el-input__inner {
                background:#fff;
                height:30px;
            }
        }
        :deep(.el-icon) {
            svg{top:-3px;}
        }
    }
    .el-icon-full-screen{
        cursor: pointer;
        position: absolute;
        right: 0;
        top: 10px;
        font-size: 16px;
        &:hover {
            transform: scale(1.1);
        }
    }
    .preview-box{
        position: relative;
        padding-top: 30px;
        margin-top: -20px;
        &.fullscreen{
            position: fixed;
            top: 30px;
            right: 30px;
            bottom: 30px;
            left: 30px;
            overflow:auto;
        }
        .el-icon{
            position: absolute;
            top:10px;
            right:0;
            cursor: pointer;
        }
        embed{cursor: pointer;}
    }
</style>
