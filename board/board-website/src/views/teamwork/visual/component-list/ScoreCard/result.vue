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
            <div v-if="vData.resultConfigs.length">
                <el-divider></el-divider>
            </div>
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
    import { reactive, ref, onBeforeMount, getCurrentInstance, nextTick } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'ScoreCard',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            let vData = reactive({
                resultTypes:   ['metric'],
                resultConfigs: [],
            });

            let methods = {
                
            };

            onBeforeMount(() => {
                
            });

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
