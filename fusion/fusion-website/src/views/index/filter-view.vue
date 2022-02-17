<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="page-form"
            :model="form"
        >
            <el-form-item
                prop="name"
                label="过滤器名称："
                :rules="[{ required: true, message: '过滤器名称必填!' }]"
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
                    type="textarea"
                    clearable
                    rows="4"
                />
            </el-form-item>

            <fieldset style="min-height:230px; max-width:500px;">
                <legend>上传方式</legend>
                <el-form-item>
                    <el-radio
                        v-model="form.dataResourceSource"
                        label="UploadFile"
                    >
                        上传过滤器文件
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
                        class="mt10"
                    >
                        <el-form-item
                            label="数据源:"
                            label-width="80px"
                        >
                            <el-select
                                v-model="search.database_name"
                                filterable
                                clearable
                            >
                                <el-option
                                    v-for="(data, index) in data_source_list"
                                    :key="index"
                                    :value="data.id"
                                    :label="`${data.name} (${data.database_type})`"
                                    @click.native="getDataSourceId(data.id)"
                                />
                            </el-select>

                            <el-button
                                type="primary"
                                @click="dataSource.show = true;"
                            >
                                新增数据源
                            </el-button>
                        </el-form-item>
                        <el-form-item
                            label="查询语句"
                            label-width="80px"
                        >
                            <el-input
                                v-model="form.sql"
                                type="textarea"
                                placeholder="select * from table limit 100"
                            />
                            <el-button
                                class="mt10"
                                @click="previewDataSet"
                            >
                                查询测试
                            </el-button>
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
                            <li>主键字段必须是第一列，并且会被自动 hash</li>
                            <li>
                                主键重复的数据会被自动去重，仅保留第一条
                            </li>
                            <li>y 值列的列名必须为 y</li>
                            <li>
                                csv 文件请使用 utf-8 编码格式
                            </li>
                        </ul>
                    </div>
                </div>

                <uploader
                    v-if="form.dataResourceSource === 'UploadFile'"
                    :options="file_upload_options"
                    @file-complete="fileUploadComplete"
                >
                    <uploader-unsupport />
                    <uploader-drop>
                        <p class="mb10">将文件（.csv .xls .xlsx）拖到此处</p>
                        或
                        <uploader-btn
                            :attrs="file_upload_attrs"
                            :single="true"
                        >
                            点击上传
                        </uploader-btn>
                    </uploader-drop>

                    <div class="el-upload__tip">
                        <ul class="data-set-upload-tip">
                            <li>注意</li>
                            <li>
                                主键重复的数据会被自动去重，仅保留第一条
                            </li>
                            <li>y 值列的列名必须为 y</li>
                            <li>
                                csv 文件请使用 utf-8 编码格式
                            </li>
                        </ul>
                    </div>
                    <uploader-list />
                </uploader>
            </fieldset>

            <el-form class="m20">
                <el-form-item
                    v-if="metadata_pagination.list.length > 0"
                    label="设置主键："
                    label-width="100px"
                    required
                >
                    <el-button
                        :disabled="fieldInfoList.length > 4"
                        @click="addFieldInfo"
                    >
                        + 添加
                    </el-button>
                </el-form-item>

                <div
                    v-for="(item, index) in fieldInfoList"
                    :key="index"
                    class="mb10"
                >
                    <div class="inlineblock f12 mb10">
                        字段：
                        <el-select
                            v-model="item.column_arr"
                            no-data-text="请先选择样本"
                            multiple
                            @change="keyFormater"
                        >
                            <el-option
                                v-for="value in dataResource.rows"
                                :key="value"
                                :value="value"
                                :label="value"
                            />
                        </el-select>
                    </div>

                    <div class="inlineblock f12 mb10 pl10">
                        处理方式：
                        <el-select
                            v-model="item.options"
                            clearable
                            style="width:150px;"
                            @change="keyFormater"
                        >
                            <el-option
                                v-for="options in optionsList"
                                :key="options.value"
                                :value="options.value"
                                :label="options.name"
                            />
                        </el-select>
                        <i
                            v-if="fieldInfoList.length"
                            class="el-icon-remove-outline f16"
                            @click="removeFieldInfo({$index: index })"
                        />
                    </div>
                </div>

                <div v-if="keyRes">
                    <el-alert
                        :closable="false"
                        class="inlineblock"
                        type="success"
                    >
                        主键生成规则:   {{ keyRes }}
                    </el-alert>
                </div>

                <el-button
                    class="save-btn mt20"
                    type="primary"
                    size="large"
                    :disabled="!keyRes"
                    :loading="saveLoading"
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
                    class="flex-form"
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
        </el-form>
    </el-card>
