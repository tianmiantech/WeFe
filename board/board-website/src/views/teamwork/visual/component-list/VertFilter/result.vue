<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <CommonResult
                :result="vData.commonResultData"
                :currentObj="currentObj"
                :jobDetail="jobDetail"
            />
            <el-form>
                <el-form-item label="数据集名称: ">
                    {{ vData.name }}
                </el-form-item>
                <el-form-item label="数据量: ">
                    {{ vData.count }}
                </el-form-item>
                <el-form-item label="特征数: ">
                    {{ vData.feature_num }}
                </el-form-item>
            </el-form>
        </template>
        <div
            v-else
            class="data-empty"
        >
            查无结果!
        </div>
    </div>
</template>

<script>
    import { reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import { getDataResult } from '@src/service';

    const mixin = resultMixin();

    export default {
        name:       'VertFilter',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            let vData = reactive({
                tabName:     '',
                members:     [],
                resultTypes: [],
                count:       '',
                feature_num: '',
                name:        '',
            });

            let methods = {
                showResult(data) {
                    vData.members = [];
                    methods.getName(data[0] || {});  
                    if (data[0].result && data[0].result.members) {
                        const { members } = data[0].result;

                        vData.tabName = `${members[0].member_id}-${members[0].role}`;
                    }

                    const { result } = data[0] || {};
                    const { train_VertSampleFilter } = result || {};
                    const { data : trainData } = train_VertSampleFilter || {};
                    const { count = {}, feature_num ={} } = trainData || {};

                    vData.count = count.value ||'';
                    vData.feature_num = feature_num.value || '';
                },
                getName(res){
                    const { flow_id, flow_node_id, job_id } = res || {};

                    getDataResult({
                        flowId: flow_id, flowNodeId: flow_node_id, jobId: job_id, type: 'data_normal',
                    }).then((data) => {
                        const { show_name= '' } = data;

                        vData.name = show_name || '';

                    });
                },
            };

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
            };
        },
    };
</script>
