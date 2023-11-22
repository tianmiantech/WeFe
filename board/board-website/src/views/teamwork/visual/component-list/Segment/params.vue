<template>
    <el-form
        ref="form"
        v-loading="vData.loading"
        :disabled="disabled"
        :model="vData.form"
        @submit.prevent
    >
        <el-form-item label="数据切分随机数：">
            <el-input
                v-model="vData.form.split_data_random_num"
                style="width:120px"
                maxlength="9"
            />
            <el-button
                class="ml20"
                @click="methods.randomNum"
            >
                生成随机数
            </el-button>
        </el-form-item>

        <el-form-item label="训练与验证数据比例（%）：">
            <div style="width:100%; overflow:hidden;">
                <div class="float-left">
                    <p style="font-weight:bold;color:#4D84F7;" class="mb5">训练:</p>
                    <el-input-number
                        v-model="vData.form.training_ratio"
                        style="width:100px"
                        size="small"
                    />
                </div>

                <div class="float-right">
                    <p style="font-weight:bold;color:#FF4343;" class="text-r mb5">验证:</p>
                    <el-input-number
                        v-model="vData.form.verification_ratio"
                        style="width:100px"
                        size="small"
                        @change="methods.onDataSetVerificationRatioChange"
                    />
                </div>
            </div>
            <el-slider
                v-model="vData.form.training_ratio"
                :show-tooltip="false"
                @input="methods.onDataSetTrainingVerificationRatioChange"
            />
        </el-form-item>
    </el-form>
</template>

<script>
    import { reactive, getCurrentInstance } from 'vue';

    export default {
        name:  'Segment',
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        setup(props) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const vData = reactive({
                inited:  false,
                loading: false,
                form:    {
                    split_data_random_num: 100,
                    training_ratio:        70,
                    verification_ratio:    30,
                },
            });
            const methods = {
                readData(model) {
                    vData.form = {
                        split_data_random_num: 100,
                        training_ratio:        70,
                        verification_ratio:    30,
                    };
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
                    if(code === 0 && data && data.params && Object.keys(data.params).length) {
                        const { params } = data;

                        vData.form = params;
                        vData.inited = true;
                    }
                },
                randomNum() {
                    vData.form.split_data_random_num = Math.round(Math.random()*(10e5 - 100) + 100);
                },
                // Event: modify validation data scale
                onDataSetVerificationRatioChange(newVaule) {
                    vData.form.training_ratio = 100 - newVaule;
                },
                // Event: drag the training and validation data scale slider
                onDataSetTrainingVerificationRatioChange(newVaule) {
                    vData.form.verification_ratio = 100 - newVaule;
                },
                checkParams() {
                    return {
                        params: vData.form,
                    };
                },
            };

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{
        margin-bottom: 10px;
    }
    .el-slider{
        margin:0 14px;
    }
</style>
