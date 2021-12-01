<template>
    <!--
        two sides, left: audit, right: dataset
        左侧(未退出):
            1. 自己审核
            2. 审核他人

        A 发起方 邀请 C
            ① C 已拒绝
                ①-1 C 主动拒绝, 只显示 C 的审核意见
                ①-2 其他人拒绝, 显示其他人的审核意见
            ② C 待审核
                ②-1 C 自己待审核
                ②-2 C 自己已同意, 需要其他人审核
            ③ C 已同意
                C 自己已同意, 其他成员也已同意, 显示所有人的审核意见

        B 审核角色
            ① C 已拒绝
                ①-1 C 主动拒绝, 只显示 C 的意见
                ①-2 其他人拒绝, 显示其他人的意见
            ② C 待审核
                ②-1 B 需要审核, 显示所有人审核意见
                ②-2 C 自己已同意, 需要其他人审核, 显示所有人的审核意见
            ③ C 已同意
                C 自己已同意, 其他成员也已同意, 显示所有人的审核意见

        C 被邀请角色
            ① 已拒绝
                ①-1 C 主动拒绝, 只显示 C 的意见
                ①-2 其他人拒绝, 显示其他人的意见
            ② C 待审核
                ②-1 C 自己待审核
                ②-2 C 自己已同意, 需要其他人审核
            ③ C 已同意
                C 自己已同意, 其他成员也已同意, 显示所有人的审核意见
    -->
    <el-row :gutter="member.audit_status !== 'agree' ? 20 : 0" style="flex-direction: column;">
        <el-col
            v-if="!member.exited"
            :span="8"
        >
            <template v-if="member.audit_status === 'disagree'">
                <!-- Refuse to join the project -->
                <p
                    v-if="member.audit_status_from_myself === 'disagree'"
                    class="f14 mb20 mt20"
                >
                    该成员已拒绝加入此项目: <span class="audit_comment">{{ member.audit_comment }}</span>
                </p>
                <!-- audit from others -->
                <div
                    v-else-if="member.audit_status_from_others && member.$other_audit.length"
                    class="f14 mb20 mt20"
                >
                    <p
                        v-for="item in member.$other_audit"
                        :key="item.id"
                        class="f12 mb10"
                    >
                        {{ item.auditor_name }}
                        <template v-if="item.audit_result !== 'auditing'">的审核意见为:
                            <span
                                v-if="item.audit_comment"
                                class="audit_comment"
                            >{{ item.audit_comment }}</span>
                            <span
                                v-else-if="item.audit_result === 'agree'"
                                class="audit_comment"
                            >同意</span>
                            <span
                                v-else-if="item.audit_result === 'disagree'"
                                class="audit_comment"
                            >拒绝</span>
                        </template>
                        <template v-else>的审核状态为: <strong class="audit_comment">[待审核]</strong>
                        </template>
                    </p>
                </div>
            </template>

            <template v-else-if="member.audit_status === 'auditing'">
                <!--
                    audit others (i'm auditing & others are agree/auding & member.$other_audit.length > 0)
                    ||
                    need others' auditing (i'm agree & others are auditing)
                 -->
                <template v-if="(member.audit_status_from_myself === 'auditing' && member.audit_status_from_others !== 'disagree' && member.$other_audit.length) || (member.audit_status_from_myself === 'agree' && member.audit_status_from_others === 'auditing')">
                    <template
                        v-for="item in member.$other_audit"
                        :key="item.id"
                    >
                        <!-- Audit status -->
                        <p class="f12 mb10">
                            <template v-if="item.audit_result !== 'auditing'">
                                {{ item.auditor_name }} 的审核意见为:
                                <span
                                    v-if="item.audit_comment"
                                    class="audit_comment"
                                >{{ item.audit_comment }}</span>
                                <span
                                    v-else-if="item.audit_result === 'agree'"
                                    class="audit_comment"
                                >同意</span>
                                <span
                                    v-else-if="item.audit_result === 'disagree'"
                                    class="audit_comment"
                                >拒绝</span>
                            </template>
                            <template v-else-if="item.auditor_id !== userInfo.member_id">
                                {{ item.auditor_name }} 的审核状态为: <strong class="audit_comment">[待审核]</strong>
                            </template>
                            <template v-else-if="item.auditor_id === userInfo.member_id">
                                <!-- audit others -->
                                <p class="f14 mb10 color-danger">
                                    参与合作审核意见:
                                </p>
                                <el-input
                                    v-model="vData.memberAuditComments[memberIndex]"
                                    type="textarea"
                                    :rows="3"
                                    @blur="methods.updateMemberAuditComment($event, memberIndex)"
                                />
                                <div class="mt20 mb10">
                                    <el-button
                                        type="primary"
                                        @click="methods.cooperAuth(true, 'others', member)"
                                    >
                                        同意
                                    </el-button>
                                    <el-button
                                        type="danger"
                                        @click="methods.cooperAuth(false, 'others', member)"
                                    >
                                        拒绝
                                    </el-button>
                                </div>
                            </template>
                        </p>
                    </template>
                </template>
            </template>
            <!-- Others have agreed, Don't show audit comments again -->
            <!-- <template v-else-if="member.audit_status === 'agree' && member.audit_status_from_others === 'agree'">
            <p
                v-for="item in member.$other_audit"
                :key="item.id"
                class="f12 mb10"
            >
                {{ item.auditor_name }}
                <template v-if="item.audit_result !== 'auditing'">
                    的审核意见为:
                    <span
                        v-if="item.audit_comment"
                        class="audit_comment"
                    >{{ item.audit_comment }}</span>
                    <span
                        v-else-if="item.audit_result === 'agree'"
                        class="audit_comment"
                    >同意</span>
                    <span
                        v-else-if="item.audit_result === 'disagree'"
                        class="audit_comment"
                    >拒绝</span>
                </template>
                <template v-else>
                    的审核状态为: <strong class="audit_comment">[待审核]</strong>
                </template>
            </p>
        </template> -->
        </el-col>
        <!--
            1. not exited &
            2. i'm not auding ||
            3. others are auding
         -->
        <el-col :span="24">
            <!-- :span="!member.exited && member.audit_status_from_others === 'auditing' ? 16 : 24" -->
            <!--
                1. not exited
                2. member is myself
                3. auding is agree
             -->
            <el-button
                v-if="!form.closed && !member.exited && ((member.member_id === userInfo.member_id && member.audit_status === 'agree') || (form.isPromoter && member.audit_status === 'agree'))"
                plain
                type="primary"
                @click="methods.addDataSet(role, memberIndex, member.member_id, member.$data_set)"
            >
                + 添加数据集到此项目
            </el-button>
            <el-table
                v-if="member.$data_set.length"
                :data="member.$data_set"
                max-height="520px"
                class="mt10"
                border
                stripe
            >
                <el-table-column label="序号" type="index" />
                <el-table-column
                    label="数据集"
                    width="260"
                >
                    <template v-slot="scope">
                        <span v-if="scope.row.audit_status === 'auditing'" class="color-danger mr10">(待审核)</span>
                        <router-link :to="{ name: scope.row.member_id === userInfo.member_id ? 'data-view' : 'union-data-view', query: { id: scope.row.data_set_id } }">
                            {{ scope.row.name }}
                        </router-link>
                        <p class="p-id pt5">{{ scope.row.data_set_id }}</p>
                    </template>
                </el-table-column>

                <el-table-column label="关键词">
                    <template v-slot="scope">
                        <template v-if="scope.row.tags">
                            <template v-for="(item, index) in scope.row.tags.split(',')">
                                <el-tag
                                    v-if="item"
                                    :key="index"
                                    class="mr10"
                                >
                                    {{ item }}
                                </el-tag>
                            </template>
                        </template>
                    </template>
                </el-table-column>

                <el-table-column label="数据量">
                    <template v-slot="scope">
                        特征：{{ scope.row.feature_count }}
                        <br>
                        行数：{{ scope.row.row_count }}
                    </template>
                </el-table-column>

                <el-table-column
                    label="使用次数"
                    width="80"
                >
                    <template v-slot="scope">
                        {{ scope.row.usage_count_in_job }}
                    </template>
                </el-table-column>

                <el-table-column label="是否包含 Y">
                    <template v-slot="scope">
                        {{ scope.row.contains_y ? '是' : '否' }}
                    </template>
                </el-table-column>

                <el-table-column
                    label="状态"
                    width="100"
                >
                    <template v-slot="scope">
                        <span v-if="scope.row.audit_status === 'agree'">已授权</span>
                        <span v-else-if="scope.row.audit_status === 'auditing'">等待审核</span>
                        <template v-else-if="scope.row.audit_status === 'disagree'">
                            <span>被拒绝</span>
                            <el-tooltip
                                class="item"
                                effect="dark"
                                content="被拒绝的数据集需要移除后再进行添加！"
                                placement="top"
                            >
                                <i class="el-icon-info"></i>
                            </el-tooltip>
                        </template>
                    </template>
                </el-table-column>
                <!--
                    1. not exited
                    2. member is myself
                    3. auditing is agree || waiting
                 -->
                <el-table-column
                    v-if="!form.closed && !member.exited && (member.audit_status !== 'disagree' && member.member_id === userInfo.member_id || (form.isPromoter && member.audit_status !== 'disagree'))"
                    min-width="220"
                    label="操作"
                >
                    <template v-slot="scope">
                        <!-- The current member is a provider -->
                        <template v-if="scope.row.member_id === userInfo.member_id">
                            <span
                                v-if="member.audit_status_from_myself === 'auditing'"
                                class="mr10"
                            >请先同意授权加入合作或</span>
                            <!-- Dataset pending approval -->
                            <template v-else-if="scope.row.audit_status === 'auditing'">
                                <el-button
                                    type="primary"
                                    @click="methods.dataAuth({ member, memberIndex, $index: scope.$index, flag: true, })"
                                >
                                    同意
                                </el-button>
                                <el-button @click="methods.dataAuth({ member, memberIndex, $index: scope.$index, flag: false, })">
                                    拒绝
                                </el-button>
                            </template>
                        </template>
                        <el-tooltip
                            v-if="scope.row.member_id === userInfo.member_id && scope.row.audit_status !== 'auditing'"
                            content="预览数据"
                            placement="top"
                        >
                            <el-button
                                circle
                                type="info"
                                class="dataset-preview"
                                @click="methods.showDataSetPreview(scope.row)"
                            >
                                <i class="el-icon-view" />
                            </el-button>
                        </el-tooltip>
                        <!--
                            1. dataset is not deleted
                            2. member is promoter || member is myself
                         -->
                        <el-button
                            v-if="form.isPromoter || scope.row.member_id === userInfo.member_id"
                            circle
                            type="danger"
                            class="mr10"
                            icon="el-icon-delete"
                            @click="methods.removeDataSet(scope.row, scope.$index)"
                        />
                        <template v-if="scope.row.deleted">
                            该数据集已被移除
                        </template>
                    </template>
                </el-table-column>
            </el-table>
        </el-col>
    </el-row>

    <el-dialog
        title="数据预览"
        v-model="vData.dataSetPreviewDialog"
        destroy-on-close
        append-to-body
    >
        <DataSetPreview ref="DataSetPreviewRef" />
    </el-dialog>

    <el-dialog
        title="提示"
        width="400px"
        destroy-on-close
        v-model="vData.cooperAuthDialog.show"
    >
        <div class="el-message-box__container">
            <i class="el-message-box__status el-icon-warning" />
            <div class="el-message-box__message">{{ vData.cooperAuthDialog.flag ? '同意加入合作' : (form.isPromoter ? '退出此次项目合作' : '拒绝与发起方的此次项目合作') }}</div>
        </div>
        <p class="f14 mt20 mb10 color-danger">审核意见:</p>
        <el-input
            v-model="vData.cooperAuthDialog.audit_comment"
            type="textarea"
            :rows="3"
        />
        <div class="mt20 text-r">
            <el-button @click="vData.cooperAuthDialog.show=false">
                取消
            </el-button>
            <el-button
                v-loading="vData.locker"
                type="primary"
                @click="methods.cooperAuthConfirm"
            >
                确定
            </el-button>
        </div>
    </el-dialog>

    <SelectDatasetDialog
        ref="SelectDatasetDialogRef"
        :contains-y="''"
        :data-sets="vData.dataSets.list"
        :data-add-btn="vData.dataSets.role === 'promoter'"
        @selectDataSet="methods.selectDataSet"
        @batchDataSet="methods.batchDataSet"
    />
