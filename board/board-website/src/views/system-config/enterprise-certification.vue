<template>
    <div class="page">
        <el-card shadow="never">
            <h3>企业认证</h3>
            <el-form
                class="mt20"
                :model="form"
                :disabled="!userInfo.super_admin_role"
            >
                <el-form-item
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
                <el-form-item
                    label="认证文件："
                    :rules="[{required: true, message: '认证文件必传!'}]"
                >
                    <el-upload
                        v-loading="pending"
                        :file-list="fileList"
                        :on-remove="onRemove"
                        :http-request="() => {}"
                        :before-upload="beforeUpload"
                        accept=".png,.jpg,.pdf"
                        list-type="picture"
                        action="#"
                        multiple
                        drag
                    >
                        <i class="el-icon--upload"></i>
                        <div>
                            将文件 (.jpg/png/PDF) 拖拽到此处或<p><el-button type="primary">点此上传</el-button>
                            </p>
                            <div class="el-upload__tip">jpg/png 文件最大上传 10M, PDF 最大上传 5M, <br>可同时上传多个文件</div>
                        </div>
                    </el-upload>
                </el-form-item>
            </el-form>

            <el-button
                v-loading="loading"
                v-if="userInfo.super_admin_role"
                :disabled="!form.principalName || !form.authType || !form.fileIdList.length || !!uploading"
                style="width: 120px;"
                class="save-btn"
                type="primary"
                @click="submit"
            >
                提交
            </el-button>
        </el-card>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        inject: ['refresh'],
        data() {
            return {
                loading:  false,
                pending:  false,
                options:  [],
                fileList: [],
                form:     {
                    realNameAuth:  0,
                    fileIdList:    [],
                    auditComment:  '',
                    principalName: '',
                    description:   '',
                    authType:      '',
                },
                fileMap: {

                },
                uploading: 0,
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.getAuthStatus();
            this.getAuthType();
        },
        methods: {
            async getAuthStatus() {
                const { code, data } = await this.$http.get('/union/member/realname/authInfo/query');

                if(code === 0) {
                    this.form.realNameAuth = data.real_name_auth_status;
                    this.form.principalName = data.principal_name;
                    this.form.auditComment = data.audit_comment;
                    this.form.description = data.description;
                    this.form.fileIdList = data.file_id_list;
                    this.form.authType = data.auth_type;

                    data.file_id_list.forEach(id => {
                        this.getFile(id);
                    });
                }
            },
            async getAuthType() {
                const { code, data } = await this.$http.get('/union/member/authtype/query');

                if(code === 0) {
                    data.list.forEach(item => {
                        this.options.push({
                            label: item.type_name,
                            value: item.type_id,
                        });
                    });
                }
            },
            blobToDataURI(blob, callback) {
                const reader = new FileReader();

                reader.onload = function (e) {
                    callback(e.target.result);
                };
                reader.readAsDataURL(blob);
            },
            async getFile(id) {
                const { code, data } = await this.$http.get(`union/download/file?fileId=${id}`, {
                    // responseType: 'blob',
                });

                if(code === 0) {
                    this.blobToDataURI(data, result => {
                        this.fileList.push({
                            name: data.name,
                            url:  result,
                        });
                    });
                }
            },

            onRemove(file) {
                console.log(this.fileList);
            },
            beforeUpload(file) {
                const isImg = file.type === 'image/jpeg' || file.type === 'image/jpg' || file.type === 'image/png';
                const isPdf = file.type === 'application/pdf';

                if(!isImg && !isPdf) {
                    this.$message.error('文件格式不支持!');
                    return false;
                }

                if(isImg) {
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
                return true;
            },
            async upload(file) {
                this.pending = true;
                const formData = new FormData();

                formData.append('file', file);

                const { code, data } = await this.$http.post({
                    url:  '/union/member/file/upload',
                    data: formData,
                });

                this.uploading--;
                if(code === 0) {
                    this.fileMap[file.uid] = data.file_id;
                    this.form.fileIdList.push(data.file_id);
                }
                if(this.uploading === 0) {
                    this.pending = false;
                }
            },
            async submit($event) {
                const { code } = await this.$http.post({
                    url:  '/union/member/realname/auth',
                    data: {
                        memberId:      this.userInfo.member_id,
                        fileIdList:    this.form.fileIdList,
                        principalName: this.form.principalName,
                        description:   this.form.description,
                        authType:      this.form.authType,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.$message.success('提交成功! 正在为您审核');
                    this.$router.replace('member-view');
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form{max-width: 400px;}
    .el-upload__tip{
        padding:20px;
        line-height: 16px;
    }
    .el-select{
        height:32px;
        :deep(.el-input) {
            .el-input__inner {
                background:#fff;
                height:30px;
            }
        }
    }
</style>
