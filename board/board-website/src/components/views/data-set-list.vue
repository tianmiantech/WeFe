<template>
    <div class="data-set-list">
        <div class="flexbox">
            <el-alert
                v-if="projectType !== 'DeepLearning' && containsY === 'true'"
                :title="containsY === 'true' ? '注意: 发起方只能选择[包含] y 值的数据资源' : ''"
                :closable="false"
                type="warning"
            />
            <slot name="data-add">
                <div
                    v-if="dataAddBtn"
                    class="data-add mb10"
                    :style="containsY != 'true' || containsY !== 'false' ? 'width: 100%;':''"
                >
                    <router-link
                        :to="{ name: 'data-add-transition' }"
                        target="_blank"
                    >
                        <el-button style="display:block;" type="primary" size="small">
                            上传数据资源
                            <el-icon>
                                <elicon-arrow-right />
                            </el-icon>
                        </el-button>
                    </router-link>
                </div>
            </slot>
        </div>

        <el-table
            v-loading="tableLoading"
            max-height="500"
            :data="list"
            stripe
            border
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column
                label="名称 / Id"
                min-width="220"
            >
                <template v-slot="scope">
                    <template v-if="isFlow">
                        {{ scope.row.data_resource ? scope.row.data_resource.name : scope.row.name }}
                        <p class="p-id">{{ scope.row.data_set_id || scope.row.id || scope.row.data_resource_id }}</p>
                    </template>
                    <template v-else>
                        {{ scope.row.name }}
                        <p class="p-id">{{ scope.row.data_set_id || scope.row.id || scope.row.data_resource_id }}</p>
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                v-if="auditStatus"
                label="授权情况"
                min-width="100"
            >
                <template v-slot="scope">
                    <el-tag v-if="scope.row.audit_status === 'agree'">已授权</el-tag>
                    <el-tag
                        v-else
                        type="danger"
                    >
                        {{ scope.row.audit_status === 'disagree' ? '已拒绝' : '等待授权' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                label="资源类型"
                prop="data_resource_type"
                align="center"
                width="130"
            >
                <template v-slot="scope">
                    {{ sourceTypeMap[scope.row.data_resource_type ]}}
                </template>
            </el-table-column>
            <el-table-column
                v-if="projectType !== 'DeepLearning'"
                label="包含Y"
                width="100"
                align="center"
            >
                <template v-slot="scope">
                    <p v-if="scope.row.data_resource_type === 'TableDataSet'">
                        <el-icon v-if="scope.row.data_resource && scope.row.data_resource.contains_y" class="el-icon-check" style="color: #67C23A">
                            <elicon-check />
                        </el-icon>
                        <el-icon v-else-if="scope.row.contains_y" class="el-icon-check" style="color: #67C23A">
                            <elicon-check />
                        </el-icon>
                        <el-icon v-else class="el-icon-close">
                            <elicon-close />
                        </el-icon>
                    </p>
                    <p v-else>-</p>
                </template>
            </el-table-column>
            <el-table-column
                label="关键词"
                min-width="120"
            >
                <template v-slot="scope">
                    <template v-if="scope.row.data_resource && scope.row.data_resource.tags || scope.row.tags">
                        <template v-for="(item, index) in (isFlow ? scope.row.data_resource.tags.split(',') : (scope.row.tags ? scope.row.tags.split(',') : []))" :key="index">
                            <el-tag
                                v-show="item"
                                class="mr10"
                            >
                                {{ item }}
                            </el-tag>
                        </template>
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="数据信息"
                prop="row_count"
                min-width="160"
            >
                <template v-slot="scope">
                    <p v-if="projectType === 'DeepLearning'">
                        样本量/已标注：{{ isFlow ? scope.row.data_resource.total_data_count : scope.row.total_data_count }}/{{isFlow ? scope.row.data_resource.labeled_count : scope.row.labeled_count }}
                        <br>
                        标注进度：{{ ((scope.row.data_resource ? scope.row.data_resource.labeled_count : scope.row.labeled_count) / (scope.row.data_resource ? scope.row.data_resource.total_data_count : scope.row.total_data_count) * 100).toFixed(2) }}%
                        <br>
                        样本分类：
                        <template v-if="scope.row.data_resource">
                            {{scope.row.data_resource.for_job_type === 'classify' ? '图像分类' : scope.row.data_resource.for_job_type === 'detection' ? '目标检测' : '-'}}
                        </template>
                        <template v-else>
                            {{scope.row.for_job_type === 'classify' ? '图像分类' : scope.row.for_job_type === 'detection' ? '目标检测' : '-'}}
                        </template>
                    </p>
                    <p v-else>
                        特征量：{{ scope.row.data_resource ? scope.row.data_resource.feature_count : scope.row.feature_count || '-' }}
                        <br>
                        样本量：{{ scope.row.data_resource ? scope.row.data_resource.total_data_count : scope.row.total_data_count }}
                        <template v-if="scope.row.data_resource ? scope.row.data_resource.contains_y && scope.row.data_resource.y_positive_sample_count : scope.row.contains_y && scope.row.y_positive_sample_count">
                            <br>
                            正例样本数量：{{ scope.row.data_resource ? scope.row.data_resource.y_positive_sample_count : scope.row.y_positive_sample_count }}
                            <br>
                            正例样本比例：{{((scope.row.data_resource ? scope.row.data_resource.y_positive_sample_ratio : scope.row.y_positive_sample_ratio) * 100).toFixed(1)}}%
                        </template>
                    </p>
                </template>
            </el-table-column>
            <el-table-column
                v-if="isFlow"
                label="参与任务次数"
                prop="usage_count_in_job"
                min-width="110"
            >
                <template v-slot="scope">
                    {{ scope.row.data_resource ? scope.row.data_resource.usage_count_in_job : 0 }}
                </template>
            </el-table-column>
            <el-table-column
                v-else
                label="参与任务次数"
                prop="usage_count_in_job"
                min-width="110"
            />
            <el-table-column
                :label="userInfo.member_id === memberId ? '上传者' : '上传时间'"
                min-width="160"
            >
                <template v-slot="scope">
                    <span v-if="userInfo.member_id === memberId">{{ scope.row.creator_nickname }}<br></span>
                    {{ dateFormat(scope.row.created_time) }}
                </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                label="选择数据资源"
                width="140"
            >
                <template v-slot="scope">
                    <slot name="operation">
                        <div class="cell-reverse">
                            <el-tooltip
                                v-if="is_my_data_set"
                                :disabled="scope.row.data_resource_type === 'BloomFilter'"
                                content="预览数据"
                                placement="top"
                            >
                                <el-button
                                    circle
                                    type="info"
                                    :disabled="scope.row.data_resource_type === 'BloomFilter'"
                                    @click="showDataSetPreview(scope.row)"
                                >
                                    <el-icon>
                                        <elicon-view />
                                    </el-icon>
                                </el-button>
                            </el-tooltip>
                            <el-switch
                                v-model="scope.row.$checked"
                                :disabled="scope.row.deleted || scope.row.$unchanged || scope.row.audit_status === 'disagree' || scope.row.audit_status === 'auditing'"
                                active-color="#35c895"
                                @change="isFlow ? selectDataSet(scope.row, scope.$index) : selectMemberSwitch(scope.row, scope.$index)"
                            />
                        </div>
                    </slot>
                </template>
            </el-table-column>
        </el-table>
        <div
            v-if="pagination.total"
            :class="['pagination', 'text-r', 'confirm-bar']"
        >
            <el-pagination
                :pager-count="5"
                :total="pagination.total"
                :page-sizes="[10, 20, 30, 40, 50]"
                :page-size="pagination.page_size"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
            <div v-if="!isFlow" class="confirm-bar">
                <p>已选择 <span>{{ checkedCount }}</span> 项</p>
                <el-button
                    type="primary"
                    :disabled="!checkedCount"
                    @click="addConfirm"
                >
                    确定添加
                </el-button>
            </div>
        </div>

        <el-dialog
            title="数据预览"
            v-model="dataSetPreviewDialog"
            destroy-on-close
            append-to-body
            width="60%"
        >
            <DataSetPreview v-if="projectType === 'MachineLearning'" ref="DataSetPreview" />
            <PreviewImageList v-if="projectType === 'DeepLearning'" ref="PreviewImageList" />
        </el-dialog>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table';
    import DataSetPreview from '@comp/views/data_set-preview';
    import PreviewImageList from '@views/data-center/components/preview-image-list.vue';

    export default {
        components: {
            DataSetPreview,
            PreviewImageList,
        },
        mixins: [table],
        props:  {
            api:         Object,
            containsY:   String,
            auditStatus: Boolean,
            dataAddBtn:  {
                type:    Boolean,
                default: true,
            },
            searchField: {
                type:    Object,
                default: _ => {},
            },
            paramsExclude: Array,
            emitEventName: String,
            dataSets:      Array,
            isShow:        Boolean,
            projectType:   String,
            memberId:      String,
        },
        emits: ['list-loaded', 'close-dialog', 'selectDataSet', 'batchDataSet'],
        data() {
            return {
                is_my_data_set:       false,
                dataSetPreviewDialog: false,

                tableLoading:    false,
                watchRoute:      false,
                turnPageRoute:   false,
                checkAll:        false,
                isIndeterminate: false,
                checkedList:     [],
                isFlow:          false, // flow detail page
                oldCheckedList:  [],    // checked list from parent component
                batchList:       [],
                isShowData:      false,
                requestMethod:   'post',
                sourceTypeMap:   {
                    BloomFilter:  '布隆过滤器',
                    ImageDataSet: 'ImageDataSet',
                    TableDataSet: '数据集',
                },
            };
        },
        computed: {
            checkedCount() {
                let total = 0;

                this.list.forEach(item => {
                    if (item.$checked) {
                        total++;
                    }
                });
                return total;
            },
            ...mapGetters(['userInfo']),
        },
        watch: {
            isShow: {
                handler(val) {
                    if (val) {
                        this.batchList = [];
                    }
                },
            },
        },
        methods: {
            // preview dataset
            showDataSetPreview(item){
                this.dataSetPreviewDialog = true;
                this.$nextTick(() =>{
                    if (this.projectType === 'MachineLearning') {
                        this.$refs['DataSetPreview'].loadData(item.data_resource && item.data_resource.id ? item.data_resource.id : item.id);
                    } else if (this.projectType === 'DeepLearning') {
                        this.$refs.PreviewImageList.methods.getSampleList(item.data_resource && item.data_resource.id ? item.data_resource.id : item.id);
                    }
                });
            },

            async getDataList({
                url,
                to,
                resetPagination,
                is_my_data_set = false,
                $data_set,
            }) {
                this.is_my_data_set = is_my_data_set;
                this.getListApi = url;
                this.checkAll = false;
                this.tableLoading = true;
                this.isIndeterminate = false;
                this.search = this.searchField;
                if(this.projectType === 'DeepLearning' && !this.isFlow) {
                    this.search.dataResourceType = ['ImageDataSet'];
                }
                if(this.search.dataResourceType) {
                    const flag = this.search.dataResourceType.includes('TableDataSet');

                    if(flag && this.containsY === true) {
                        this.search.containsY = true;
                    } else if (flag && this.containsY === false) {
                        this.search.containsY = false;
                    }
                }
                this.unUseParams = this.$props.paramsExclude;
                await this.getList({ to, resetPagination });

                this.oldCheckedList = $data_set || [];
                this.list.forEach((item, index) => {
                    item.$checked = false;
                    item.$unchanged = false;
                    this.list[index] = item;
                    this.oldCheckedList.find(sitem => {
                        if (item.data_resource && item.data_resource.data_resource_id === sitem.data_set_id || item.data_resource_id === sitem.data_set_id ) {
                            item.$checked = true;
                            item.$unchanged = true;
                        }
                    });
                });
                setTimeout(() => {
                    this.tableLoading = false;
                }, 300);
            },

            afterTableRender() {
                this.$emit('list-loaded', this.list);
            },

            selectAllChange(val) {
                this.checkAll = val;
                this.isIndeterminate = false;
                this.list.forEach((item, index) => {
                    item.$checked = val;
                    this.list[index] = item;
                });
            },

            toggleRowSelection(row) {
                let checkedLength = 0;

                this.list.forEach((item, index) => {

                    if(item.$checked) {
                        checkedLength++;
                    }
                    this.list[index] = item;
                });
                if(row.$checked) {
                    if(checkedLength === this.list.length) {
                        this.checkAll = true;
                        this.isIndeterminate = false;
                    }
                } else {
                    if(checkedLength === 0) {
                        this.checkAll = false;
                        this.isIndeterminate = false;
                    } else {
                        if(this.checkAll) {
                            this.isIndeterminate = true;
                        } else {
                            this.checkAll = false;
                        }
                    }
                }
            },

            // check & emit events
            selectDataSet(item, idx) {
                if(this.auditStatus && item.audit_status !== 'agree') {
                    return this.$message({
                        type:                     'error',
                        dangerouslyUseHTMLString: true,
                        message:                  '数据资源暂未授权, 无法使用! <div class="mt10"><strong>请先在项目详情中对数据进行授权!</strong></div>',
                    });
                }
                item.$source_page = this.emitEventName;
                if (this.isFlow) this.list[idx] = item;
                this.$emit('selectDataSet', item);
                this.$bus.$emit('selectDataSet', item);
            },

            cancelPopup() {
                this.$emit('close-dialog');
            },

            selectMemberSwitch(item, idx) {
                this.list[idx] = item;
                if (item.$checked) {
                    this.checkedList.push(item);
                } else {
                    this.removeByValue(this.checkedList, 'id', item.id);
                }
            },
            removeByValue(arr, attr, value) {
                let index = 0;

                for(const i in arr){
                    if(arr[i][attr] === value){
                        index = i;
                        break;
                    }
                }
                arr.splice(index, 1);
            },

            addConfirm() {
                // from create project
                if (this.isShowData) {
                    this.batchList = [];
                }
                this.list.forEach(item => {
                    if (item.$checked && !item.$unchanged) {
                        this.batchList.push(item);
                    }
                });
                if(this.batchList.length) {
                    this.$emit('batchDataSet', this.batchList);
                }
                this.$emit('close-dialog');
            },
        },
    };
</script>

<style lang="scss" scoped>
    .data-add{
        width:200px;
        text-align:right;
    }
    .el-alert{
        width: auto;
        height: 30px;
        min-width: 300px;
    }
    .pagination{
        display: flex;
        margin-top: 20px;
    }
    .el-pagination{
        max-width: 90%;
        overflow: auto;
    }
    .btns{
        text-align: right;
        flex: 1;
    }
    .cell-reverse {
        display: flex;
        justify-content: space-around;
        align-items: center;
    }
    .confirm-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        white-space: nowrap;
        padding: 2px 5px;
        p {
            margin-right: 10px;
            span {
                color: #4D84F7;
            }
        }
    }
</style>
