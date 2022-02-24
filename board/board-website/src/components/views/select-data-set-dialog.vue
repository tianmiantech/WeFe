<template>
    <el-dialog
        v-model="show"
        destroy-on-close
        title="请选择数据资源"
        :close-on-click-modal="false"
        width="75%"
    >
        <el-form
            inline
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
                    v-if="projectType === 'DeepLearning'"
                    v-model="search.dataResourceType"
                    :disabled="true"
                    filterable
                >
                    <el-option
                        label="ImageDataSet"
                        value="ImageDataSet"
                    />
                </el-select>
                <el-select
                    v-else
                    v-model="search.dataResourceType"
                    filterable
                    multiple
                    @change="resourceTypeChange"
                >
                    <el-option
                        v-for="item in sourceTypeList"
                        :key="item.label"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="projectType !== 'DeepLearning' && search.dataResourceType.length === 1 && search.dataResourceType[0] === 'TableDataSet'"
                label="是否包含Y值："
                label-width="100"
            >
                <el-select
                    v-model="search.containsY"
                    style="width: 90px"
                    filterable
                    clearable
                >
                    <el-option label="是" :value="true"></el-option>
                    <el-option label="否" :value="false"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="projectType === 'DeepLearning'"
                label="样本分类："
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
            :is-show="isShow"
            :data-sets="dataSets"
            :search-field="search"
            :contains-y="containsY"
            :data-add-btn="dataAddBtn"
            :emit-event-name="emitEventName"
            :project-type="projectType"
            :member-id="memberId"
            @close-dialog="closeDialog"
            @selectDataSet="selectDataSet"
            @batchDataSet="batchDataSet"
        />
    </el-dialog>
</template>

<script>
    import { mapGetters } from 'vuex';
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
                    id:               '',
                    name:             '',
                    creator:          '',
                    containsY:        '',
                    dataResourceType: '',
                },
                isShow:              false,
                hideRelateSourceTab: false,
                sourceTypeList:      [
                    {
                        label: 'TableDataSet',
                        value: 'TableDataSet',
                    },
                    {
                        label: '布隆过滤器',
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
                checkedDataList: [],
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
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
                        id:               '',
                        name:             '',
                        creator:          '',
                        containsY:        '',
                        dataResourceType: this.projectType === 'DeepLearning' ? ['ImageDataSet'] : ['TableDataSet', 'BloomFilter'],
                    };

                    if(this.containsY) {
                        this.search.containsY = true;
                    }

                    $ref.list = [];
                    $ref.pagination.page_index = 1;
                    $ref.pagination.page_size = 20;
                });
            },

            searchDataList({ memberId, resetPagination }) {
                this.loadDataList({ memberId, resetPagination, $data_set: this.checkedDataList });
            },

            loadDataList({
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

                this.$nextTick(_ => {
                    if(this.projectType === 'DeepLearning') {
                        this.search.dataResourceType = ['ImageDataSet'];
                    } else if(this.search.dataResourceType.length === 0) {
                        this.search.dataResourceType = ['TableDataSet', 'BloomFilter'];
                    }

                    if (memberId) {
                        this.memberId = memberId;
                    }

                    this.myMemberId = this.userInfo.member_id;
                    this.searchList({ resetPagination, $data_set });
                });
            },

            searchList(opt = {}) {
                let url;

                if(this.interfaceApi) {
                    // define API from parent
                    url = this.interfaceApi;
                } else {
                    // my own data set，search from board
                    if (this.memberId === this.myMemberId) {
                        url = '/data_resource/query';
                    } else {
                        // search from union
                        url = `/union/data_resource/query?member_id=${this.memberId}`;
                    }

                }

                this.$nextTick(_ => {
                    const $ref = this.$refs['raw'];

                    $ref.getDataList({ url, is_my_data_set: this.memberId === this.myMemberId, ...opt });
                });
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
