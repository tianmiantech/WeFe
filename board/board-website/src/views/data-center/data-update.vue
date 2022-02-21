<template>
    <el-card
        v-loading="loading"
        shadow="never"
        class="page"
    >
        <el-form :model="form">
            <el-row :gutter="30">
                <el-col :span="10">
                    <el-form-item
                        prop="name"
                        label="数据资源名称"
                        :rules="[
                            { required: true, message: '数据资源名称必填!' }
                        ]"
                    >
                        <el-input
                            v-model="form.name"
                            size="large"
                        />
                    </el-form-item>
                    <el-form-item prop="tag">
                        <el-tag
                            v-for="tag in form.tags"
                            :key="tag"
                            closable
                            :disable-transitions="false"
                            @close="handleCloseTag(tag)"
                        >
                            {{ tag }}
                        </el-tag>
                        <el-autocomplete
                            v-if="tagInputVisible"
                            ref="saveTagInput"
                            v-model="tagInputValue"
                            :fetch-suggestions="getDataSetTags"
                            :trigger-on-focus="false"
                            :loading="loading"
                            class="input-new-tag"
                            @select="handleTagInputConfirm"
                            @keyup.enter="handleTagInputConfirm"
                        />
                        <el-button
                            v-else
                            class="button-new-tag ml10"
                            size="small"
                            type="success"
                            @click="showInputTag"
                        >
                            + 关键词
                        </el-button>
                        <span class="tags-tips f12 ml10">为数据资源设置关键词，方便大家快速了解你 ：）</span>
                    </el-form-item>
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
                <el-col
                    :span="14"
                    style="position: relative;"
                >
                    <fieldset style="min-height:240px;">
                        <legend>可见性</legend>
                        <el-form-item>
                            <el-radio
                                v-model="form.public_level"
                                label="Public"
                            >
                                对所有成员可见
                            </el-radio>
                            <el-radio
                                v-model="form.public_level"
                                label="OnlyMyself"
                            >
                                仅自己可见
                            </el-radio>
                            <el-radio
                                v-model="form.public_level"
                                label="PublicWithMemberList"
                            >
                                对指定成员可见
                            </el-radio>
                        </el-form-item>
                        <el-form-item>
                            <div v-if="form.public_level === 'PublicWithMemberList'">
                                <el-button
                                    size="small"
                                    class="mr10"
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

                                        <el-icon class="el-icon-close" @click="deleteSelectedMember(item, index)">
                                            <elicon-close />
                                        </el-icon>
                                    </li>
                                </ul>
                            </div>
                        </el-form-item>
                        <DataSetPublicTips v-if="form.public_level != 'OnlyMyself'" />
                    </fieldset>
                </el-col>
            </el-row>
            <el-row :gutter="30" v-if="addType === 'csv'">
                <el-col :span="10">
                    <h4 class="mt10 mb20">
                        字段信息：
                        <el-select
                            v-model="dataTypeFillVal"
                            class="float-right"
                            size="small"
                            clearable
                            style="width: 140px;"
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
                        max-height="500"
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
                    <h4 class="m5">数据资源预览：</h4>
                    <DataSetPreview ref="DataSetPreview" />
                </el-col>
            </el-row>
            <el-row v-if="addType === 'img'" :gutter="30" style="padding: 0 20px;">
                <h4 style="margin-bottom: 6px;">数据资源预览</h4>
                <preview-image-list ref="PreviewImageListRef" />
            </el-row>
            <el-button
                class="save-btn mt20"
                type="primary"
                size="large"
                @click="update"
            >
                保存
            </el-button>
        </el-form>

        <SelectMember
            ref="SelectMemberDialog"
            :block-my-id="true"
            :public-member-info-list="public_member_info_list"
            @select-member="selectMember"
        />
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import DataSetPreview from '@comp/views/data_set-preview';
    import DataSetPublicTips from './components/data-set-public-tips';
    import PreviewImageList from './components/preview-image-list';
    import SelectMember from './components/select-member';

    export default {
        components: {
            DataSetPreview,
            DataSetPublicTips,
            SelectMember,
            PreviewImageList,
        },
        data() {
            return {
                id:      this.$route.query.id,
                loading: false,

                // ui status
                tagInputVisible:         false,
                tagInputValue:           '',
                options_tags:            [],
                public_member_info_list: [],

                dataTypeFillVal:   '',
                // preview
                data_type_options: ['Integer', 'Double', 'Enum', 'String'],

                // model
                form: {
                    public_level:       '',
                    contains_y:         false,
                    name:               '',
                    tags:               [],
                    description:        '',
                    public_member_list: [],
                    metadata_list:      [],
                },
                raw_data_list:       [],
                metadata_pagination: {
                    list:       [],
                    total:      0,
                    page_size:  20,
                    page_index: 1,
                },
                addType: 'csv',
                search:  {
                    page_index: 1,
                    page_size:  20,
                    label:      '',
                    labeled:    '',
                    total:      1,
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.addType = this.$route.query.type || 'csv';
            this.getData();
        },
        mounted() {
            if (this.addType === 'csv') {
                this.loadDataSetColumnList();
                this.$refs['DataSetPreview'].loadData(this.id);
            } else if (this.addType === 'img') {
                this.$refs['PreviewImageListRef'].methods.getSampleList(this.id);
            }
        },
        methods: {
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
            dataCommentChange(row) {
                this.metadata_list[row.$index].comment = row.comment;
            },

            async loadDataSetColumnList(){
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url: '/table_data_set/column/list?data_set_id=' + this.id,
                });

                if (code === 0) {
                    this.raw_data_list = data.list;
                    this.metadata_list = [];
                    this.metadata_pagination.list = [];
                    this.metadata_list = data.list.map((item, index) => {
                        item.$index = index;
                        item.comment = item.comment || '';
                        return item;
                    });
                    this.metadata_pagination.total = data.list.length;

                    const length = this.metadata_pagination.page_size;

                    for(let i = 0; i < length; i++) {
                        const item = data.list[i];

                        if(item) {
                            this.metadata_pagination.list.push(item);
                        }
                    }
                }
                this.loading = false;
            },

            showSelectMemberDialog() {
                const ref = this.$refs['SelectMemberDialog'];

                ref.show = true;
                ref.loadDataList();
            },

            selectMember(list) {
                this.public_member_info_list = list;
            },

            deleteSelectedMember(item, idx) {
                this.public_member_info_list.splice(idx, 1);
                const ref = this.$refs['SelectMemberDialog'];

                ref.checkedList.splice(ref.checkedList.indexOf(item), 1); // Ensure that the members currently removed in the parent component are also removed
            },

            async getData() {
                this.loading = true;
                const map = {
                    BloomFilter: '/bloom_filter/detail',
                    img:         '/image_data_set/detail',
                    csv:         '/table_data_set/detail',
                };
                const { code, data } = await this.$http.get({
                    url: `${map[this.addType]}?id=` + this.id,
                });

                if (code === 0) {
                    this.form = Object.assign(this.form, data);
                    if (this.form.public_level === 'PublicWithMemberList') {
                        for (const key in data.public_member_info_list) {
                            this.public_member_info_list.push({
                                id:   key,
                                name: data.public_member_info_list[key],
                            });
                        }
                    }

                    this.form.tags = data.tags.split(',').filter(x => {
                        return x;
                    });
                }
                this.loading = false;
            },

            handleCloseTag(tag) {
                this.form.tags.splice(this.form.tags.indexOf(tag), 1);
            },

            handleTagInputConfirm() {
                const tagInputValue = this.tagInputValue;

                if (tagInputValue) {
                    this.form.tags.push(tagInputValue);
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
            async update() {

                if (this.form.public_level === 'PublicWithMemberList') {
                    const ids = [];

                    for (const index in this.public_member_info_list) {
                        ids.push(this.public_member_info_list[index].id);
                    }
                    this.form.public_member_list = ids.join(',');

                    if(ids.length === 0){
                        this.$message.error('请选择可见成员！');
                        return;
                    }
                }else{
                    this.form.public_member_list = '';
                }

                this.loading = true;
                const map = {
                    csv:    '/table_data_set/update',
                    img:    '/image_data_set/update',
                    filter: '/bloom_filter/update',
                };
                const { code } = await this.$http.post({
                    url:     map[this.addType],
                    timeout: 1000 * 60 * 2,
                    data:    {
                        ...this.form,
                        metadata_list: this.metadata_list,
                    },
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.$router.push({
                        name:  'data-view',
                        query: { id: this.id, type: this.addType },
                    });
                }
                this.loading = false;
            },

            async getDataSetTags(keyword, cb) {
                this.options_tags = [];
                if (keyword) {
                    this.loading = true;
                    const { code, data } = await this.$http.post({
                        url:  '/table_data_set/all_tags',
                        data: {
                            tag: keyword,
                        },
                    });

                    this.loading = false;
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
    .page{overflow: visible;}
    .el-pagination{overflow: auto;}
    .el-upload {position: relative;}
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
    .warning-tip {
        color: $--color-danger;
        line-height: 18px;
        font-weight: bold;
    }
    .save-btn {
        width: 100px;
    }
    .el-tag + .el-tag {
        margin-left: 10px;
    }
    .input-new-tag {
        width: 90px;
        margin-left: 10px;
        vertical-align: bottom;
    }
    .tags-tips{
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
    .flex-center {
        display: flex;
        align-items: center;
        .el-icon-close {
            margin-right: unset;
            cursor: pointer;
            color: rgb(201, 199, 199);
            padding-left: 30px;
        }
    }
</style>
