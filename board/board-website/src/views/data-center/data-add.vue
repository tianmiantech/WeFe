<template>
    <el-card
        v-loading.fullscreen="loading"
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-row :gutter="40">
                <el-col :span="10">
                    <el-form-item
                        prop="name"
                        label="数据集名称："
                        :rules="[{ required: true, message: '数据集名称必填!' }]"
                    >
                        <el-input
                            v-model="form.name"
                            size="large"
                            :minlength="4"
                            :maxlength="30"
                            show-word-limit
                        />
                    </el-form-item>
                    <el-form-item prop="tag">
                        <p class="tags-tips mb10 f12">为数据集设置关键词，方便大家快速了解你 ：）</p>
                        <el-tag
                            v-for="(tag, index) in tagList"
                            :key="index"
                        >
                            <el-checkbox v-model="tag.checked">
                                {{ tag.label }}
                            </el-checkbox>
                        </el-tag>
                        <el-input
                            ref="saveTagInput"
                            v-model="tagInputValue"
                            class="input-new-tag"
                            show-word-limit
                            :maxlength="10"
                            placeholder="按回车或 Tab 添加建关键词"
                            @keyup.enter="tagInputConfirm"
                            @keydown.tab="tagInputConfirm"
                        />
                    </el-form-item>
                </el-col>
                <el-col :span="14">
                    <el-form-item
                        label="简介："
                        style="width:100%;"
                    >
                        <el-input
                            v-model="form.description"
                            placeholder="简介, 描述"
                            type="textarea"
                            clearable
                            rows="4"
                        />
                    </el-form-item>
                </el-col>
            </el-row>
            <el-divider />
            <el-row :gutter="40">
                <el-col
                    :span="10"
                    style="position: relative;"
                >
                    <fieldset style="min-height:230px;">
                        <legend>可见性</legend>
                        <el-form-item>
                            <el-radio
                                v-model="form.publicLevel"
                                label="Public"
                            >
                                对所有成员可见
                            </el-radio>
                            <el-radio
                                v-model="form.publicLevel"
                                label="OnlyMyself"
                            >
                                仅自己可见
                            </el-radio>
                            <el-radio
                                v-model="form.publicLevel"
                                label="PublicWithMemberList"
                            >
                                对指定成员可见
                            </el-radio>
                        </el-form-item>
                        <el-form-item>
                            <div v-if="form.publicLevel === 'PublicWithMemberList'">
                                <el-button
                                    size="mini"
                                    @click="showSelectMemberDialog"
                                >
                                    选择可见成员
                                </el-button>
                                已选（{{ public_member_info_list.length }}）
                                <ul class="member_list_ul">
                                    <li
                                        v-for="(item, index) in public_member_info_list"
                                        :key="item.id"
                                        class="flex-center"
                                    >
                                        <p>
                                            <span class="name">{{
                                                item.name
                                            }}</span>
                                            <br>
                                            <span class="p-id">
                                                {{ item.id }}
                                            </span>
                                        </p>
                                        <i class="el-icon-close" @click="deleteSelectedMember(item, index)"></i>
                                    </li>
                                </ul>
                            </div>
                        </el-form-item>
                        <DataSetPublicTips v-if="form.public_level != 'OnlyMyself'" />
                    </fieldset>
                </el-col>
                <!-- 结构化数据 -->
                <el-col v-if="addDataType === 'csv'" :span="14">
                    <fieldset style="min-height:230px">
                        <legend>选择文件</legend>
                        <el-form-item>
                            <el-radio
                                v-model="form.data_set_add_method"
                                label="HttpUpload"
                            >
                                上传数据集文件
                            </el-radio>
                            <el-radio
                                v-model="form.data_set_add_method"
                                label="LocalFile"
                            >
                                服务器本地文件
                            </el-radio>
                            <el-radio
                                v-model="form.data_set_add_method"
                                label="Database"
                            >
                                数据库
                            </el-radio>
                        </el-form-item>
                        <div v-if="form.data_set_add_method === 'LocalFile'">
                            <el-input
                                v-model="local_filename"
                                placeholder="文件在服务器上的绝对路径"
                                @keydown.enter="previewDataSet"
                                @keydown.tab="previewDataSet"
                            />
                        </div>
                        <uploader
                            v-if="form.data_set_add_method === 'HttpUpload'"
                            ref="uploaderRef"
                            :options="file_upload_options"
                            :file-status-text="fileStatusText"
                            :list="form.data_set_add_method.files"
                            @file-complete="fileUploadComplete"
                            @file-removed="fileRemoved"
                            @file-added="fileAdded"
                        >
                            <uploader-unsupport />
                            <uploader-drop v-if="file_upload_options.files.length === 0">
                                <p class="mb10">将文件（.csv .xls .xlsx）拖到此处</p>或
                                <uploader-btn
                                    :attrs="file_upload_attrs"
                                    :single="true"
                                >
                                    点击上传
                                </uploader-btn>
                            </uploader-drop>
                            <uploader-list :file-list="file_upload_options.files.length" />
                        </uploader>

                        <el-form v-if="form.data_set_add_method === 'Database'">
                            <el-form-item label="数据源">
                                <el-select
                                    v-model="form.databaseName"
                                    filterable
                                    clearable
                                    @change="dataBaseNameChanged"
                                >
                                    <el-option
                                        v-for="item in dataSource.dataSourceList"
                                        :key="item.id"
                                        :value="item.name"
                                        class="f12"
                                    >
                                        [{{item.database_type}}] {{ item.name }} ({{ item.host }}:{{ item.port }})
                                        <i
                                            class="el-icon-close ml5 f16"
                                            @click.prevent.stop="removeDataSource($event, item)"
                                        />
                                    </el-option>
                                </el-select>
                                <el-button
                                    class="ml10"
                                    @click="dataSource.show = true"
                                >添加数据源</el-button>
                            </el-form-item>
                            <el-form-item label="查询语句">
                                <el-input
                                    type="textarea"
                                    v-model="form.sql"
                                    placeholder="select * from table where hello = 'world'"
                                />
                                <el-button class="mt10" @click="previewDataSet">
                                    查询测试
                                </el-button>
                            </el-form-item>
                        </el-form>

                        <ul class="data-set-upload-tip">
                            <li>主键字段必须是第一列，并且会被自动 hash</li>
                            <li>主键重复的数据会被自动去重，仅保留第 1 条</li>
                            <li>y 值列的列名必须为 y</li>
                            <li>csv 文件请使用 utf-8 编码格式</li>
                        </ul>
                    </fieldset>
                </el-col>
                <!-- 图像数据 -->
                <el-col v-else :span="14">
                    图像数据
                </el-col>
            </el-row>
            <el-row
                v-if="metadata_pagination.list.length > 0"
                :gutter="40"
            >
                <el-col :span="10">
                    <h4 class="mt10 mb20">
                        字段信息：
                        <el-select
                            v-model="dataTypeFillVal"
                            class="float-right"
                            size="mini"
                            clearable
                            placeholder="数据类型缺失填充"
                            @change="dataTypeFill"
                        >
                            <el-option
                                v-for="dataType in data_type_options"
                                :key="dataType"
                                :label="dataType"
                                :value="dataType"
                            />
                        </el-select>
                    </h4>
                    <el-table
                        border
                        :data="metadata_pagination.list"
                        style="width: 100%;"
                    >
                        <el-table-column
                            label="序号"
                            width="50"
                        >
                            <template v-slot="scope">
                                {{ scope.row.$index }}
                            </template>
                        </el-table-column>
                        <el-table-column
                            prop="name"
                            label="名称"
                            width="130"
                        />
                        <el-table-column
                            label="数据类型"
                            width="110"
                        >
                            <template v-slot="scope">
                                <el-select
                                    v-model="scope.row.data_type"
                                    placeholder="请选择"
                                    @change="dataTypeChange(scope.row)"
                                >
                                    <el-option
                                        v-for="item in data_type_options"
                                        :key="item"
                                        :label="item"
                                        :value="item"
                                    />
                                </el-select>
                            </template>
                        </el-table-column>
                        <el-table-column label="注释">
                            <template v-slot="scope">
                                <el-input
                                    v-model="scope.row.comment"
                                    maxlength="250"
                                    @blur="dataCommentChange(scope.row)"
                                />
                            </template>
                        </el-table-column>
                    </el-table>
                    <div
                        v-if="metadata_pagination.total"
                        class="mt20 text-r"
                    >
                        <el-pagination
                            :total="metadata_pagination.total"
                            :current-page="metadata_pagination.page_index"
                            :page-size="metadata_pagination.page_size"
                            layout="total, prev, pager, next, jumper"
                            @current-change="metadataPageChange"
                        />
                    </div>
                </el-col>
                <el-col :span="14">
                    <h4 class="m5">数据集预览：</h4>
                    <c-grid
                        v-if="!loading"
                        :theme="gridTheme"
                        :data="raw_data_list"
                        :frozen-col-count="1"
                        font="12px sans-serif"
                        :style="{height:`${gridHeight}px`}"
                    >
                        <c-grid-column
                            v-for="(item, index) in data_set_header"
                            :key="index"
                            :field="item"
                            min-width="100"
                            :column-style="{textOverflow: 'ellipsis'}"
                        >
                            {{ item }}
                        </c-grid-column>
                    </c-grid>
                </el-col>
            </el-row>
            <el-row
                :gutter="100"
                class="m20"
            >
                <el-col :span="12">
                    <el-checkbox v-model="form.deduplication">
                        自动剔除主键相同的数据
                    </el-checkbox>
                    <br>
                    <div class="deduplication-tips">
                        <p>注：数据集中不允许包含主键相同的数据。</p>
                        <p>1. 如果 <strong>不确定</strong> 是否包含重复数据，请 <strong>启用</strong> 自动去重功能。</p>
                        <p>2. 如果 <strong>确定</strong> 不包含重复数据，<strong>可以禁用</strong> 自动去重功能，以提高数据集上传速度。</p>
                    </div>
                    <el-button
                        class="save-btn mt20"
                        type="primary"
                        size="large"
                        :disabled="!data_preview_finished"
                        @click="add"
                    >
                        添加
                    </el-button>
                </el-col>
            </el-row>
        </el-form>

        <SelectMemberDialog
            ref="SelectMemberDialog"
            :block-my-id="true"
            :public-member-info-list="public_member_info_list"
            @select-member="selectMember"
        />

        <el-dialog
            v-model="uploadTask.visible"
            :close-on-click-modal="false"
            :show-close="isCanClose"
            title="正在存储数据集..."
            destroy-on-close
            width="450px"
        >
            <div class="text-c">
                <el-progress
                    :percentage="uploadTask.progress || 0"
                    :color="uploadTask.colors"
                    type="dashboard"
                />
                <p class="mb10">正在存储数据集...</p>
                <div class="upload-info">
                    <p class="mb5">总数据行数：<span>{{uploadTask.total_row_count}}</span></p>
                    <p class="mb5">已处理数据行数：<span>{{uploadTask.added_row_count}}</span></p>
                    <p class="mb10">主键重复条数：<span>{{uploadTask.repeat_id_row_count}}</span></p>
                    <p v-if="uploadTask.error_message" class="mb10">错误信息：<span class="color-danger">{{uploadTask.error_message}}</span></p>
                    <strong v-if="uploadTask.repeat_id_row_count" class="color-danger">!!! 包含重复主键的数据集上传效率会急剧下降，建议在本地去重后执行上传。</strong>
                </div>
                <p class="mt10 mb10">预计剩余时间: {{ timeFormat(uploadTask.estimate_time) }}</p>
            </div>
        </el-dialog>

        <el-dialog
            title="添加数据源"
            v-model="dataSource.show"
            :close-on-click-modal="false"
            destroy-on-close
            width="450px"
        >
            <el-form
                v-loading="dataSource.loading"
                label-width="130px"
                class="flex-form"
            >
                <el-form-item label="数据源名称" required>
                    <el-input v-model="dataSource.name" placeholder="dataset-sql"></el-input>
                </el-form-item>
                <el-form-item label="数据库类型">
                    <el-select v-model="dataSource.databaseType">
                        <el-option
                            v-for="item in dataSource.databaseTypes" :value="item"
                            :label="item"
                            :key="item"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="IP" required>
                    <el-input
                        v-model="dataSource.host"
                        placeholder="192.168.10.1"
                        clearable
                    ></el-input>
                </el-form-item>
                <el-form-item label="端口" required>
                    <el-input
                        v-model="dataSource.port"
                        placeholder="3306"
                        clearable
                    ></el-input>
                </el-form-item>
                <el-form-item label="库名" required>
                    <el-input
                        v-model="dataSource.databaseName"
                        placeholder="test"
                        clearable
                    ></el-input>
                </el-form-item>
                <el-form-item label="数据库用户名">
                    <el-input
                        v-model="dataSource.userName"
                        autocomplete="new-password"
                        placeholder="admin"
                        clearable
                    ></el-input>
                </el-form-item>
                <el-form-item label="数据库密码">
                    <el-input
                        v-model="dataSource.password"
                        autocomplete="new-password"
                        placeholder="password"
                        type="password"
                        clearable
                    ></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button
                        class="ml10"
                        @click="pingTest"
                    >连接测试</el-button>
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
    import table from '@src/mixins/table';
    import DataSetPublicTips from './components/data-set-public-tips';
    import SelectMemberDialog from './components/select-member-dialog';

    let canLeave = false;

    export default {
        components: {
            DataSetPublicTips,
            SelectMemberDialog,
        },
        mixins: [table],
        data() {
            return {
                loading: false,

                // ui status
                tagInputValue:           '',
                options_tags:            [],
                public_member_info_list: [],

                getListApi:    '/union/tag/query',
                fillUrlQuery:  false,
                defaultSearch: true,
                watchRoute:    false,
                turnPageRoute: false,
                tagList:       [],

                // preview
                raw_data_list:     [],
                metadata_list:     [],
                data_set_header:   [],
                dataTypeFillVal:   '',
                data_type_options: ['Integer', 'Long', 'Double', 'Enum', 'String'],

                // help：https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#%E5%A4%84%E7%90%86-get-%E6%88%96%E8%80%85-test-%E8%AF%B7%E6%B1%82
                file_upload_options: {
                    files:               [],
                    target:              window.api.baseUrl + '/file/upload',
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
                fileStatusText: {
                    success:   '成功',
                    error:     '错误',
                    uploading: '上传中',
                    paused:    '已暂停',
                    waiting:   '等待中',
                },
                file_upload_attrs: {
                    accept: '.csv,.xls,.xlsx',
                },

                http_upload_filename: '',
                local_filename:       '',

                // model
                form: {
                    publicLevel:         'Public',
                    name:                '',
                    tags:                [],
                    description:         '',
                    public_member_list:  [],
                    data_set_add_method: 'HttpUpload',
                    filename:            '',
                    metadata_list:       [],
                    deduplication:       true,
                    databaseType:        'Database',
                    dataSourceId:        '',
                    sql:                 '',
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
                uploadTask:            {
                    visible:       false,
                    name:          '',
                    progress:      0,
                    estimate_time: 0,
                    colors:        [
                        { color: '#f56c6c', percentage: 20 },
                        { color: '#e6a23c', percentage: 40 },
                        { color: '#5cb87a', percentage: 60 },
                        { color: '#1989fa', percentage: 80 },
                        { color: '#6f7ad3', percentage: 100 },
                    ],
                    total_row_count:     0,
                    added_row_count:     0,
                    repeat_id_row_count: 0,
                },
                isCanClose:  false,
                addDataType: 'csv',
            };
        },
        watch: {
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
                deep: true,
            },
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.addDataType = this.$route.query.type;
            this.getDataSouceList();
            this.checkStorage();

            this.$bus.$on('loginAndRefresh', () => {
                this.getDataSouceList();
                this.getList();
            });
        },
        beforeRouteLeave(to, from, next) {
            if(canLeave) {
                canLeave = false;
                next();
            } else {
                this.$confirm('未保存的数据将会丢失! 确定要离开当前页面吗', '警告', {
                    type: 'warning',
                }).then(async () => {
                    canLeave = false;
                    next();
                });
            }
        },
        methods: {
            async checkStorage() {
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/member/service_status_check',
                    data: {
                        member_id: this.userInfo.member_id,
                    },
                });

                this.loading = false;
                if(code === 0) {
                    const { success } = data.status.storage;

                    if(!success) {
                        this.$alert('存储不可用! 请联系管理员', '警告!');
                    }
                }
            },

            afterTableRender(list) {
                this.tagList = list.map(item => {
                    return {
                        checked: false,
                        label:   item.tag_name,
                        id:      item.id,
                    };
                });
            },

            metadataPageChange(val) {
                const { page_size } = this.metadata_pagination;

                this.metadata_pagination.page_index = val;
                this.metadata_pagination.list = [];
                for(let i = page_size * (val - 1); i < val * page_size; i++) {
                    const item = this.metadata_list[i];

                    if(item) {
                        this.metadata_pagination.list.push(item);
                    }
                }
            },

            dataTypeChange(row) {
                this.metadata_list[row.$index].data_type = row.data_type;
            },

            dataTypeFill(val) {
                this.metadata_list.forEach(item => {
                    if(!item.data_type) {
                        item.data_type = val;
                    }
                });
                this.metadata_pagination.list.forEach(item => {
                    if(!item.data_type) {
                        item.data_type = val;
                    }
                });
            },

            async getDataSouceList() {
                const { code, data } = await this.$http.get('/data_source/query');

                if(code === 0) {
                    this.dataSource.dataSourceList = data.list;
                    if(data.list.length) {
                        const item = data.list[0];

                        this.form.databaseName = item.name;
                        this.form.dataSourceId = item.id;
                    } else {
                        this.form.databaseName = '';
                        this.form.dataSourceId = '';
                    }
                }
            },

            removeDataSource($event, item) {
                this.$confirm('警告', {
                    dangerouslyUseHTMLString: true,
                    type:                     'warning',
                    title:                    '警告',
                    message:                  `你确定要删除该数据源吗? 此操作不可撤销! <p>[${item.database_type}] ${ item.database_name } (${ item.host }:${ item.port })</p>`,
                }).then(async action => {
                    if(action === 'confirm') {
                        this.loading = true;
                        const { code } = await this.$http.post({
                            url:  '/data_source/delete',
                            data: {
                                id: item.id,
                            },
                        });

                        this.loading = false;
                        if(code === 0) {
                            this.$message.success('移除成功!');
                            this.getDataSouceList();
                        }
                    }
                });
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
                    this.getDataSouceList();
                }
            },

            dataBaseNameChanged(val) {
                const item = this.dataSource.dataSourceList.find(x => x.name === val);

                if(item) {
                    this.form.dataSourceId = item.id;
                }
            },

            dataCommentChange(row) {
                this.metadata_list[row.$index].comment = row.comment;
            },

            // preview
            async previewDataSet() {
                this.loading = true;
                this.data_preview_finished = false;

                this.form.filename = this.form.data_set_add_method === 'HttpUpload' ? this.http_upload_filename : this.local_filename;

                const params = {
                    filename:            this.form.filename,
                    data_set_add_method: this.form.data_set_add_method,
                };

                if(this.form.data_set_add_method === 'Database') {
                    params.sql = this.form.sql;
                    this.dataSource.dataSourceList.find(item => {
                        if (item.id === this.form.dataSourceId) {
                            params.dataSourceId = item.id;
                        }
                    });
                }
                const { code, data } = await this.$http.get({
                    url: '/data_set/preview',
                    params,
                });

                if (code === 0) {
                    this.data_set_header = data.header;
                    this.metadata_pagination.list = [];
                    this.raw_data_list = data.raw_data_list.map(item => {
                        for(const key in item) {
                            const val = item[key];

                            item[key] = String(val);
                        }
                        return item;
                    });
                    this.metadata_list = data.metadata_list.map((item, index) => {
                        item.$index = index;
                        item.comment = '';
                        return item;
                    });
                    this.metadata_pagination.total = data.metadata_list.length;

                    const length = this.metadata_pagination.page_size;

                    for(let i = 0; i < length; i++) {
                        const item = this.metadata_list[i];

                        if(item) {
                            this.metadata_pagination.list.push(item);
                        }
                    }
                    this.gridHeight = 41 * (data.raw_data_list.length + 1) + 1;
                    this.data_preview_finished = true;
                }
                this.loading = false;
            },

            // add files
            fileAdded(file) {
                this.file_upload_options.files = [file];
            },
            fileRemoved() {
                this.file_upload_options.files = [];
            },
            // upload completed
            async fileUploadComplete() {
                this.loading = true;
                this.data_preview_finished = false;
                const file = arguments[0].file;
                const { code, data } = await this.$http.get({
                    url:     '/file/merge',
                    timeout: 1000 * 60 * 2,
                    params:  {
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
                }
            },

            async showSelectMemberDialog() {
                const ref = this.$refs['SelectMemberDialog'];

                ref.show = true;
                ref.loadDataList();
            },

            tagInputConfirm() {
                const val = this.tagInputValue;

                if (val) {
                    this.tagList.push({
                        checked: true,
                        label:   val,
                    });
                }
                this.tagInputValue = '';
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

            selectMember(list) {
                this.public_member_info_list = list;
            },

            deleteSelectedMember(item, idx) {
                this.public_member_info_list.splice(idx, 1);
                const ref = this.$refs['SelectMemberDialog'];

                ref.checkedList.splice(ref.checkedList.indexOf(item), 1); // Ensure that the members currently removed in the parent component are also removed
            },

            async add() {
                if (!this.form.name) {
                    this.$message.error('请输入数据集名称！');
                    return;
                }
                this.form.tags = [];
                this.tagList.forEach(tag => {
                    if(tag.checked) {
                        this.form.tags.push(tag.label);
                    }
                });
                if (!this.form.tags || this.form.tags.length === 0) {
                    this.$message.error('请为数据集设置关键词！');
                    return;
                }

                const ids = [];

                for (const index in this.public_member_info_list) {
                    ids.push(this.public_member_info_list[index].id);
                }
                this.form.public_member_list = ids.join(',');

                if(this.form.publicLevel === 'PublicWithMemberList' && ids.length === 0){
                    this.$message.error('请选择可见成员！');
                    return;
                }

                this.loading = true;
                this.form.metadata_list = this.metadata_list;

                const { code, data } = await this.$http.post({
                    url:     '/data_set/add',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    this.form,
                });

                if (code === 0) {
                    if (data.repeat_data_count > 0) {
                        this.$message.success(`保存成功，数据集包含重复数据 ${data.repeat_data_count} 条，已自动去重。`);
                    } else {
                        this.$message.success('保存成功!');
                    }
                    setTimeout(() => {
                        this.getAddTask(data.id);
                    }, 500);
                }
                this.loading = false;
            },

            async getAddTask(id) {
                const { code, data } = await this.$http.get({
                    url:    '/data_set_task/detail',
                    params: {
                        id,
                    },
                });

                if(code === 0) {
                    if(data) {
                        const { estimate_time, name, data_set_id, progress, total_row_count, added_row_count, repeat_id_row_count, error_message } = data;

                        this.uploadTask.name = name;
                        this.uploadTask.progress = progress;
                        this.uploadTask.estimate_time = estimate_time / 1000;
                        this.uploadTask.visible = true;
                        this.uploadTask.total_row_count = total_row_count;
                        this.uploadTask.added_row_count = added_row_count;
                        this.uploadTask.repeat_id_row_count = repeat_id_row_count;
                        this.uploadTask.error_message = error_message;

                        // error in uploading, stop refreshing the interface
                        if (data.error_message || repeat_id_row_count) {
                            this.isCanClose = true;
                            if(data.error_message) return;
                        } else {
                            this.isCanClose = false;
                        }

                        if(this.uploadTask.visible) {
                            setTimeout(() => {
                                if(progress < 100) {
                                    // uploading
                                    this.getAddTask(id);
                                } else {
                                    // upload completed
                                    this.uploadTask.visible = false;

                                    canLeave = true;

                                    this.$router.push({
                                        name:  'data-view',
                                        query: { id: data_set_id },
                                    });
                                }
                            }, 1000);
                        }
                    }
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .page{overflow: visible;}
    .el-form{overflow-x: auto;}
    .el-icon-close{
        font-weight: bold;
        margin-right: -20px;
        color: $--color-danger;
        position: relative;
        top: 2px;
    }
    .el-pagination{overflow: auto;}
    .deduplication-tips{
        font-size: 14px;
        p:first-child{
            font-weight: bold;
            color: red;
            padding: 8px 0 5px 0;
        }
    }
    .c-grid{
        border: 1px solid #EBEEF5;
        position: relative;
        z-index: 1;
    }
    .el-divider--horizontal {
        margin: 8px 0 20px 0;
    }
    .uploader {
        position: relative;
        min-width: 380px;
    }
    .uploader-drop {border-radius: 5px;}
    .uploader-btn {background: #fff;}
    .uploader-list{
        :deep(.uploader-file-status){
            font-size: 12px;
            text-indent: 2px;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
        }
        :deep(.uploader-file-meta) {display: none;}
    }
    .data-set-upload-tip {
        font-size: 14px;
        text-align: left;
        margin-top: 8px;
        color: #ff5757;
        line-height: 130%;
    }
    .data-set-upload-tip li {
        list-style: inside;
    }
    .warning-tip {
        color: $--color-danger;
        line-height: 18px;
        font-weight: bold;
    }
    .save-btn {width: 100px;}
    .el-form-item .el-tag {
        height: 32px;
        line-height: 32px;
        margin: 0 5px 5px 0;
        vertical-align: top;
    }
    .button-new-tag {
        height: 32px;
        line-height: 30px;
        padding-top: 0;
        padding-bottom: 0;
    }
    .input-new-tag {
        width: 230px;
        vertical-align: top;
    }
    .tags-tips{
        color: #999;
        line-height: 16px;
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
    .upload-info {
        max-width: 275px;
        margin: 0 auto;
        span {
            color: #28c2d7;
        }
    }
    .flex-center {
        display: flex;
        align-items: center;
        justify-content: space-between;
        .el-icon-close {
            margin-right: unset;
            cursor: pointer;
            color: rgb(201, 199, 199);
        }
    }
</style>
