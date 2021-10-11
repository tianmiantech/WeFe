<template>
    <el-form
        v-loading="vData.loading"
        class="result"
    >
        <CommonResult
            v-if="vData.commonResultData.task"
            :result="vData.commonResultData"
            :currentObj="currentObj"
            :jobDetail="jobDetail"
        />
        <div
            v-else
            class="data-empty"
        >
            查无结果!
        </div>
    </el-form>
</template>

<script>
    import {
        reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'FeatureSelection',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            let vData = reactive({});

            let methods = {};

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
