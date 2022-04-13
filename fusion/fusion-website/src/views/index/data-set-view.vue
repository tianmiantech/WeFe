<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="page-form block-form"
            :model="form"
        >
            <el-form-item
                prop="name"
                label="数据集名称："
                :rules="[{ required: true, message: '数据集名称必填!' }]"
            >
                <el-input
                    v-model="form.name"
                    size="large"
                    maxlength="30"
                    minlength="4"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item label="简介：">
                <el-input
                    v-model="form.description"
                    placeholder="简介, 描述"
                    maxlength="500"
                    type="textarea"
                    clearable
                    rows="4"
                />
            </el-form-item>

            <div class="add-title mb10">选择文件</div>
            <fieldset
                class="inline-form"
                style="min-height:230px;"
            >
                <legend>上传方式</legend>
                <el-form-item>
                    <el-radio
                        v-model="form.dataResourceSource"
                        label="UploadFile"
                    >
                        上传数据集文件
                    </el-radio>
                    <el-radio
                        v-model="form.dataResourceSource"
                        label="Sql"
                    >
                        数据库
                    </el-radio>
                    <el-radio
                        v-model="form.dataResourceSource"
                        label="LocalFile"
                    >
                        服务器本地文件
                    </el-radio>

                    <div
                        v-if="form.dataResourceSource === 'Sql'"
                        class="mt20"
                    >
                        <el-form-item
                            label="数据源:"
                            label-width="60px"
                        >
                            <el-select
                                v-model="search.database_name"
                                filterable
                                clearable
                            >
                                <el-option
                                    v-for="(data, index) in data_source_list"
                                    :key="index"
                                    :label="`${data.name} (${data.database_type})`"
                                    :value="data.id"
                                    @click.native="getDataSourceId(data.id)"
                                />
                            </el-select>

                            <el-button
                                type="primary"
                                @click="dataSource.show = true;"
                            >
                                新增数据源
                            </el-button>

                            <el-form-item label="查询语句">
                                <el-input
                                    v-model="form.sql"
                                    type="textarea"
                                    placeholder="select * from table where hello = 'world'"
                                />
                                <el-button
                                    class="mt10"
                                    @click="previewDataSet"
                                >
                                    查询测试
                                </el-button>
                            </el-form-item>
                        </el-form-item>
                    </div>
                </el-form-item>
                <div v-if="form.dataResourceSource === 'LocalFile'">
                    <el-input
                        v-model="local_filename"
                        placeholder="文件在服务器上的绝对路径"
                        @keydown.enter.native="previewDataSet"
                        @keydown.tab.native="previewDataSet"
                        @blur="previewDataSet"
                    />

                    <div class="el-upload__tip">
                        <ul class="data-set-upload-tip">
                            <li>csv 文件请使用 utf-8 编码格式</li>
                        </ul>
                    </div>
                </div>

                <uploader
                    v-if="form.dataResourceSource === 'UploadFile'"
                    :options="file_upload_options"
                    @file-complete="fileUploadComplete"
                    @file-removed="fileRemoved"
                    @file-added="fileAdded"
                >
                    <uploader-unsupport />
                    <uploader-drop v-if="file_upload_options.files.length === 0">
                        <p class="mb10">将文件（.csv .xls .xlsx）拖到此处</p>
                        或
                        <uploader-btn
                            :attrs="file_upload_attrs"
                            :single="true"
                        >
                            点击上传
                        </uploader-btn>
                        <uploader-list :file-list="file_upload_options.files.length" />
                    </uploader-drop>

                    <div class="el-upload__tip">
                        <ul class="data-set-upload-tip">
                            <li>注意</li>
                            <li>csv 文件请使用 utf-8 编码格式</li>
                        </ul>
                    </div>
                    <uploader-list />
                </uploader>
            </fieldset>

            <div class="add-title mt20">字段信息</div>
            <el-alert type="info">
                <template>
                    <div class="f14">
                        <p>* 请勾选需要上传的对齐字段信息</p>
                        <p>* 对齐标识字段建议限制 <i style="color:#F85564;">5个以内</i></p>
                        <p>* 可包含样本的日期字段信息</p>
                    </div>
                </template>
            </el-alert>
            <el-table
                max-height="600px"
                :data="metadata_pagination.list"
                :select-on-indeterminate="false"
                @selection-change="tableSelectionChange"
            >
                <el-table-column
                    type="selection"
                    width="45"
                />
                <el-table-column
                    prop="name"
                    label="名称"
                />
                <el-table-column
                    label="数据类型"
                >
                    <template slot-scope="scope">
                        {{ scope.row.data_type }}
                    </template>
                </el-table-column>
            </el-table>

            <el-button
                v-loading="addLoading"
                size="large"
                type="primary"
                class="save-btn mt20"
                :disabled="!row_list.length"
                @click="add"
            >
                生成
            </el-button>
        </el-form>

        <progressBar
            ref="progressRef"
            :process-data="processData"
        />

        <el-dialog
            :visible.sync="dataSource.show"
            :close-on-click-modal="false"
            title="添加数据源"
            destroy-on-close
            width="450px"
        >
            <el-form
                v-loading="dataSource.loading"
                label-width="130px"
                class="inline-form"
            >
                <el-form-item
                    label="数据源名称"
                    required
                >
                    <el-input
                        v-model="dataSource.name"
                        placeholder="dataset-sql"
                    />
                </el-form-item>
                <el-form-item label="数据库类型">
                    <el-select v-model="dataSource.databaseType">
                        <el-option
                            v-for="item in dataSource.databaseTypes"
                            :key="item"
                            :value="item"
                            :label="item"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item
                    label="IP"
                    required
                >
                    <el-input
                        v-model="dataSource.host"
                        placeholder="192.168.10.1"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    label="端口"
                    required
                >
                    <el-input
                        v-model="dataSource.port"
                        placeholder="3306"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    label="库名"
                    required
                >
                    <el-input
                        v-model="dataSource.databaseName"
                        placeholder="test"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="数据库用户名">
                    <el-input
                        v-model="dataSource.userName"
                        autocomplete="new-password"
                        placeholder="admin"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="数据库密码">
                    <el-input
                        v-model="dataSource.password"
                        autocomplete="new-password"
                        placeholder="password"
                        type="password"
                        clearable
                        @paste.native.prevent
                        @copy.native.prevent
                        @contextmenu.native.prevent
                    >
                        <template v-slot:append>
                            <el-button
                                class="ml10"
                                @click="pingTest"
                            >
                                连接测试
                            </el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item>
                    <el-button
                        type="primary"
                        @click="addDatabaseType"
                    >
                        保存
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import progressBar from '../components/progressBar';

