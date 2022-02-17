<template>
    <VueDragResize
        position=""
        ref="component-panel-box"
        :control-points="controlPoints"
        :is-resizeable="true"
        :window-btns="false"
    >
        <div
            ref="component-panel"
            class="component-form"
        >
            <div class="ctrl-btns">
                <i
                    v-if="maxSize"
                    title="原始大小"
                    class="iconfont icon-recover"
                    @click="changeSize"
                />
                <i
                    v-else
                    title="网页全屏"
                    class="iconfont icon-enlarge"
                    @click="changeSize"
                />
            </div>
            <template v-if="showComponentPanel">
                <el-scrollbar
                    v-if="componentType === 'defaultPanel'"
                    height="100%"
                >
                    <el-form
                        :class="['p20', { 'readonly-form': myRole !== 'promoter' || !isCreator }]"
                        :disabled="myRole !== 'promoter' || !isCreator"
                        @submit.prevent
                    >
                        <div v-if="!isCreator" class="mb10">
                            <el-alert
                                title="!!! 仅允许查看"
                                style="width:250px;"
                                :closable="false"
                                type="warning"
                                effect="dark"
                                show-icon
                            />
                        </div>
                        <el-form-item
                            label="流程名称："
                            required
                        >
                            <el-input
                                v-model="flow_name"
                                @blur="saveFlowInfo($event)"
                            />
                        </el-form-item>
                        <el-form-item label="流程描述：">
                            <el-input
                                v-model="flow_desc"
                                type="textarea"
                                rows="6"
                                @blur="saveFlowInfo($event)"
                            />
                        </el-form-item>
                        <el-form-item label="训练类型：">
                            <template v-if="learningType === 'vertical'">
                                纵向
                            </template>
                            <template v-if="learningType === 'horizontal'">
                                横向
                            </template>
                            <template v-if="learningType === 'mix'">
                                混合
                            </template>
                        </el-form-item>
                        <p style="color:#fff;">{{ jobId }}</p>
                    </el-form>
                </el-scrollbar>

                <template v-for="(component, key) in componentsList">
                    <div
                        v-if="componentType.split('-')[0] === key"
                        :class="['component-panel-content', key]"
                        :key="key"
                    >
                        <el-tabs
                            v-model="tabName"
                            type="border-card"
                            tab-position="left"
                            @tab-click="tabChange"
                        >
                            <el-tab-pane
                                v-if="component.params"
                                label="参数"
                                name="params"
                            >
                                <el-scrollbar height="100%">
                                    <div v-if="!isCreator" class="mb10">
                                        <el-alert
                                            title="!!! 仅允许查看"
                                            style="width:250px;"
                                            :closable="false"
                                            type="warning"
                                            effect="dark"
                                            show-icon
                                        />
                                    </div>
                                    <el-popover
                                        title="参数说明"
                                        :offset="15"
                                        :width="250"
                                        placement="left-start"
                                        popper-class="explain-popper"
                                        v-model:visible="isExplainShow"
                                        trigger="manual"
                                        effect="dark"
                                    >
                                        <components-params-explain
                                            :is="`${key}`"
                                            :components-name="`${key}`"
                                            @closeExplainPop="closeExplainPop"
                                        />

                                        <template #reference>
                                            <component
                                                :is="`${key}-params`"
                                                :ref="`${key}-params`"
                                                :is-creator="isCreator"
                                                :disabled="jobGraphShow || myRole !== 'promoter' || !isCreator"
                                                :class="{ 'readonly-form': myRole !== 'promoter' || !isCreator }"
                                                :current-obj="currentObj"
                                                :project-id="projectId"
                                                :flow-id="flowId"
                                                :job-id="jobId"
                                                :learning-type="learningType"
                                                :ootModelFlowNodeId="ootModelFlowNodeId"
                                                :ootJobId="ootJobId"
                                                :project-type="projectType"
                                            >
                                            </component>
                                        </template>
                                    </el-popover>

                                    <div
                                        v-if="!jobGraphShow && isCreator"
                                        class="mt20"
                                    >
                                        <el-button
                                            v-if="tabName === 'params'"
                                            type="primary"
                                            @click="saveComponentData"
                                        >
                                            保存
                                        </el-button>
                                        <el-button @click="resetGraphState">取消</el-button>
                                    </div>
                                </el-scrollbar>
                            </el-tab-pane>
                            <el-tab-pane
                                v-if="component.result && jobGraphShow"
                                label="执行结果"
                                name="result"
                            >
                                <el-scrollbar height="100%">
                                    <component
                                        :is="`${key}-result`"
                                        :ref="`${key}-result`"
                                        :flow-node-id="currentObj.nodeId"
                                        :current-obj="currentObj"
                                        :job-detail="jobDetail"
                                        :is-creator="isCreator"
                                        :my-role="myRole"
                                        :flow-id="flowId"
                                        :job-id="jobId"
                                        :project-type="projectType"
                                    />
                                </el-scrollbar>
                            </el-tab-pane>
                            <el-tab-pane
                                label="帮助文档"
                                name="help"
                            >
                                <el-scrollbar height="100%">
                                    <component
                                        :is="`${key}-help`"
                                        :ref="`${key}-help`"
                                    />
                                </el-scrollbar>
                            </el-tab-pane>
                        </el-tabs>
                    </div>
                </template>
            </template>
        </div>
    </VueDragResize>
