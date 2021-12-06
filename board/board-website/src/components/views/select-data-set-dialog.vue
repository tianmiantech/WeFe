<template>
    <el-dialog
        v-model="show"
        custom-class="mid-min-width"
        title="请选择数据集"
        destroy-on-close
        width="70%"
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
            <el-form-item v-if="memberRole === 'provider'" label="包含Y：">
                <el-select
                    v-model="search.contains_y"
                    style="width:80px;"
                    clearable
                >
                    <el-option label="是" value="true"></el-option>
                    <el-option label="否" value="false"></el-option>
                </el-select>
            </el-form-item>
            <el-button
                class="ml10 mb10"
                type="primary"
                @click="loadDataList({ memberId, resetPagination: true })"
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

            async loadDataList({
                memberId,
                jobRole,
                resetPagination,
                $data_set,
                projectType,
            }) {
                // change memberId, reset search
                if (memberId && this.memberId !== memberId) {
                    this.resetSearch();
                }

                this.jobRole = jobRole || this.jobRole;
                this.projectType = projectType || this.projectType;

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
                    // my own data set，search from board
                    if (this.memberId === this.myMemberId) {
                        if (this.projectType === 'DeepLearning') {
                            url = '/image_data_set/query';
                        } else {
                            url = this.jobRole === 'promoter' || this.jobRole === 'promoter_creator' ? `/table_data_set/query?member_id=${this.memberId}` : '/table_data_set/query';
                        }
                    } else {
                        // search from union
                        if (this.projectType === 'DeepLearning') {
                            url = `/union/image_data_set/query?member_id=${this.memberId}`;
                        } else {
                            url = `/union/table_data_set/query?member_id=${this.memberId}`;
                        }
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
