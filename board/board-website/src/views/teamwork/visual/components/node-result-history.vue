<template>
    <el-dialog
        v-model="vData.show"
        :fullscreen="vData.fullscreen"
        :close-on-click-modal="false"
        custom-class="node-history-dialog"
        title="节点执行历史:"
        destroy-on-close
        append-to-body
    >
        <div
            v-loading="vData.loading"
            class="flexbox"
        >
            <el-icon
                title="切换全屏"
                class="el-icon-full-screen f16"
                @click="switchFullscreen"
            >
                <elicon-full-screen />
            </el-icon>

            <div class="node-history">
                <h4 class="text-c mb5">执行历史:</h4>
                <p class="f12 text-c">(勾选列表进行对比 {{ vData.detailList.length }}/2)</p>
                <ul class="node-history-ul">
                    <li
                        v-for="item in vData.list"
                        :class="['mb5 mt10', { 'disabled': !item.job_id || (vData.detailList.length === 2 && !item.checked)}]"
                        :key="item.job_id"
                    >
                        <el-checkbox
                            v-model="item.checked"
                            :disabled="!item.job_id || (vData.detailList.length === 2 && !item.checked)"
                            @change="historyChanged($event, item.job_id)"
                        >
                            启动时间: <span class="start-time">{{ dateFormat(item.start_time) }}</span>
                            <p class="job-id">{{ item.job_id }}</p>
                        </el-checkbox>
                    </li>
                </ul>
            </div>

            <div class="job-compare ml20">
                <h4 class="text-c">历史对比:</h4>
                <div class="jobs-compare mt10">
                    <EmptyData v-if="vData.detailList.length === 0" />
                    <div
                        v-else
                        v-for="row in vData.detailList"
                        :key="row.job_id"
                        class="flex-item"
                    >
                        <p class="f16 text-c">启动时间: {{ dateFormat(row.start_time) }}</p>
                        <p class="job-id text-c mb10">{{ row.job_id }}</p>

                        <template v-if="componentsList[row.component_type]">
                            <component
                                :key="row.job_id"
                                :autoReadResult="true"
                                :is="`${row.component_type}-result`"
                                :flow-node-id="row.flow_node_id"
                                :flow-id="row.flow_id"
                                :job-id="row.job_id"
                                :my-role="row.role"
                            />
                        </template>
                        <p v-else class="text-c color-danger pt40">
                            任务信息: {{ row.message }}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </el-dialog>
</template>

<script>
    import {
        nextTick,
        onBeforeMount,
        getCurrentInstance,
        reactive,
    } from 'vue';
    import {
        componentsList,
        resultComponents,
    } from '../component-list/component-map';

    export default {
        components: {
            ...componentsList,
            ...resultComponents,
        },
        props: {
            flowId:        String,
            currentObj:    Object,
            componentType: String,
            myRole:        String,
        },
        setup(props) {
            const { appContext } = getCurrentInstance();
            const { $bus, $http } = appContext.config.globalProperties;
            const vData = reactive({
                fullscreen: false,
                loading:    false,
                show:       false,
                detailList: [],
                list:       [],
            });
            const getResultHistory = async (nodeId) => {
                vData.show = true;
                vData.loading = true;
                const { code, data } = await $http.get({
                    url:    '/flow/job/task/result_history',
                    params: {
                        role:       props.myRole,
                        flowId:     props.flowId,
                        flowNodeId: nodeId || props.currentObj.nodeId,
                        type:       props.componentType,
                    },
                });

                nextTick(_ => {
                    vData.loading = false;
                    if(code === 0) {
                        vData.list = data.list.map(x => {
                            x.checked = false;
                            return x;
                        });
                    }
                });
            };
            const historyChanged = (val, job_id) => {
                const item = vData.list.find(x => x.job_id === job_id);

                if(val) {
                    vData.detailList.push({ ...item });
                } else {
                    const i = vData.detailList.findIndex(x => x.job_id === job_id);

                    vData.detailList.splice(i, 1);
                }
            };
            const switchFullscreen = () => {
                vData.fullscreen = !vData.fullscreen;
            };

            onBeforeMount(() => {
                // node job history
                $bus.$on('show-node-history', nodeId => {
                    vData.detailList = [];
                    vData.list = [];
                    getResultHistory(nodeId);
                });
            });

            return {
                vData,
                componentsList,
                switchFullscreen,
                getResultHistory,
                historyChanged,
            };
        },
    };
</script>

<style lang="scss">
    .node-history-dialog{min-width: 1100px;}
</style>

<style lang="scss" scoped>
    .el-icon-full-screen{
        position: absolute;
        top: 70px;
        right: 20px;
        cursor: pointer;
        &:hover{color:$--color-primary;}
    }
    .node-history{width:240px;}
    .node-history-ul{
        max-height: calc(100vh - 140px);
        overflow-y: auto;
        :deep(.el-checkbox__label) {vertical-align: top;}
    }
    .job-id{
        font-size: 12px;
        color: $color-text-placeholder;
    }
    .job-compare{
        width: calc(100% - 250px);
        :deep(.history-btn){display:none;}
    }
    .jobs-compare{
        display:flex;
        justify-content: center;
        max-height: calc(100vh - 140px);
        overflow-y: auto;
        .flex-item {
            flex: 1;
            width: 50%;
            padding: 0 10px;
        }
    }
</style>
