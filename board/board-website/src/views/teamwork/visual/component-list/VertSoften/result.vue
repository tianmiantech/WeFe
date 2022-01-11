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

    const mixin = resultMixin();

    export default {
        name:       'VertSoften',
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
            });

            let methods = {
                showResult(data) {
                    vData.members = [];
                    if (data[0].result && data[0].result.members) {
                        const { members } = data[0].result;

                        vData.tabName = `${members[0].member_id}-${members[0].role}`;
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
            };
        },
    };
</script>