export default {
    components: {
        progressBar,
    },
    data() {
        return {
            loading:                 false,
            // ui status
            tagInputVisible:         false,
            tagInputValue:           '',
            options_tags:            [],
            public_member_info_list: [],
            // preview
            raw_data_list:           [],
            metadata_list:           [],
            data_set_header:         [],
            data_type_options:       ['Integer', 'Long', 'Double', 'Enum', 'String'],
            // help: https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#%E5%A4%84%E7%90%86-get-%E6%88%96%E8%80%85-test-%E8%AF%B7%E6%B1%82
            file_upload_options:     {
                files:  [],
                target: window.api.baseUrl + '/file/upload',

                headers: {
                    token: '',
                },
                singleFile: true,

                testChunks:          true,
                chunkSize:           8 * 1024 * 1024,
                simultaneousUploads: 4,
                parseTimeRemaining(timeRemaining, parsedTimeRemaining) {
                    return parsedTimeRemaining
                        .replace(/\syears?/, '年')
                        .replace(/\days?/, '天')
                        .replace(/\shours?/, '小时')
                        .replace(/\sminutes?/, '分钟')
                        .replace(/\sseconds?/, '秒');
                },
            },
            file_upload_attrs: {
                accept: '.csv,.xls,.xlsx',
            },
            http_upload_filename: '',
            local_filename:       '',
            // model
            form:                 {
                publicLevel:        'Public',
                name:               '',
                description:        '',
                public_member_list: [],
                filename:           '',
                dataResourceSource: 'UploadFile',
                metadata_list:      [],
                deduplication:      false,
                row_list:           [],
                data_source_id:     '',
                sql:                '',
            },
            metadata_pagination: {
                list:       [],
                total:      0,
                page_size:  20,
                page_index: 1,
            },
            gridTheme: {
                color:       '#6C757D',
                borderColor: '#EBEEF5',
            },
            data_preview_finished: false,
            gridHeight:            0,
            search:                {
                database_name: '',
                tag:           '',
                source_type:   '',
            },

            dataSource: {
                loading:        false,
                show:           false,
                name:           '',
                host:           '',
                port:           '',
                databaseName:   '',
                userName:       '',
                password:       '',
                databaseType:   'MySql',
                databaseTypes:  ['MySql', 'Hive', 'Impala'],
                dataSourceList: [],
                dataset:        [],
            },

            data_source_list: [],
            data_source_id:   '',
            row_list:         [],
            isuploadok:       false,
            addLoading:       false,
            timer:            null,
            processData:      {
                text:       '正在存储数据集...',
                percentage: 0,
            },
        };
    },
    computed: {
        ...mapGetters(['userInfo']),
    },
    watch: {
        'form.dataResourceSource': {
            handler(newVal, oldVal) {
                if (newVal !== oldVal) {
                    this.metadata_pagination.list = [];
                    this.form.metadata_list = [];
                    this.metadata_list = [];
                    this.row_list = [];
                    this.file_upload_options.files = [];
                }
            },
            deep: false,
        },
        'dataSource.show': {
            handler(val) {
                if (!val) {
                    this.dataSource.name = '';
                    this.dataSource.host = '';
                    this.dataSource.port = '';
                    this.dataSource.databaseName = '';
                    this.dataSource.userName = '';
                    this.dataSource.password = '';
                    this.dataSource.dataset = [];
                }
            },
            deep: false,
        },
    },
    created() {

        this.file_upload_options.headers.token = this.userInfo.token;
        this.getDataSources();
    },

    methods: {
        async getDataSources() {
            const { code, data } = await this.$http.get('/data_source/query');

            if (code === 0) {
                this.data_source_list = data.list;
            }
        },
        // test db connect
        async pingTest() {
            this.dataSource.loading = true;
            const { code } = await this.$http.post({
                url:  '/data_source/test_db_connect',
                data: {
                    databaseType: this.dataSource.databaseType,
                    databaseName: this.dataSource.databaseName,
                    userName:     this.dataSource.userName,
                    password:     this.dataSource.password,
                    name:         this.dataSource.name,
                    host:         this.dataSource.host,
                    port:         this.dataSource.port,
                },
            });

            this.dataSource.loading = false;
            if (code === 0) {
                this.$message.success('数据库连接成功!');
            }
        },

        // add data source
        async addDatabaseType() {
            this.dataSource.loading = true;
            const { code } = await this.$http.post({
                url:  '/data_source/add',
                data: {
                    databaseType: this.dataSource.databaseType,
                    databaseName: this.dataSource.databaseName,
                    userName:     this.dataSource.userName,
                    password:     this.dataSource.password,
                    name:         this.dataSource.name,
                    host:         this.dataSource.host,
                    port:         this.dataSource.port,
                },
            });

            this.dataSource.loading = false;
            if (code === 0) {
                this.dataSource.show = false;
                this.$message.success('添加成功!');
                this.getDataSources();
            }
        },

        tableSelectionChange(val) {
            this.row_list = [];
            val.forEach(item => {
                if (item) {
                    this.row_list.push(item.name);
                }
            });
        },
        metadataPageChange(val) {
            const { page_size } = this.metadata_pagination;

            this.metadata_pagination.page_index = val;
            this.metadata_pagination.list = [];
            for (let i = page_size * (val - 1); i < val * page_size; i++) {
                const item = this.metadata_list[i];

                if (item) {
                    this.metadata_pagination.list.push(item);
                }
            }
        },
        dataTypeChange(row) {
            this.metadata_list[row.$index].data_type = row.data_type;
        },
        dataCommentChange(row) {
            this.metadata_list[row.$index].comment = row.comment;
        },

        getDataSourceId(id) {
            this.form.data_source_id = id;
            this.data_source_id = id;
        },

        async previewDataSource(id) {
            this.loading = true;
            this.data_preview_finished = false;
            this.data_source_id = id;

            this.form.filename = this.form.dataResourceSource === 'UploadFile' ? this.http_upload_filename : this.local_filename;

            const { code, data } = await this.$http.get({
                url:    '/data_set/preview',
                params: {
                    filename:           this.form.filename,
                    id,
                    dataResourceSource: this.form.dataResourceSource,
                },
            });

            if (code === 0) {
                this.raw_data_list = data.raw_data_list;
                this.data_set_header = data.header;

                this.metadata_pagination.list = [];
                this.metadata_list = data.metadata_list.map((item, index) => {
                    item.$index = index;
                    item.comment = '';
                    return item;
                });
                this.metadata_pagination.total = data.metadata_list.length;

                const length = this.metadata_pagination.page_size;

                for (let i = 0; i < length; i++) {
                    const item = this.metadata_list[i];

                    if (item) {
                        this.metadata_pagination.list.push(item);
                    }
                }
                this.gridHeight = 41 * (data.raw_data_list.length + 1) + 1;
                this.data_preview_finished = true;
            }
            this.loading = false;
        },

        async previewDataSet() {
            this.loading = true;
            this.data_preview_finished = false;

            this.form.filename = this.form.dataResourceSource === 'UploadFile' ? this.http_upload_filename : this.local_filename;

            const { code, data } = await this.$http.get({
                url:    '/data_set/preview',
                params: {
                    filename:           this.form.filename,
                    dataResourceSource: this.form.dataResourceSource,
                    sql:                this.form.sql,
                    id:                 this.form.data_source_id,
                },
            });

            if (code === 0) {
                this.raw_data_list = data.raw_data_list;
                this.data_set_header = data.header;

                this.metadata_pagination.list = [];
                this.metadata_list = data.metadata_list.map((item, index) => {
                    item.$index = index;
                    item.comment = '';
                    return item;
                });
                this.metadata_pagination.total = data.metadata_list.length;

                const length = this.metadata_pagination.page_size;

                for (let i = 0; i < length; i++) {
                    const item = this.metadata_list[i];

                    if (item) {
                        this.metadata_pagination.list.push(item);
                    }
                }
                this.gridHeight = 41 * (data.raw_data_list.length + 1) + 1;
                this.data_preview_finished = true;
            }
            this.loading = false;
        },

        fileAdded(file) {
            this.file_upload_options.files = [file];
        },
        fileRemoved() {
            this.file_upload_options.files = [];
            this.metadata_pagination.list = [];
        },

        async fileUploadComplete() {
            this.loading = true;
            this.data_preview_finished = false;
            const file = arguments[0].file;
            const { code, data } = await this.$http.get({
                url:    '/file/merge',
                params: {
                    filename:         file.name,
                    uniqueIdentifier: arguments[0].uniqueIdentifier,
                },
                headers: {
                    token: this.userInfo.token,
                },
            })
                .catch(err => {
                    console.log(err);
                });

            this.loading = false;
            if (code === 0) {
                this.http_upload_filename = data.filename;
                this.previewDataSet();
                this.isuploadok = true;
            }
        },

        async showSelectMemberDialog() {
            const ref = this.$refs['SelectMemberDialog'];

            ref.show = true;
            ref.loadDataList();
        },
        handleCloseTag(tag) {
            this.form.tags.splice(this.form.tags.indexOf(tag), 1);
        },
        handleTagInputConfirm() {
            if (this.tagInputValue) {
                this.form.tags.push(this.tagInputValue);
            }
            this.tagInputVisible = false;
            this.tagInputValue = '';
        },
        showInputTag() {
            this.tagInputVisible = true;
            this.$nextTick(_ => {
                this.$refs.saveTagInput.$refs.input.focus();
            });
        },
        async add() {
            if(this.addLoading) return;
            if (!this.form.name) {
                return this.$message.error('请输入数据集名称！');
            } else if (this.form.name.length < 4) {
                return this.$message.error('数据集名称不能少于4个字！');
            } else if (!this.row_list.length && this.form.dataResourceSource === 'UploadFile') {
                return this.$message.error('请选择字段！');
            } else if (this.form.dataResourceSource === 'LocalFile' && !this.local_filename) {
                return this.$message.error('请填写文件在服务器上的绝对路径！');
            } else if (this.form.dataResourceSource === 'LocalFile' && !this.row_list.length) {
                return this.$message.error('请选择字段信息！');
            } else if (this.row_list.length > 5) {
                return this.$message.error('对齐标识字段建议限制在5个以内！');
            }

            const ids = [];

            for (const index in this.public_member_info_list) {
                ids.push(this.public_member_info_list[index].id);
            }
            this.form.public_member_list = ids.join(',');

            if (this.form.publicLevel === 'PublicWithMemberList' && ids.length === 0) {
                this.$message.error('请选择可见成员！');
                return;
            }

            this.loading = true;
            this.form.metadata_list = this.metadata_list;
            this.form.rows = this.row_list;
            this.form.data_source_id = this.data_source_id;

            this.addLoading = true;
            if (this.form.dataResourceSource !== 'LocalFile') this.getDataSetStatus();

            const { code, data } = await this.$http.post({
                url:     '/data_set/add',
                timeout: 1000 * 60 * 24 * 30,
                data:    this.form,
            });

            this.addLoading = false;
            if (code === 0) {
                if (data.repeat_data_count > 0) {
                    this.$message.success('保存成功');
                } else {
                    this.getDataSetStatus(data.id);
                }

            } else {
                this.$refs['progressRef'].hideDialog();
            }
            this.loading = false;
        },

        async getDataSetStatus(data_set_id = '') {
            this.$refs['progressRef'].showDialog();
            if (data_set_id) {
                const { code, data } = await this.$http.post({
                    url:  '/data_set/get_state',
                    data: { data_set_id },
                });

                if (code === 0) {
                    const percentage = data.row_count === 0 ? 0 : Math.round(data.process_count / data.row_count * 100);

                    this.processData.percentage = percentage;

                    if (data.progress === 'Running') {
                        clearTimeout(this.timer);
                        this.timer = setTimeout(_ => {
                            this.getDataSetStatus(data_set_id);
                        }, 1000);
                    } else {
                        this.$message.success('保存成功!');
                        this.$router.push({
                            name: 'data-set-list',
                        });
                    }
                }
                if (code === 10019) {
                    clearTimeout(this.timer);
                }
            }
        },

        async getDataSetTags(keyword, cb) {
            this.options_tags = [];
            if (keyword) {
                const { code, data } = await this.$http.post({
                    url:  '/data_set/tags',
                    data: {
                        tag: keyword,
                    },
                });

                if (code === 0) {
                    for (const key in data) {
                        this.options_tags.push({ value: key, label: key });
                    }
                }
            }

            cb(this.options_tags);
        },
    },
};
</script>

