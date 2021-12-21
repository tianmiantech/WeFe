<template>
    <el-dialog
        v-model="show"
        width="75%"
        title="请选择数据集"
        destroy-on-close
        :close-on-click-modal="false"
    >
        <el-form
            inline
            size="mini"
            @submit.prevent
        >
            <el-form-item
                v-if="memberRole !== 'provider'"
                label="上传者："
            >
                <el-input
                    v-model="search.creator"
                    clearable
                />
            </el-form-item>
            <el-form-item label="名称：">
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>
            <el-form-item label="ID：">
                <el-input
                    v-model="search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="资源类型："
                label-width="100"
            >
                <el-select
                    v-model="search.dataResourceType"
                    filterable
                    clearable
                    @change="resourceTypeChange"
                    :disabled="isTypeDisabled"
                >
                    <el-option
                        v-for="item in sourceTypeList"
                        :key="item.label"
                        :value="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="search.dataResourceType === 'TableDataSet'"
                label="是否包含Y值："
                label-width="100"
            >
                <el-select
                    v-model="search.containsY"
                    filterable
                    clearable
                >
                    <el-option label="是" :value="true"></el-option>
                    <el-option label="否" :value="false"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="search.dataResourceType === 'ImageDataSet'"
                label="任务类型："
                label-width="100"
            >
                <el-select
                    v-model="search.forJobType"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="item in forJobTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <!-- <el-form-item v-if="memberRole === 'provider'" label="包含Y：">
                <el-select
                    v-model="search.contains_y"
                    style="width:80px;"
                    clearable
                >
                    <el-option label="是" value="true"></el-option>
                    <el-option label="否" value="false"></el-option>
                </el-select>
            </el-form-item> -->
            <el-button
                class="ml10 mb10"
                type="primary"
                @click="searchDataList({ memberId, resetPagination: true })"
            >
                查询
            </el-button>
        </el-form>

        <DataSetList
            ref="raw"
            source-type="Raw"
            :is-show="isShow"
            :data-sets="dataSets"
            :search-field="search"
            :contains-y="containsY"
            :data-add-btn="dataAddBtn"
            :emit-event-name="emitEventName"
            :project-type="projectType"
            @close-dialog="closeDialog"
            @selectDataSet="selectDataSet"
            @batchDataSet="batchDataSet"
        />
    </el-dialog>
</template>

<script>
    import DataSetList from './data-set-list';

    export default {
        components: {
            DataSetList,
        },
        props: {
            memberRole:   String,
            dataSets:     Array,
            containsY:    String,
            callbackFunc: {
                type:    Function,
                default: null,
            },
            dataAddBtn: {
                type:    Boolean,
                default: true,
            },
            emitEventName: String,
            interfaceApi:  String,
        },
        emits: ['selectDataSet', 'batchDataSet'],
        data() {
            return {
                show:        false,
                memberId:    '',
                jobRole:     '',
                projectType: '',
                myMemberId:  '',
                search:      {
                    id:         '',
                    name:       '',
                    creator:    '',
                    contains_y: '',
                },
                hideRelateSourceTab: false,
                isShow:              false,
                sourceTypeList:      [
                    {
                        label: 'TableDataSet',
                        value: 'TableDataSet',
                    },
                    {
                        label: 'ImageDataSet',
                        value: 'ImageDataSet',
                    },
                    {
                        label: 'BloomFilter',
                        value: 'BloomFilter',
                    },
                ],
                forJobTypeList: [
                    {
                        label: '目标检测',
                        value: 'detection',
                    },
                    {
                        label: '图像分类',
                        value: 'classify',
                    },
                ],
                isTypeDisabled:  false,
                checkedDataList: [],
            };
        },
        watch: {
            show: {
                handler(val) {
                    if (val) {
                        this.resetSearch();
                        this.isShow = val;
                        this.$nextTick(_ => {
                            this.$refs['raw'].isShowData = true;
                        });
                    }
                },
            },
        },
        methods: {
            closeDialog() {
                this.show = false;
            },

            resourceTypeChange() {
                this.search.containsY = '';
                this.search.forJobType = '';
            },

            resetSearch() {
                this.$nextTick(() => {
                    const $ref = this.$refs['raw'];

                    this.search = {
                        id:         '',
                        name:       '',
                        creator:    '',
                        contains_y: '',
                    };

                    if(this.containsY) {
                        this.search.source_type = 'Raw';
                        this.search.contains_y = true;
                    }

                    $ref.list = [];
                    $ref.pagination.page_index = 1;
                    $ref.pagination.page_size = 20;
                });
            },

            searchDataList({ memberId, resetPagination }) {
                this.loadDataList({ memberId, resetPagination, $data_set: this.checkedDataList });
            },

            async loadDataList({
                memberId,
                jobRole,
                resetPagination,
                $data_set,
                projectType,
            }) {
                this.checkedDataList = $data_set;
                // change memberId, reset search
                if (memberId && this.memberId !== memberId) {
                    this.resetSearch();
                }

                this.jobRole = jobRole || this.jobRole;
                this.projectType = projectType || this.projectType;
                await this.$nextTick((_)=>{}); // Asynchronous queue update dataResourceType field
                this.search.dataResourceType = this.projectType === 'DeepLearning' ? 'ImageDataSet' : 'TableDataSet';
                this.isTypeDisabled = true;
                
                if (memberId) {
                    this.memberId = memberId;
                }

                const { code, data } = await this.$http.get({
                    url: '/member/detail',
                });

                if(code === 0) {
                    this.myMemberId = data.member_id;

                    this.searchList({ resetPagination, $data_set });
                }
            },

            searchList(opt = {}) {
                let url;

                if(this.interfaceApi) {
                    // define API from parent
                    url = this.interfaceApi;
                } else {
                    // data_resource/query
                    // my own data set，search from board
                    if (this.memberId === this.myMemberId) {
                        url = 'data_resource/query';
                    } else {
                        // search from union
                        url = `/union/data_resource/query?member_id=${this.memberId}`;
                    }
                }

                const $ref = this.$refs['raw'];

                $ref.getDataList({ url, is_my_data_set: this.memberId === this.myMemberId, ...opt });
            },

            selectDataSet(item) {
                this.$emit('selectDataSet', item);
            },

            batchDataSet(batchlist) {
                this.$emit('batchDataSet', batchlist);
            },
        },
    };
</script>

<style lang="scss" scoped>
    .dataset-dialog {
        min-width: 800px;
    }
    .el-form{
        .el-form-item{
            margin-bottom: 10px;
        }
    }
</style>
