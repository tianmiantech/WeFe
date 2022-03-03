<template>
    <div class="page" v-loading="vData.loading">
        <el-card class="page_layer">
            <el-form
                :model="vData.form"
                :disabled="!vData.userInfo.admin_role"
                @submit.prevent
                class="config_form"
            >
                <el-row :gutter="24">
                    <el-col>
                        <el-form-item label="环境：">
                            <el-radio-group v-model="vData.form.calculation_engine_config.backend" size="small">
                                <el-radio label="SPARK"></el-radio>
                                <el-radio label="FC"></el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-divider content-position="left">函数计算相关配置</el-divider>
                    <div class="flex_box">
                        <el-col :span="11">
                            <el-card>
                                <el-form-item label="每日费用上限：" :disabled="vData.form.calculation_engine_config.backend === 'FC'">
                                    <el-input type="number" v-model="vData.form.function_compute_config.max_cost_in_day" style="width: 95%" clearable :disabled="vData.form.calculation_engine_config.backend === 'FC'" /> <span style="color: #999">¥</span>
                                </el-form-item>
                                <el-form-item label="每月费用上限：">
                                    <el-input type="number" v-model="vData.form.function_compute_config.max_cost_in_month" style="width: 95%" clearable :disabled="vData.form.calculation_engine_config.backend === 'FC'" /> <span style="color: #999">¥</span>
                                </el-form-item>
                            </el-card>
                        </el-col>
                    </div>
                </el-row>
                <el-row :gutter="24">
                    <el-col :span="24">
                        <el-divider content-position="left">深度学习相关配置</el-divider>
                        <div style="width: 50%">
                            <el-form-item label="芯片：">
                                <el-select v-model="vData.form.deep_learning_config.device" placeholder="请选择芯片">
                                    <el-option label="CPU" value="cpu"></el-option>
                                    <el-option label="GPU" value="gpu"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="飞浆可视化服务地址：">
                                <el-input v-model="vData.form.deep_learning_config.paddle_visual_dl_base_url" style="width: 82%; margin-right: 5px;" placeholder="请输入地址" clearable />
                                <el-button type="primary" @click="methods.jumpToNewPage">跳转</el-button>
                            </el-form-item>
                        </div>
                    </el-col>
                </el-row>
            </el-form>
            <el-button
                v-loading="vData.loading"
                class="save-btn mt10"
                type="primary"
                :disabled="!vData.userInfo.admin_role"
                @click="methods.update"
            >
                提交
            </el-button>
        </el-card>
    </div>
</template>

<script>
    import { computed, reactive, getCurrentInstance, nextTick, onBeforeMount } from 'vue';
    import { useStore } from 'vuex';
    import { useRouter } from 'vue-router';
    export default {
        setup() {
            const store = useStore();
            const router = useRouter();
            const userInfo = computed(() => store.state.base.userInfo);
            const { appContext } = getCurrentInstance();
            const { $http, $message } = appContext.config.globalProperties;
            const vData = reactive({
                userInfo,
                form: {
                    calculation_engine_config: {
                        backend: 'SPARK',
                    },
                    // fc
                    function_compute_config: {
                        max_cost_in_day:   '',
                        max_cost_in_month: '',
                    },
                    // deeplearning
                    deep_learning_config: {
                        device:                    'cpu',
                        paddle_visual_dl_base_url: '',
                    },
                },
                loading: false,
            });
            const methods = {
                async getData() {
                    vData.loading = true;
                    const { code, data } = await $http.post({
                        url:  '/global_config/get',
                        data: { groups: ['function_compute_config', 'deep_learning_config', 'calculation_engine_config'] },
                    });

                    if (code === 0) {
                        vData.form = data;
                    }
                    vData.loading = false;
                },
                async update() {
                    vData.loading = true;
                    const { code } = await $http.post({
                        url:  '/global_config/update',
                        data: { groups: vData.form },
                    });

                    nextTick(_ => {
                        if (code === 0) {
                            $message.success('保存成功!');
                            router.push({ name: 'calculation-engine-config' });
                            methods.getData();
                        }
                        vData.loading = false;
                    });
                },
                jumpToNewPage() {
                    const url = vData.form.deep_learning_config.paddle_visual_dl_base_url;

                    window.open(url, '_blank');
                },
            };

            onBeforeMount(()=> {
                methods.getData();
            });

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
.page_layer {
    padding: 10px;
}
.flex_box {
    width: 100%;
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}
.config_form {
    :deep(.el-form-item) {
        margin-bottom: 0;
    }
}
</style>