<style lang="scss" scoped>
.page {
    overflow: visible;
}

.page-form {
    max-width: 600px;
}

.c-grid {
    border: 1px solid #EBEEF5;
    position: relative;
    z-index: 1;
}

.uploader-list {
    ::v-deep .uploader-file-meta {
        display: none;
    }

    ::v-deep .uploader-file-size,
    ::v-deep .uploader-file-name {
        font-size: 12px;
    }

    ::v-deep .uploader-file-actions {
        width: auto;
    }

    ::v-deep .uploader-file[status=success] .uploader-file-remove {
        display: block;
    }

    ::v-deep .uploader-file-status {
        font-size: 12px;
        text-indent: 2px;
        min-width: 130px;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
    }
}

.el-divider--horizontal {
    margin: 8px 0 20px 0;
}

.uploader-file-name {
    width: auto;
}

.uploader-file-status {
    width: 80px;
    text-indent: 2px;
}

.uploader-file-actions {
    width: 150px;
}

.uploader-drop {
    border-radius: 5px;
}

.uploader-btn {
    background: #fff;
}

.el-icon-upload {
    line-height: 1;
    margin: 10px auto 0;
}

.el-upload {
    position: relative;
}

.el-upload__input {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 10;
    display: block;
    cursor: pointer;
    opacity: 0;
}

.el-upload__tip {
    font-size: 14px;

    .data-set-upload-tip {
        text-align: left;
        margin-top: 8px;
        line-height: 130%;
        color: $--color-danger;
    }

    .data-set-upload-tip li {
        list-style: inside;
    }
}

.warning-tip {
    color: $--color-danger;
    line-height: 18px;
    font-weight: bold;
}

.save-btn {
    width: 100px;
}

.el-form-item .el-tag {
    margin-right: 10px;
}

.button-new-tag {
    height: 32px;
    line-height: 30px;
    padding-top: 0;
    padding-bottom: 0;
}

.input-new-tag {
    width: 230px;
    vertical-align: bottom;
}

.tags-tips {
    color: #999;
}

.member_list_ul {
    margin-top: 15px;
}

.member_list_ul li {
    line-height: 16px;
    margin-bottom: 8px;
}

.member_list_ul li .name {
    font-weight: bold;
}

.add-title {
    font-size: 14px;
    color: #6C757D;
    line-height: 32px;

    &::before {
        content: "*";
        color: $--color-danger;
        margin-right: 4px;
    }
}
</style>
