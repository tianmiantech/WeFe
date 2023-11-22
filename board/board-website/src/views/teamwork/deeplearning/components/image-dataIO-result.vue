<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                        :showHistory="false"
                    />
                    <el-form
                        v-if="vData.commonResultData.task.result"
                        class="flex-form"
                    >
                        <el-form-item label="数据量：">
                            {{ vData.commonResultData.task.result.total_data_count }}
                        </el-form-item>
                    </el-form>
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
    import CommonResult from '../../visual/component-list/common/CommonResult.vue';
    import resultMixin from '../../visual/component-list/result-mixin';

    const mixin = resultMixin();

    export default {
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                header:      [],
                datasetList: [],
            });

            let methods = {
                showResult(data) {
                    if (data.result) {
                        vData.result = data.result;
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
                activeName,
                methods,
            };
        },
    };
</script>
