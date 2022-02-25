<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        label-position="top"
    >
        <el-form-item label="聚类数目">
            <el-input v-model="vData.soften_rules"></el-input>
        </el-form-item>
    </el-form>
</template>

<script>
    import { reactive, getCurrentInstance } from 'vue';

    export default {
        name:  'VertSoften',
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
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const vData = reactive({
                loading:      false,
                soften_rules: '',
            });
            const methods = {
                async readData (model) {
                    if(vData.loading) return;
                    vData.loading = true;

                    methods.getNodeDetail(model);
                },

                async getNodeDetail(model) {
                    vData.loading = true;
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    vData.loading = false;
                    if (code === 0 && data && data.params && Object.keys(data.params).length) {
                        vData.soften_rules = data.params.soften_rules;
                        vData.inited = true;
                    }
                },

                checkParams() {
                    return {
                        params: {
                            soften_rules: vData.soften_rules,
                        },
                    };
                },
            };

            return {
                methods,
                vData,
            };
        },
    };
</script>
