<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        label-position="top"
        @submit.prevent
    >
        <el-form-item label="聚类数目">
            <el-input v-model="vData.k"></el-input>
        </el-form-item>
        <el-form-item label="最大迭代次数">
            <el-input v-model="vData.max_iter"></el-input>
        </el-form-item>
        <el-form-item label="收敛条件">
            <el-input v-model="vData.tol"></el-input>
        </el-form-item>
        <el-form-item label="随机种子">
            <el-input v-model="vData.random_stat"></el-input>
        </el-form-item>
    </el-form>

</template>

<script>
    import { reactive, getCurrentInstance, nextTick } from 'vue';

    export default {
        name:  'VertKmeans',
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
                loading:     false,
                k:           0,
                max_iter:    0,
                tol:         0,
                random_stat: 0,
            });
            const methods = {
                async readData (model) {
                    if(vData.loading) return;
                    vData.loading = true;

                    const { code, data }  = await $http.post({
                        url:  '/flow/job/task/feature',
                        data: {
                            job_id:       props.jobId,
                            flow_id:      props.flowId,
                            flow_node_id: model.id,
                        },
                    });

                    nextTick(_ => {
                        vData.loading = false;
                        if(code === 0) {
                            vData.data_set_list = [];
                            data.members.forEach(member => {
                                const $features = member.features.map(feature => feature.name);

                                vData.data_set_list.push({
                                    member_id:   member.member_id,
                                    member_role: member.member_role,
                                    member_name: member.member_name,
                                    columns:     member.features.length,
                                    show:        true,
                                    features:    [],
                                    $features,
                                });
                            });
                            methods.getNodeDetail(model);
                        }
                    });
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
                        const { featureMethods, members, workMode, form } = data.params;

                        vData.form = form;
                        vData.percentages = [];
                        vData.typeChecked = [];

                        featureMethods.forEach(row => {
                            if(row.name !== 'percentile') {
                                vData.typeChecked.push(row.name);
                            } else {
                                vData.percentages.push({
                                    label:   'percentile',
                                    number:  row.value || 50,
                                    checked: true,
                                });
                            }
                        });
                        if(vData.percentages.length === 0) {
                            vData.percentages = [{
                                label:   'percentile',
                                checked: false,
                                number:  50,
                            }];
                        }

                        members.forEach(member => {
                            const item = vData.data_set_list.find(row => row.member_id === member.member_id);

                            if(item) {
                                item.features.push(...member.features);
                            }
                        });
                        vData.workMode = workMode;
                        methods.workModeChange(workMode);
                        vData.inited = true;
                    }
                },

                checkParams() {
                    return {
                        params: {
                            k:           vData.k,
                            max_iter:    0,
                            tol:         0,
                            random_stat: 0,
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

<style lang="scss" scoped>
    .el-checkbox-group{
        max-height: 500px;
        overflow: auto;
        font-size: 14px;
    }
    .el-checkbox{user-select:auto;}
    .el-tag-list{
        max-height: 140px;
        overflow: auto;
    }
</style>