</template>

<script>
    import { useStore } from 'vuex';
    import {
        ref,
        reactive,
        computed,
        nextTick,
        inject,
        getCurrentInstance,
    } from 'vue';
    import SelectDatasetDialog from '@comp/views/select-data-set-dialog';
    import DataSetPreview from '@comp/views/data_set-preview';

    export default{
        props: {
            form:        Object,
            member:      Object,
            memberIndex: Number,
            promoter:    Object,
            role:        String,
        },
        components: {
            DataSetPreview,
            SelectDatasetDialog,
        },
        emits: ['deleteDataSetEmit'],
        setup(props, context) {
            const store = useStore();
            const refresh = inject('refresh');
            const { appContext } = getCurrentInstance();
            const { $bus, $http, $message, $confirm, $prompt } = appContext.config.globalProperties;
            const userInfo = computed(() => store.state.base.userInfo);
            const DataSetPreviewRef = ref();
            const SelectDatasetDialogRef = ref();
            const vData = reactive({
                locker:               false,
                dataSetPreviewDialog: false,
                cooperAuthDialog:     {
                    audit_comment: '',
                    show:          false,
                    flag:          false,
                    row:           {},
                },
                dataSets: {
                    role:  '',
                    id:    '',
                    index: 0,
                    list:  [],
                },
                memberAuditComments: [],
                batchDataSetList:    [],
            });
            const methods = {
                showDataSetPreview(item){
                    vData.dataSetPreviewDialog = true;

                    nextTick(() =>{
                        DataSetPreviewRef.value.loadData(item.data_set_id);
                    });
                },

                updateMemberAuditComment($event, index) {
                    const val = vData.memberAuditComments[index];

                    $bus.$emit('update-member-audit-comment', {
                        index,
                        role:    props.role,
                        comment: val,
                    });
                },

                addDataSet(role, memberIndex, memberId, $data_set) {
                    const ref = SelectDatasetDialogRef.value;

                    ref.show = true;
                    vData.dataSets.index = memberIndex;
                    vData.dataSets.role = role;
                    vData.dataSets.oldListLength = $data_set.length;
                    vData.dataSets.list = $data_set.map(row => {
                        return {
                            ...row,
                            data_set_id: row.id,
                        };
                    });
                    ref.loadDataList({ memberId, $data_set, projectType: props.form.project_type });
                },

                cooperAuth(flag, role = 'myself', { $audit_comment, member_id }) {
                    vData.cooperAuthDialog.role = role;
                    if(role === 'others') {
                        vData.cooperAuthDialog.audit_comment = $audit_comment;
                        vData.cooperAuthDialog.member_id = member_id;
                    }
                    // vData.cooperAuthDialog.show = true;
                    vData.cooperAuthDialog.flag = flag;
                    methods.cooperAuthConfirm();
                },

                async cooperAuthConfirm() {
                    if(vData.locker) return;
                    vData.locker = true;

                    const { flag, audit_comment, role, member_id } = vData.cooperAuthDialog;
                    const params = {
                        project_id:   props.form.project_id,
                        audit_result: flag ? 'agree' : 'disagree',
                        audit_comment,
                    };

                    if(role === 'others') {
                        params.member_id = member_id;
                    }
                    const { code } = await $http.post({
                        url: role === 'others' ? '/project/member/add/audit' : '/project/add/audit',
                        params,
                    });

                    nextTick(_ => {
                        vData.locker = false;
                        if (code === 0) {
                            refresh();
                            $message.success('操作成功!');
                        }
                    });
                },

                dataAuth({ member, $index, flag }) {
                    if(member.audit_status !== 'agree') {
                        if(member.audit_status_from_myself !== 'agree') {
                            return $message.error('请先同意授权加入合作!');
                        } else if (member.audit_status_from_others !== 'agree') {
                            return $message.error('请先等待他人同意授权加入合作!');
                        }
                    }
                    const result = flag ? $prompt('确定同意协作方使用数据集进行流程训练吗', '提示', {
                        type:        'warning',
                        customClass: 'audit_dialog',
                    }) : $prompt('拒绝协作方在此项目中使用此数据集:\n 原因:', '提示', {
                        inputValidator(value) {
                            return value != null && value !== '';
                        },
                        inputErrorMessage: '原因不能为空',
                        customClass:       'audit_dialog',
                    });

                    result.then(async ({ action, value }) => {
                        if(action === 'confirm') {
                            const { code } = await $http.post({
                                url:  '/project/data_set/audit',
                                data: {
                                    project_id:    props.form.project_id,
                                    audit_status:  flag ? 'agree' : 'disagree',
                                    data_set_id:   member.$data_set[$index].data_set_id,
                                    audit_comment: value,
                                },
                            });

                            if(code === 0) {
                                $bus.$emit('update-member-audit-status', {
                                    memberIndex: props.memberIndex,
                                    index:       $index,
                                    status:      flag ? 'agree' : 'disagree',
                                    role:        props.role,
                                });
                                $message.success('操作成功!');
                            }
                        }
                    });
                },

                async batchDataSet(batchlist) {
                    const { role, index } = vData.dataSets;
                    const row = role === 'promoter_creator' ? props.promoter : role === 'promoter' ? props.form.promoterList[index] : props.form.memberList[index];

                    if (batchlist.length) {
                        batchlist.forEach(item => {
                            vData.batchDataSetList.push({
                                member_role:   row.member_role,
                                member_id:     row.member_id,
                                data_set_id:   item.id,
                                data_set_type: props.form.project_type === 'DeepLearning' ? 'ImageDataSet' : props.form.project_type === 'MachineLearning' ? 'TableDataSet' : '',
                            });
                        });
                        const { code } = await $http.post({
                            url:  '/project/data_set/add',
                            data: {
                                project_id:  props.form.project_id,
                                dataSetList: vData.batchDataSetList,
                            },
                        });

                        if(code === 0) {
                            refresh();
                            $message.success('数据集添加成功!');
                        }
                    }
                },

                async selectDataSet(item) {
                    const { role, index } = vData.dataSets;
                    const row = role === 'promoter_creator' ? props.promoter : role === 'promoter' ? props.form.promoterList[index] : props.form.memberList[index];
                    const list = row.$data_set;
                    const has = list.find(row => row.data_set_id === item.data_set_id || row.data_set_id === item.id);

                    if(!has) {
                        const { code } = await $http.post({
                            url:  '/project/data_set/add',
                            data: {
                                project_id:  props.form.project_id,
                                dataSetList: [
                                    {
                                        member_role:   row.member_role,
                                        member_id:     row.member_id,
                                        data_set_id:   item.id,
                                        data_set_type: props.form.project_type === 'DeepLearning' ? 'ImageDataSet' : props.form.project_type === 'MachineLearning' ? 'TableDataSet' : '',
                                    },
                                ],
                            },
                        });

                        if(code === 0) {
                            refresh();
                            $message.success('数据集添加成功!');
                        }
                    } else {
                        vData.loading = false;
                    }
                },

                removeDataSet(row, idx) {
                    $confirm('删除后将不再使用当前数据样本', '警告', {
                        type: 'warning',
                    })
                        .then(async action => {
                            if(action === 'confirm') {
                                const { code } = await $http.post({
                                    url:  '/project/data_set/remove',
                                    data: {
                                        project_id:  props.form.project_id,
                                        data_set_id: row.data_set_id,
                                        member_role: row.member_role,
                                    },
                                });

                                if(code === 0) {
                                    // refresh();
                                    $bus.$emit('delete-data-set-emit', props.member.$data_set, idx);
                                    $message.success('操作成功!');
                                }
                            }
                        });
                },
            };

            return {
                vData,
                methods,
                userInfo,
                DataSetPreviewRef,
                SelectDatasetDialogRef,
            };
        },
    };
</script>
