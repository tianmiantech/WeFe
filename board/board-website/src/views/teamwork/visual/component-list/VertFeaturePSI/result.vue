<template>
    <div v-loading="vData.loading" class="el-form">
        <template v-if="vData.commonResultData.task">
            <el-collapse
                v-model="activeName"
            >
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <template v-if="vData.hasResult">
                    <el-collapse-item title="特征PSI" name="2">
                        
                        <el-tabs v-model="bodyCollapse">
                            <el-tab-pane v-if="vData.promoter_psi" label="promoter" name="b1">
                                <psi-table :tableData="vData.promoter_psi" type="featurePsi" :judge="true" />
                            </el-tab-pane>
                            <el-tab-pane v-for="item in vData.provider_psi" :key="item.member_id" label="provider" :name="item.member_id">
                                <psi-table :tableData="item.feature_psi_results" type="featurePsi" :judge="true" />
                            </el-tab-pane>
                        </el-tabs>
                    </el-collapse-item>

                </template>
            </el-collapse>
        </template>
        <div v-else class="data-empty">查无结果!</div>
    </div>
</template>

<script>
    import { reactive,ref } from 'vue';
    import resultMixin from '../result-mixin';
    import psiTable from '../../components/psi/psi-table.vue';
    import CommonResult from '../common/CommonResult';

    const mixin = resultMixin();

    export default {
        name:       'VertFeaturePSI',
        components: {
            psiTable,
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');
            const bodyCollapse = ref('b1');

            let vData = reactive({
                promoter_psi: {},
                provider_psi: [],
            });

            let methods = {
                showResult(data = []) {
                    const { status,result } = data[0] || {};

                    if (status) {
                        vData.commonResultData = {
                            task: data[0],
                        };
                        const { psi_results } = result || {};
                        const { promoter_psi: { feature_psi_results: promoter_bin_psi_results = {} } = {},provider_psi } = psi_results || {};

                        vData.promoter_psi = promoter_bin_psi_results || {};
                        vData.provider_psi = provider_psi || [];

                        if(result) {
                            vData.hasResult = true;
                        } else {
                            vData.hasResult = false;
                        }
                    }
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
                activeName,
                bodyCollapse,
            };
        },
    };
</script>
