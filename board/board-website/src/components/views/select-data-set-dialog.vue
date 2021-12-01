<template>
    <el-dialog
        v-model="show"
        custom-class="dataset-dialog"
        title="请选择数据集"
        destroy-on-close
        width="70%"
    >
        <el-form
            inline
            @submit.prevent
        >
            <el-form-item
                label="名称："
                label-width="60px"
            >
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="ID："
                label-width="60px"
            >
                <el-input
                    v-model="search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item>
                <el-checkbox
                    v-model="search.containsY"
                    clearable
                >
                    是否包含Y
                </el-checkbox>
            </el-form-item>
            <el-button
                class="ml10 mb20"
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
                    id:        '',
                    name:      '',
                    containsY: '',
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
                    const ref = this.$refs['raw'];

                    this.search = {
                        id:   '',
                        name: '',
                    };

                    if(this.containsY) {
                        this.search.source_type = 'Raw';
                    }

                    ref.list = [];
                    ref.pagination.page_index = 1;
                    ref.pagination.page_size = 20;
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
                            url = this.jobRole === 'promoter' || this.jobRole === 'promoter_creator' ? `/data_set/query?contains_y=${this.containsY}&member_id=${this.memberId}` : '/data_set/query';
                        }
                    } else {
                        // search from union
                        if (this.projectType === 'DeepLearning') {
                            url = '/union/image_data_set/query';
                        } else {
                            url = `/union/data_set/query?member_id=${this.memberId}`;
                        }
                    }
                }

                const ref = this.$refs['raw'];

                ref.getDataList({ url, is_my_data_set: this.memberId === this.myMemberId, ...opt });
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
</style>
