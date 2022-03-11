<template>
    <el-card
        name="参与成员"
        class="nav-title mb30"
        shadow="never"
    >
        <h3 class="mb10 card-title">参与成员</h3>
        <el-tabs
            v-if="promoter.member_id"
            v-model="memberTabName"
            type="border-card"
        >
            <el-tab-pane :name="`${promoter.member_id}-${promoter.member_role}`">
                <template #label>
                    发起方: {{ promoter.member_name }}
                    <span v-if="form.promoterList.length" class="f12 color-danger"> &lt;创建者&gt; </span>
                    <MemberServiceStatus :status="promoter.$serviceStatus" :onlyIcon="true" />
                </template>

                <p class="f12" style="color: #999;">
                    点击刷新服务状态:
                    <MemberServiceStatus :status="promoter.$serviceStatus" />
                </p>

                <p
                    v-if="promoter.$error"
                    class="service-offline f12 mb10"
                >
                    {{ promoter.$error }}
                </p>

                <el-button
                    v-if="form.isCreator && !form.closed"
                    type="primary"
                    @click="addDataSet('promoter_creator', 0, promoter.member_id, promoter.$data_set)"
                >
                    + 添加资源到此项目
                </el-button>

                <el-table
                    v-if="promoter.$data_set.length"
                    :data="promoter.$data_set"
                    max-height="520px"
                    class="mt10"
                    border
                    stripe
                >
                    <el-table-column type="index" />
                    <el-table-column
                        label="数据资源"
                        width="260"
                    >
                        <template v-slot="scope">
                            <template v-if="scope.row.data_resource">
                                <router-link :to="{
                                    name: scope.row.member_id === userInfo.member_id ? 'data-view' : 'union-data-view',
                                    query: {
                                        id: scope.row.data_resource.data_resource_id,
                                        type: projectType === 'DeepLearning' ? 'img' : scope.row.data_resource_type === 'BloomFilter' ? 'BloomFilter' : 'csv',
                                        data_resource_type: scope.row.data_resource_type,
                                    }
                                }">
                                    {{ scope.row.data_resource.name }}
                                </router-link>
                                <el-tag v-if="scope.row.data_resource_type === 'BloomFilter'" class="ml5" size="small">
                                    bf
                                </el-tag>
                                <br>
                                <span>{{ scope.row.data_resource.data_resource_id }}</span>
                            </template>
                        </template>
                    </el-table-column>
                    <el-table-column label="数据类型" min-width="100">
                        <template v-slot="scope">
                            {{ sourceTypeMap[scope.row.data_resource_type] }}
                        </template>
                    </el-table-column>
                    <el-table-column label="关键词">
                        <template v-slot="scope">
                            <template v-if="scope.row.data_resource && scope.row.data_resource.tags">
                                <template
                                    v-for="(item, index) in scope.row.data_resource.tags.split(',')"
                                    :key="index"
                                >
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
                    <el-table-column v-if="projectType === 'MachineLearning'" label="数据量" min-width="150">
                        <template v-slot="scope">
                            <p v-if="scope.row.data_resource_type === 'BloomFilter'">
                                样本量：{{ scope.row.data_resource.total_data_count }}
                                <br>
                                主键组合方式: {{ scope.row.data_resource.hash_function }}
                            </p>
                            <template v-else>
                                特征量：{{ scope.row.data_resource.feature_count }}
                                <br>
                                样本量：{{ scope.row.data_resource.total_data_count }}
                            </template>
                        </template>
                    </el-table-column>
                    <el-table-column
                        label="使用次数"
                        width="80"
                    >
                        <template v-slot="scope">
                            {{ scope.row.data_resource ? scope.row.data_resource.usage_count_in_job : '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column v-if="projectType === 'MachineLearning'" label="是否包含 Y">
                        <template v-slot="scope">
                            {{ scope.row.data_resource && scope.row.data_resource.contains_y ? '是' : '否' }}
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="projectType === 'DeepLearning'"
                        label="样本分类"
                        prop="for_job_type"
                        width="100"
                    >
                        <template v-slot="scope">
                            <template v-if="scope.row.data_resource">
                                {{scope.row.data_resource.for_job_type === 'classify' ? '图像分类' : scope.row.data_resource.for_job_type === 'detection' ? '目标检测' : '-'}}
                            </template>
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="projectType === 'DeepLearning'"
                        label="数据总量"
                        width="80"
                    >
                        <template v-slot="scope">
                            {{ scope.row.data_resource ? scope.row.data_resource.total_data_count : 0 }}
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="projectType === 'DeepLearning'"
                        label="已标注"
                        prop="labeled_count"
                        width="100"
                    >
                        <template v-slot="scope">
                            <template v-if="scope.row.data_resource">
                                {{scope.row.data_resource ? scope.row.data_resource.labeled_count : scope.row.labeled_count}}
                            </template>
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="projectType === 'DeepLearning'"
                        label="标注状态"
                        prop="label_completed"
                        width="100"
                    >
                        <template v-slot="scope">
                            <template v-if="scope.row.data_resource">
                                {{scope.row.data_resource.label_completed ? '已完成' : '标注中'}}
                            </template>
                        </template>
                    </el-table-column>
                    <el-table-column
                        label="状态"
                        width="100"
                    >
                        <template v-slot="scope">
                            {{ scope.row.audit_status === 'auditing' ? '待授权' : scope.row.audit_status === 'disagree' ? '已拒绝' : scope.row.audit_status === 'agree' ? '已授权' : '-' }}
                            <p
                                v-if="scope.row.audit_status !== 'agree'"
                                class="color-danger"
                            >
                                {{ scope.row.audit_comment }}
                            </p>
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="!form.closed && !promoter.exited && (promoter.audit_status !== 'disagree' && promoter.member_id === userInfo.member_id || form.isCreator)"
                        min-width="160"
                        label="操作"
                        fixed="right"
                    >
                        <template v-slot="scope">
                            <el-tooltip
                                v-if="!scope.row.deleted && scope.row.member_id === userInfo.member_id"
                                :disabled="scope.row.data_resource_type === 'BloomFilter'"
                                content="预览数据"
                                placement="top"
                            >
                                <el-button
                                    circle
                                    type="info"
                                    class="dataset-preview mr5"
                                    :disabled="scope.row.data_resource_type === 'BloomFilter'"
                                    @click="showDataSetPreview(scope.row)"
                                >
                                    <el-icon>
                                        <elicon-view />
                                    </el-icon>
                                </el-button>
                            </el-tooltip>
                            <!--
                                1. 数据资源未被删除
                                2. 成员是 promoter or 成员是自己
                            -->
                            <el-button
                                v-if="form.isPromoter || scope.row.member_id === userInfo.member_id"
                                circle
                                type="danger"
                                class="mr10"
                                icon="elicon-delete"
                                @click="removeDataSet(scope.row, scope.$index)"
                            />
                            <template v-if="scope.row.deleted">
                                该数据资源已被移除
                            </template>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane
                v-for="(member, memberIndex) in form.promoterList"
                :key="`${member.member_id}-${member.member_role}`"
                :name="`${member.member_id}-${member.member_role}`"
            >
                <template #label>
                    发起方:
                    <MemberTabHead
                        :form="form"
                        :member="member"
                    />
                </template>

                <MemberSetting
                    v-if="form.isCreator"
                    :member="member"
                    :form="form"
                />

                <MemberTabAudit
                    :member="member"
                    :memberIndex="memberIndex"
                />

                <MemberDataSet
                    :form="form"
                    :member="member"
                    :memberIndex="memberIndex"
                    :promoter="promoter"
                    role="promoter"
                />
            </el-tab-pane>

            <el-tab-pane
                v-for="(member, memberIndex) in form.memberList"
                :key="`${member.member_id}-${member.member_role}`"
                :name="`${member.member_id}-${member.member_role}`"
            >
                <template #label>
                    协作方:
                    <MemberTabHead
                        :form="form"
                        :member="member"
                    />
                </template>

                <MemberSetting
                    v-if="form.isCreator"
                    :member="member"
                    :form="form"
                />

                <MemberTabAudit
                    :member="member"
                    :memberIndex="memberIndex"
                />

                <MemberDataSet
                    :form="form"
                    :member="member"
                    :memberIndex="memberIndex"
                    :promoter="promoter"
                    role="memberList"
                />
            </el-tab-pane>
        </el-tabs>

        <div
            v-if="!form.closed && !form.exited && promoter.member_id === userInfo.member_id"
            class="mt20"
        >
            <el-button
                v-if="projectType === 'MachineLearning'"
                class="add-provider-btn mr20"
                @click="showSelectMemberDialog('promoter')"
            >
                + 添加更多发起方
            </el-button>

            <el-button
                type="primary"
                class="add-provider-btn"
                @click="showSelectMemberDialog('provider')"
            >
                + 添加更多协作方
            </el-button>
        </div>

        <el-dialog
            title="数据预览"
            v-model="dataSetPreviewDialog"
            destroy-on-close
            append-to-body
            width="60%"
        >
            <DataSetPreview v-if="form.project_type === 'MachineLearning'" ref="DataSetPreview" />
            <PreviewImageList v-if="form.project_type === 'DeepLearning'" ref="PreviewImageList" />
        </el-dialog>

        <SelectMemberDialog
            ref="SelectMemberDialog"
            :members="checkedMembersList"
            :current-delete-member="currentDeleteMember"
            @select-member="selectMember"
        />
        <SelectDatasetDialog
            ref="SelectDatasetDialog"
            :contains-y="''"
            :data-sets="dataSets.list"
            :data-add-btn="dataSets.role === 'promoter'"
            @selectDataSet="selectDataSet"
            @batchDataSet="batchDataSet"
        />
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import SelectMemberDialog from '@comp/views/select-member-dialog';
    import SelectDatasetDialog from '@comp/views/select-data-set-dialog';
    import DataSetPreview from '@comp/views/data_set-preview';
    import MemberServiceStatus from './member-service-status';
    import MemberSetting from './member-setting';
    import MemberTabHead from './member-tab-head';
    import MemberTabAudit from './member-tab-audit';
    import MemberDataSet from './member-data-set';
    import PreviewImageList from '@views/data-center/components/preview-image-list.vue';

    export default {
        name:       'MemberList',
        components: {
            DataSetPreview,
            SelectMemberDialog,
            SelectDatasetDialog,
            MemberServiceStatus,
            MemberSetting,
            MemberTabHead,
            MemberTabAudit,
            MemberDataSet,
            PreviewImageList,
        },
        inject: ['refresh'],
        props:  {
            form:        Object,
            promoter:    Object,
            projectType: String,
        },
        data() {
            return {
                dataSetPreviewDialog: false,
                dataSets:             {
                    role:  '',
                    id:    '',
                    index: 0,
                    list:  [],
                },
                currentDeleteMember: {},
                memberType:          '', // promoter | provider
                checkedMembersList:  [],
                memberTabName:       '',
                batchDataSetList:    [],
                sourceTypeMap:       {
                    TableDataSet: '结构化数据集',
                    ImageDataSet: '图像数据集',
                    BloomFilter:  '布隆过滤器',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
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

            showSelectMemberDialog(type) {
                const ref = this.$refs['SelectMemberDialog'];
                const memberlist = [];

                this.memberType = type;

                this.userInfo.$ispromoterself = true;
                memberlist.push(this.userInfo);
                this.checkedMembersList = type === 'promoter' ? this.form.promoterList.concat(memberlist) : type === 'provider' ? this.form.memberList : [];
                setTimeout(() => {
                    ref.show = true;
                });
            },

            selectMember(item) {
                this.$parent.loading = true;
                this.currentDeleteMember = {};
                const currentMembersList = this.memberType === 'promoter' ? this.form.promoterList : this.memberType === 'provider' ? this.form.memberList : [];
                const has = currentMembersList.find(row => row.id === item.id);

                if(!has) {
                    /* add dataset */
                    if(!item.$data_set) {
                        item.$data_set = [];
                    }
                    if(!item.$keywords) {
                        item.$keywords = [];
                    }
                    this.addMember([], item);
                }
            },

            async addMember(memberList, item) {
                const { code } = await this.$http.post({
                    url:  '/project/member/add',
                    data: {
                        project_id: this.form.project_id,
                        memberList: memberList.length ? memberList : [
                            {
                                member_id:   item.member_id || item.id,
                                member_name: item.member_name || item.name,
                                member_role: this.memberType,
                            },
                        ],
                    },
                });

                if(code === 0) {
                    this.refresh();
                    this.$message.success('成员添加成功!');
                }
                this.$parent.loading = false;
            },

            /*
            状态: 同意/拒绝/审核中
            自审时:
                1. 未退出 && 项目为已通过
                2. 审核状态为非拒绝
                3. 自己审核为非审核中
            他审时:
                被他人审核非同意时, 要显示具体审核信息, 包括审核人 审核结果 审核意见等
                邀请人不用重新审核
            */
            memberAuditStatus({
                exited,
                audit_status_from_myself,
                audit_status_from_others,
            }) {
                let flag = false;
                const audit = {
                    // audit myself
                    auditFromMySelf() {
                        if(audit_status_from_myself === 'auditing' && audit_status_from_others !== 'agree') {
                            return true;
                        }
                    },
                    // audit other members
                    auditForOthers() {
                        if(audit_status_from_myself === 'agree' && audit_status_from_others === 'auditing') {
                            return true;
                        }
                    },
                };

                if(!exited && this.form.audit_status === 'agree') {
                    // audit myself or other members
                    if(audit.auditFromMySelf() || audit.auditForOthers()) {
                        flag = true;
                    }
                }
                return flag;
            },

            addDataSet(role, memberIndex, memberId, $data_set) {
                const ref = this.$refs['SelectDatasetDialog'];

                ref.show = true;
                this.dataSets.role = role;
                this.dataSets.index = memberIndex;
                this.dataSets.oldListLength = $data_set.length;
                this.dataSets.list = $data_set.map(row => {
                    return {
                        ...row,
                        data_resource_id: row.id,
                    };
                });
                ref.loadDataList({ memberId, jobRole: role, resetPagination: false, $data_set, projectType: this.projectType });
            },

            async batchDataSet(batchlist) {
                const { role, index } = this.dataSets;
                const row = role === 'promoter_creator' ? this.promoter : role === 'promoter' ? this.form.promoterList[index] : this.form.memberList[index];

                if (batchlist.length) {
                    batchlist.forEach(item => {
                        this.batchDataSetList.push({
                            member_role:        row.member_role,
                            member_id:          row.member_id,
                            data_set_id:        item.data_resource_id,
                            data_resource_type: item.data_resource_type,
                        });
                    });
                    const { code } = await this.$http.post({
                        url:  '/project/data_resource/add',
                        data: {
                            project_id:       this.form.project_id,
                            dataResourceList: this.batchDataSetList,
                        },
                    });

                    if(code === 0) {
                        this.refresh();
                        this.$message.success('数据资源添加成功!');
                    }
                }
            },

            async selectDataSet(item) {
                const { role, index } = this.dataSets;
                const row = role === 'promoter_creator' ? this.promoter : role === 'promoter' ? this.form.promoterList[index] : this.form.memberList[index];
                const list = row.$data_set;
                const has = list.find(row => row.data_resource_id === item.data_resource_id || row.data_resource_id === item.id);

                if(!has) {
                    const { code } = await this.$http.post({
                        url:  '/project/data_resource/add',
                        data: {
                            project_id:  this.form.project_id,
                            dataSetList: [
                                {
                                    member_role:        row.member_role,
                                    member_id:          row.member_id,
                                    data_resource_id:   item.data_resource_id,
                                    data_resource_type: item.data_resource_type,
                                },
                            ],
                        },
                    });

                    if(code === 0) {
                        this.refresh();
                        this.$message.success('数据资源添加成功!');
                    }
                } else {
                    this.loading = false;
                }
            },

            removeDataSet(row, idx) {
                this.$confirm('删除后将不再使用当前数据样本', '警告', {
                    type: 'warning',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await this.$http.post({
                                url:  '/project/data_resource/remove',
                                data: {
                                    project_id:  this.form.project_id,
                                    data_set_id: row.data_set_id,
                                    member_role: row.member_role,
                                },
                            });

                            if(code === 0) {
                                this.$emit('deleteDataSetEmit', this.promoter.$data_set, idx);
                                this.$message.success('操作成功!');
                            }
                        }
                    });
            },

        },
    };
</script>

<style lang="scss">
.project-setting-icon{
    position: absolute;
    right: 0px;
    top: 0px;
    .el-icon-setting{
        cursor: pointer;
        color:#5088fc;
    }
}
</style>

<style lang="scss" scoped>
    .el-tabs{
        :deep(.el-tab-pane){position: relative;}
    }
    .service-offline{
        color: $--color-danger;
        cursor: pointer;
    }
    .member-exited{text-decoration: line-through;}
    .audit_comment{color: $--color-danger;}
    .dataset-preview{
        width: 34px;
        height: 34px;
        padding: 10px;
        vertical-align:top;
    }
    .add-provider-btn{
        width: 40%;
        font-size:14px;
        max-width: 300px;
        &.mr20{
            color:#fff;
            border: 1px solid #409EFF;
            background: #409EFF;
            &:hover{opacity:0.9;}
        }
    }
</style>