</template>

<script>
    import {
        componentsList,
        helpComponents,
        paramComponents,
        resultComponents,
    } from '../component-list/component-map';
    import ComponentsParamsExplain from './components-params-explain.vue';

    export default {
        components: {
            ...helpComponents,
            ...paramComponents,
            ...resultComponents,
            ComponentsParamsExplain,
        },
        props: {
            pageRef:            Object,
            jobDetail:          Object,
            currentObj:         Object,
            componentType:      String,
            jobGraphShow:       Boolean,
            myRole:             String,
            isCreator:          Boolean,
            projectId:          String,
            projectType:        String,
            flowId:             String,
            jobId:              String,
            oldLearningType:    String,
            ootModelFlowNodeId: String,
            ootJobId:           String,
        },
        emits: ['component-panel-change-size', 'getComponents', 'resetGraphState', 'update-currentObj', 'changeHeaderTitle', 'updateFlowInfo', 'remove-params-node', 'update-empty-params-node'],
        data() {
            return {
                componentsList, // all components key
                nodeModel:          {},
                locker:             false,
                tabName:            'params',
                flow_name:          '新流程',
                flow_desc:          '',
                learningType:       'vertical',
                showComponentPanel: true,
                controlPoints:      [
                    {
                        'ctrl-left': {
                            action:    'resize',
                            direction: 'horzantical',
                            icon:      'icon-vertical',
                        },
                    },
                ],
                isExplainShow: false,
                maxSize:       false,
            };
        },
        methods: {
            closeExplainPop() {
                this.isExplainShow = false;
            },

            changeSize() {
                if(this.maxSize) {
                    this.$refs['component-panel-box'].vData.rect.width = 350;
                } else {
                    this.$refs['component-panel-box'].vData.rect.width = this.pageRef.offsetWidth;
                }
                this.maxSize = !this.maxSize;
                this.$emit('component-panel-change-size', this.maxSize);
            },

            tabChange({ paneName }) {
                const child = this.$refs[`${this.componentType.split('-')[0]}-${paneName}`];

                if(paneName === 'result') {
                    child.methods.readData(this.nodeModel);
                } else if(paneName === 'params' && (child.vData.inited === false || this.jobGraphShow)) {
                    // never inited
                    child.methods.readData && child.methods.readData(this.nodeModel);
                }
            },

            resetGraphState() {
                this.$emit('resetGraphState');
            },

            /*
             * when currentObj.componentType === this.componentType, show the component
             */
            switchComponent(model, type) {
                const { id, data } = model;

                // cache node state
                this.nodeModel = model;
                if(data.componentType) {
                    // cache last node id
                    const lastNodeId = this.currentObj.nodeId;
                    const cfg = this.componentsList[data.componentType];

                    if(lastNodeId === id) return;

                    if(type) {
                        this.tabName = type;
                        if(type === 'result' && !cfg.result) {
                            this.tabName = 'params';
                        }
                    } else if (cfg.params) {
                        this.tabName = 'params';
                    } else if(this.jobGraphShow) {
                        this.tabName = 'result';
                    } else {
                        this.tabName = 'help';
                    }

                    this.showComponentPanel = false;
                    // must refresh component panel
                    this.$nextTick(_ => {
                        this.showComponentPanel = true;

                        const componentType = `${data.componentType}-${this.tabName}`;

                        this.$emit('update-currentObj', {
                            componentType,
                            nodeId: id,
                        });

                        this.$nextTick(_ => {
                            // switched
                            if(lastNodeId !== id) {
                                // call readData
                                let ref = this.$refs[this.componentType];

                                ref = Array.isArray(ref) ? ref[0]: ref;

                                if(ref) {
                                    let readData;

                                    if(ref.readData) {
                                        readData = ref.readData;
                                    } else if(ref.methods && ref.methods.readData) {
                                        readData = ref.methods.readData;
                                    }

                                    if(readData) {
                                        readData(model);
                                    }
                                }
                            }
                        });
                    });
                }
            },

            async saveFlowInfo($event) {
                if(this.locker) return;
                this.locker = true;

                if(this.flow_name.trim() === '') {
                    this.flow_name = '新流程';
                    this.$emit('changeHeaderTitle');
                    return this.$message.error('流程名称不能为空!');
                }
                const { projectId, flowId } = this;
                const params = {
                    name:                  this.flow_name,
                    desc:                  this.flow_desc,
                    FederatedLearningType: this.learningType,
                    projectId,
                    flowId,
                };

                const { code } = await this.$http.post({
                    url:  '/project/flow/update/base_info',
                    data: params,
                });

                this.locker = false;
                if(code === 0) {
                    this.$emit('updateFlowInfo', {
                        flow_name:    this.flow_name,
                        flow_desc:    this.flow_desc,
                        learningType: this.learningType,
                    });
                    this.$emit('changeHeaderTitle');
                    this.$notify.success({
                        offset:   5,
                        duration: 1500,
                        title:    '提示',
                        message:  '保存成功!',
                    });

                    if(!flowId) {
                        this.$router.replace({
                            query: {
                                project_id: projectId,
                                flow_id:    flowId,
                            },
                        });
                    }
                } else if (code === 10012) {
                    this.learningType = this.oldLearningType;
                }
            },

            // save component form
            saveComponentData($event) {
                if(this.currentObj.nodeId) {
                    const ref = this.$refs[this.componentType];
                    const refInstance = Array.isArray(ref) ? ref[0]: ref;

                    if(refInstance) {
                        const formData = refInstance.methods.checkParams();

                        if(formData) {
                            this.submitFormData($event, formData.params);
                        }
                    }
                }
            },

            async submitFormData($event, params) {
                const btnState = {};

                if($event !== 'node-update') {
                    btnState.target = $event;
                }
                const { projectId, flowId } = this;
                const { code, data } = await this.$http.post({
                    url:  '/project/flow/node/update',
                    data: {
                        nodeId:        this.currentObj.nodeId,
                        componentType: this.currentObj.componentType.replace('-params', ''),
                        flowId,
                        params,
                    },
                    btnState,
                });

                if(code === 0) {
                    // remove from left list
                    this.$emit('remove-params-node', this.currentObj.nodeId);

                    if(data.params_is_null_flow_nodes) {
                        this.$emit('update-empty-params-node', data.params_is_null_flow_nodes);
                    }

                    if(!flowId) {
                        this.$router.replace({
                            query: {
                                project_id: projectId,
                                flow_id:    flowId,
                            },
                        });
                    }
                    if($event !== 'node-update') {
                        this.$notify.success({
                            offset:   5,
                            duration: 1000,
                            title:    '提示',
                            message:  '保存成功!',
                        });
                    }
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
.drag-resize{
    min-width: 350px;
    background: #fff;
    z-index: 10;
    :deep(.drag-content){overflow: visible;}
    :deep(.ctrl-left){
        left: -24px;
        padding:12px 3px;
        background: #fff;
        border-radius:4px 0 0 4px;
        border: 1px solid #e5e9f2;
    }
    :deep(.control-points.covered){
        .ctrl-left{background: #f5f5f5;}
    }
}
.ctrl-btns{
    position: absolute;
    top: 10px;
    right:15px;
    z-index: 200;
    .iconfont{
        cursor: pointer;
        font-size: 20px;
        color: $color-light;
        background: #fff;
        border-radius: 4px;
        padding:5px;
        &:hover{color:$--color-primary;}
    }
}
.component-form{
    min-width: 350px;
    box-shadow: none;
    height:100%;
    :deep(.el-form-item__label){
        color:#909399;
        margin-bottom: 6px;
        font-size: 13px;
    }
    :deep(.el-input__inner),
    :deep(.el-textarea__inner){max-width: 300px;}
}
#pane-help, .component-panel-content{height: 100%;}
#pane-params, #pane-result{
    height: 100%;
    :deep(.el-scrollbar__view){
        min-height: 100%;
        position: relative;
        & > .el-loading-parent--relative{
            position: static !important;
        }
    }
    :deep(.el-form-item__label),
    :deep(.el-form-item__content){line-height: 20px;}
}
.el-tabs--border-card{
    box-shadow: none;
    position: relative;
    overflow: visible;
    height:100%;
    border:0;
    & > :deep(.el-tabs__header) {
        margin:0;
        border-left: 1px solid #dfe4ed;
        position: absolute;
        left: -51px;
        top: 0;
        z-index: 1;
        height:auto;
        .el-tabs__nav{transform: translateY(0) !important;}
        .el-tabs__nav-prev,
        .el-tabs__nav-next{display:none;}
        .el-tabs__nav-scroll,
        .el-tabs__nav-wrap{
            height:auto;
            padding:0;
        }
        .el-tabs__item{
            width: 50px;
            height: auto;
            padding: 10px 7px;
            text-align: center;
            word-break: break-all;
            white-space: normal;
            line-height: 20px;
            margin-left: 1px !important;
        }
    }
    & > :deep(.el-tabs__content) {
        position: absolute;
        top:0;
        left:0;
        right:0;
        height:100%;
        overflow-y: hidden;
    }
}
.readonly-form{
    position: relative;
    &:before{
        content: '';
        position: absolute;
        z-index: 10000;
        width: 100%;
        height:100%;
    }
}
.el-tabs{
    :deep(.el-tabs__item){
        font-size: 13px;
        color:#909399;
    }
}
</style>
