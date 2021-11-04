<template>
    <el-card class="page">
        <!-- <el-form
            inline
            @submit.prevent
        >
            <el-form-item label="名称">
                <el-input v-model="search.name" />
            </el-form-item>
            <el-form-item>
                <el-checkbox label="已删除" v-model="search.status"></el-checkbox>
            </el-form-item>
            <el-button
                type="primary"
                native-type="submit"
                @click="getList({ to: true, resetPagination: true })"
            >
                搜索
            </el-button>
        </el-form> -->

        <div class="mb20">
            <el-button
                type="primary"
                @click="editDialog = true; editName = ''; editURL = '';"
            >
                上传协议
            </el-button>
        </div>

        <el-table
            v-loading="loading"
            class="card-list"
            :data="list"
            border
            stripe
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column label="序号" type="index"></el-table-column>
            <el-table-column label="名称" min-width="200">
                <template v-slot="scope">
                    {{ scope.row.file_name }}
                </template>
            </el-table-column>
            <el-table-column label="预览" min-width="100">
                <template v-slot="scope">
                    <el-button
                        type="primary"
                        @click="filePreview($event, scope.row)"
                    >预览</el-button>
                </template>
            </el-table-column>
            <el-table-column label="上传时间" min-width="140">
                <template v-slot="scope">
                    {{ dateFormat(scope.row.create_time) }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                fixed="right"
                width="240"
            >
                <template v-slot="scope">
                    <el-button
                        type="danger"
                        @click="remove($event, scope.row)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <div
            v-if="pagination.total"
            class="mt20 text-r"
        >
            <el-pagination
                :total="pagination.total"
                :page-sizes="[10, 20, 30, 40, 50]"
                :page-size="pagination.page_size"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
        </div>

        <el-dialog
            title="上传认证协议"
            v-model="editDialog"
            width="400px"
        >
            <el-upload
                drag
                :action="action"
                accept=".png,.jpg,.pdf"
                :before-upload="beforeUpload"
                :on-success="uploadFinished"
            >
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">
                    拖拽文件或 <em>点击上传</em>
                </div>
                <template #tip>
                    <div class="el-upload__tip">
                        支持 png, jpg 最大上传 5M, PDF 最大上传 10M
                    </div>
                </template>
            </el-upload>
        </el-dialog>

        <el-dialog
            title="文件预览:"
            v-model="preview.visible"
        >
            <div :class="['preview-box', { fullscreen: preview.fullscreen }]">
                <i class="el-icon-full-screen" @click="preview.fullscreen = !preview.fullscreen"></i>
                <embed
                    v-if="preview.visible"
                    :src="preview.fileData"
                    :alt="preview.fileName"
                    :style="`width: 100%;${preview.fileType.includes('image') ? 'height:auto;' : 'min-height:calc(100vh - 50px);' }display:block;`"
                >
            </div>
        </el-dialog>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table';

    export default {
        inject: ['refresh'],
        mixins: [table],
        data() {
            return {
                editId:   '',
                editName: '',
                editURL:  '',
                search:   {
                    id:     '',
                    name:   '',
                    status: '',
                },
                watchRoute:    true,
                defaultSearch: true,
                requestMethod: 'post',
                getListApi:    '/auth/agreement/template/query',
                action:        `${window.api.baseUrl || ''}/manager-service/auth/agreement/template/upload`,
                editDialog:    false,
                preview:       {
                    visible:    false,
                    fileName:   '',
                    fileData:   '',
                    fileType:   '',
                    fullscreen: false,
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            blobToDataURI(blob, callback) {
                const reader = new FileReader();

                reader.onload = function (e) {
                    callback(e.target.result);
                };
                reader.readAsDataURL(blob);
            },
            async filePreview(event, row) {
                this.editId = row.id;
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url:          '/download/file?fileId=' + row.auth_agreement_file_id,
                    responseType: 'blob',
                });

                this.loading = false;
                if(code === 0) {
                    this.preview.fileType = data.type;
                    this.blobToDataURI(data, result => {
                        this.preview.visible = true;
                        this.preview.fullscreen = false;
                        this.preview.fileData = result;
                    });
                }
            },
            remove(event, row) {
                this.$confirm('是否继续删除该文件吗?', '警告', {
                    type: 'warning',
                })
                    .then(async () => {
                        const { code } = await this.$http.post({
                            url:  '/auth/agreement/template/delete',
                            data: {
                                unionNodeId: row.union_node_id,
                            },
                            btnState: {
                                target: event,
                            },
                        });

                        if(code === 0) {
                            this.refresh();
                            this.$message.success('处理成功!');
                        }
                    });
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
                return true;
            },
            uploadFinished(res) {
                if(res.code === 0) {
                    this.$message.success('上传成功!');
                    this.refresh();
                } else {
                    this.$message.error(res.message);
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .card-list{min-height: calc(100vh - 250px);}
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
        margin-top: -30px;
        &.fullscreen{
            position: fixed;
            top: 30px;
            right: 30px;
            bottom: 30px;
            left: 30px;
            overflow:auto;
        }
    }
</style>
