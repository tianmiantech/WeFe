<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <el-collapse-item
                    title="特征值列表"
                    name="2"
                >
                    <h4>发起方:</h4>
                    <el-table
                        :data="vData.promoterList"
                        stripe
                        border
                    >
                        <el-table-column
                            label="列名"
                            prop="name"
                            width="120"
                        />
                        <el-table-column
                            label="特征值"
                            prop="value"
                        />
                    </el-table>
                    <h4 class="mt10">协作方:</h4>
                    <el-table
                        :data="vData.providerList"
                        stripe
                        border
                    >
                        <el-table-column
                            label="列名"
                            prop="name"
                            width="120"
                        />
                        <el-table-column
                            label="特征值"
                            prop="value"
                        />
                    </el-table>
                </el-collapse-item>
            </el-collapse>
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
    import { ref, reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'VertPCA',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                promoterList: [],
                providerList: [],
            });

            let methods = {
                showResult(data) {
                    vData.promoterList = [];
                    vData.providerList = [];
                    if (data[0].result && data[0].result.statistics_pca) {
                        const { statistics_pca: { data: { cov: { value: {
                            mix_feature_names,
                            eig_values,
                        } } } } } = data[0].result;

                        mix_feature_names.forEach((name, index) => {
                            const item = {
                                name,
                                value: eig_values[index],
                            };

                            if(~name.indexOf('promoter_')) {
                                vData.promoterList.push(item);
                            } else {
                                vData.providerList.push(item);
                            }
                        });
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
            };
        },
    };
</script>
