<template>
    <div v-loading="vData.loading" :disabled="disabled">
        <selectFeature title="发起方" :featureData="vData.promoter" :disabled="disabled" @selectFeature="methods.promoteFeature"></selectFeature>
        <selectFeature title="协作方" :featureData="vData.provider" :disabled="disabled" @selectFeature="methods.provideFeature"></selectFeature>
        <psi-bin 
            v-model:binValue="vData.binValue" 
            title="分箱方式"
            :disabled="disabled"
            :filterMethod="['custom']" />
    </div>
</template>

<script>
    import {
        ref,
        reactive,
    } from 'vue';
    import selectFeature from './components/selectFeature.vue';
    import psiBin from '../../components/psi/psi-bin.vue';
    import dataStore from '../data-store-mixin';
    import { getNodeDetail,getFlowNodeFeature } from '@src/service';

    export default {
        name:       'VertFeaturePSI',
        components: {
            selectFeature,
            psiBin,
        },
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        setup(props, context) {
            const CheckFeatureDialogRef = ref();

            let vData = reactive({
                binValue: {
                    method:    'bucket',
                    binNumber: 6,
                },
                loading:  false,
                promoter: {
                    name:            '',
                    featureNames:    [],
                    member_id:       '',
                    member_role:     '',
                    features:        [],
                    selectedFeature: [],
                },
                provider: {
                    featureNames:    [],
                    name:            '',
                    member_id:       '',
                    member_role:     '',
                    features:        [],
                    selectedFeature: [],
                },
            });

            let methods = {
                checkParams: () => {
                    const { provider, promoter,binValue } = vData;
                    const { method, binNumber } = binValue;
                    const $params = {
                        members: [{
                                      'memberId':   provider.member_id,
                                      'memberRole': provider.member_role,
                                      'features':   provider.selectedFeature,
                                  },
                                  {
                                      'memberId':   promoter.member_id,
                                      'memberRole': promoter.member_role,
                                      'features':   promoter.selectedFeature,
                                  },
                        ],
                        method,
                        count: binNumber,
                    };

                    return {
                        params: $params,
                    };
                },
                promoteFeature: (selectedFeature) => {
                    vData.promoter.selectedFeature = selectedFeature;

                },
                provideFeature: (selectedFeature) => {
                    vData.provider.selectedFeature = selectedFeature;
                },
                getNodeDetail: (model) => {
                    getNodeDetail({
                        flowId: props.flowId,
                        id:     model.id,
                    }).then((params)=>{
                        const { method ='bucket', count = 6, members } = params || {};

                        vData.binValue = {
                            method, binNumber: count,
                        };
                        if(Array.isArray(members)){
                            members.forEach(item => {
                                const { memberRole, features } = item;
                                
                                vData[memberRole].selectedFeature = features;
                            });
                        }
                        
                    });

                    methods.getFlowNodeFeature(model);
                },
                /** 获取特征 */
                getFlowNodeFeature: (model) => {
                    getFlowNodeFeature({
                        jobId: props.jobId,flowId: props.flowId,flowNodeId: model.id,
                    }).then(res => {
                        const { members = [] } = res || {};

                        members.forEach(item => {
                            const { features = [] } = item;

                            /** 保存一份name数组 */
                            item.featureNames = features.map(feature => feature.name);
                        });
                        vData.promoter = {
                            ... vData.promoter,
                            ...members.filter(item => item.member_role === 'promoter')[0],
                        };
                        vData.provider = {
                            ... vData.provider,
                            ... members.filter(item => item.member_role === 'provider')[0],
                        };

                    });
                },
            };

            // onMounted(()=>{
            //     methods.getNodeDetail();
            // });
            const { $data, $methods } = dataStore.mixin({
                props,
                vData,
                methods,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
                CheckFeatureDialogRef,
            };
        },
        
    };
</script>

<style lang="scss" scoped>
    .el-input-number{
        width: 104px;
        margin-right:10px;
        :deep(.el-input__inner){
            padding-left:5px;
            padding-right: 40px;
        }
    }
</style>
