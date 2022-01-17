<template>
    <FeatureStatisticResult
        ref="FeatureStatisticRef"
        reference="HorzStatistic"
        :autoReadResult="autoReadResult"
        :flowId="flowId"
        :flowNodeId="flowNodeId"
        :jobDetail="jobDetail"
        :jobId="jobId"
        :myRole="myRole"
        :isCreator="isCreator"
        :currentObj="currentObj"
    />
</template>

<script>
    import { ref } from 'vue';
    import FeatureStatisticResult from '../FeatureStatistic/result';

    export default {
        name:       'HorzStatistic',
        components: {
            FeatureStatisticResult,
        },
        props: {
            autoReadResult: Boolean,
            flowId:         String,
            flowNodeId:     String,
            jobDetail:      Object,
            jobId:          String,
            myRole:         String,
            isCreator:      Boolean,
            currentObj:     Object,
        },
        setup(props, context) {
            const FeatureStatisticRef = ref();

            const methods = {
                readData (model) {
                    FeatureStatisticRef.value.methods.readData(model);
                },
            };

            return {
                methods,
                FeatureStatisticRef,
            };
        },
    };
</script>