</template>

<script>
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
            filter_header:           [],
            data_type_options:       ['Integer', 'Long', 'Double', 'Enum', 'String'],
            // help: https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#%E5%A4%84%E7%90%86-get-%E6%88%96%E8%80%85-test-%E8%AF%B7%E6%B1%82
            file_upload_options:     {
                target: window.api.baseUrl + '/file/upload',

                singleFile: true,

                testChunks:          true,
                chunkSize:           8 * 1024 * 1024,
                simultaneousUploads: 4,
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
                fieldInfoList:      [],
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
            data_source_list: [],
            data_source_id:   '',
            row_list:         [],
            fieldInfoList:    [],
            optionsList:      [{
                name:  'md5',
                value: 'MD5',
            },
            {
                name:  'sha',
                value: 'SHA1',
            },
            {
                name:  '不处理',
                value: 'NONE',
            }],

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

            // dataResource
            dataResource: {
                visible:     false,
                editor:      false,
                id:          '',
                name:        '',
                type:        '',
                description: '',
                rows:        '',
            },
            keyRes:      '',
            isuploadok:  false,
            saveLoading: false,
            timer:       null,
            processData: {
                text: '正在存储过滤器...',
            },
        };
    },
    watch: {
        'form.dataResourceSource': {
            handler(newVal, oldVal) {
                if (newVal !== oldVal) {
                    this.metadata_pagination.list = [];
                    this.file_upload_options.files = [];
                    this.fieldInfoList = [];
                    this.keyRes = '';
                }
            },
            deep: true,
        },
    },
    async created() {
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
            if(code === 0) {
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
            if(code === 0) {
                this.dataSource.show = false;
                this.$message.success('添加成功!');
                this.getDataSources();
            }
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

        getDataSourceId(id){
            this.form.data_source_id = id;
            this.data_source_id = id;
        },

        async previewDataSource(id) {
            this.loading = true;
            this.data_preview_finished = false;
            this.data_source_id = id;

            this.form.filename = this.form.dataResourceSource === 'UploadFile' ? this.http_upload_filename : this.local_filename;

            const { code, data } = await this.$http.get({
                url:    '/filter/preview',
                params: {
                    filename:           this.form.filename,
                    id,
                    dataResourceSource: this.form.dataResourceSource,
                },
            });

            if (code === 0) {
                this.raw_data_list = data.raw_data_list;
                this.filter_header = data.header;

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
                url:    '/filter/preview',
                params: {
                    filename:           this.form.filename,
                    dataResourceSource: this.form.dataResourceSource,
                    sql:                this.form.sql,
                    id:                 this.form.data_source_id,
                },
            });

            if (code === 0) {
                this.raw_data_list = data.raw_data_list;
                this.filter_header = data.header;

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
            if (!this.form.name) {
                this.$message.error('请输入过滤器名称！');
                return;
            } else if (this.form.name.length < 4) {
                this.$message.error('数据集名称长度不能少于4，不能大于30');
                return;
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

            this.fieldInfoList.forEach((item, index) => {
                item.columns=item.column_arr.join(',');
            });

            this.loading = true;
            this.form.metadata_list = this.metadata_list;
            this.form.rows = this.dataResource.rows;
            this.form.data_source_id = this.data_source_id;
            this.form.fieldInfoList = this.fieldInfoList;

            if ((!this.form.fieldInfoList.length || !this.form.fieldInfoList[0].column_arr.length || !this.form.fieldInfoList[0].options) && (this.form.dataResourceSource ==='UploadFile' || this.form.dataResourceSource ==='LocalFile')) {
                this.$message.error('请添加主键！');
                return;
            }
            if(this.form.dataResourceSource === 'LocalFile' && !this.local_filename) {
                this.$message.error('请填写文件在服务器上的绝对路径！');
                return;
            }

            this.saveLoading = true;
            if (this.form.dataResourceSource !== 'LocalFile') this.getDataSetStatus();
            const { code, data } = await this.$http.post({
                url:     '/filter/add',
                timeout: 1000 * 60 * 24 * 30,
                data:    this.form,
            });

            if (code === 0) {
                if (data.repeat_data_count > 0) {
                    this.$message.success(`保存成功，过滤器包含重复数据 ${data.repeat_data_count} 条，已自动去重。`);
                } else {
                    this.getDataSetStatus(data.data_source_id);
                }
            } else {
                this.saveLoading = false;
            }
            this.loading = false;
        },

        async getDataSetStatus(data_set_id) {
            this.$refs['progressRef'].showDialog();
            if (data_set_id) {
                const { code, data } = await this.$http.post({
                    url:  '/filter/get_state',
                    data: { bloom_filter_id: data_set_id },
                });

                if (code === 0) {
                    const percentage = data.row_count === 0 ? 0 : Math.round(data.process_count / data.row_count * 100);

                    this.processData = {
                        percentage,
                    };
                    if (data.progress === 'Running') {
                        clearTimeout(this.timer);
                        this.timer = setTimeout(_ => {
                            this.getDataSetStatus(data_set_id);
                        }, 1000);
                    } else {
                        this.$message.success('保存成功!');
                        this.$router.push({
                            name: 'filter-list',
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
                    url:  '/filter/tags',
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

        async addFieldInfo() {
            this.dataResource.rows = [];
            this.metadata_pagination.list.forEach(data => {
                if (data) {
                    this.dataResource.rows.push(data.name);
                }
            });


            const fieldInfo = {
                column_arr:  '',
                columns:     '',
                options:     '',
                frist_index: '',
                end_index:   '',
            };

            this.fieldInfoList.push(fieldInfo);
        },

        removeFieldInfo($index) {
            this.fieldInfoList.splice($index, 1);

            this.keyFormater();
        },

        addDataSet() {
            const ref = this.$refs['SelectDatasetDialog'];

            ref.show = true;
            ref.loadDataList();
        },

        keyFormater() {

            let res = '';

            if(this.fieldInfoList.length===0){
                this.keyRes = res;
                return;
            }

            const lastItem = this.fieldInfoList[this.fieldInfoList.length - 1];

            if(!lastItem.column_arr.length || !lastItem.options) {
                return;
            }

            this.fieldInfoList.forEach((item, index) => {
                if(item.options === 'NONE'){
                    res = res + `${res?' + ': ''}${item.column_arr.join(' + ')}`;
                } else {
                    res = res + `${res?' + ': ''}${item.options}(${item.column_arr.join(' + ')})`;
                }
            });
            res = `Id = ${res}`;
            this.keyRes = res;
        },
    },
};
</script>

<style lang="scss" scoped>
    .page {overflow: visible;}

    .el-input, .el-textarea{max-width: 500px;}

    .el-icon-remove-outline{
        color:#ff5757;
        cursor: pointer;
        margin-left: 10px;
    }
    .el-alert{width:auto;}
    .el-select{
        ::v-deep .el-tag__close.el-icon-close{
            background:#fff;
            &:hover{background:#28c2d7;}
        }
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
            color: #ff5757;
            line-height: 130%;
        }

        .data-set-upload-tip li {
            list-style: inside;
        }
    }

    .save-btn {
        width: 100px;
    }
</style>
