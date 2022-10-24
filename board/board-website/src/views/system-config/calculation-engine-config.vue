<template>
    <div class="page" v-loading="vData.loading">
        <el-card class="page_layer">
            <el-form
                :model="vData.form"
                :disabled="!vData.userInfo.admin_role"
                @submit.prevent
                class="config_form"
                label-width="180px"
                :inline="true"
            >
                <el-divider content-position="left">机器学习相关配置</el-divider>
                <el-row :gutter="24">
                    <el-col>
                        <el-form-item label="计算引擎：">
                            <el-radio-group v-model="vData.form.calculation_engine_config.backend" size="small">
                                <el-radio label="SPARK">Spark（单机）</el-radio>
                                <el-radio label="FC">函数计算</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <div class="flex_box">
                        <el-col :span="24">
                            <el-card v-show="vData.form.calculation_engine_config.backend === 'SPARK'">
                                <el-row :gutter="24">
                                    <el-col :span="11">
                                        <el-form-item label="硬件加速：">
                                            <el-radio-group v-model="vData.form.spark_standalone_config.hardware_acceleration" size="small">
                                                <el-radio label="NONE">无</el-radio>
                                                <el-radio label="GPU">GPU</el-radio>
                                            </el-radio-group>
                                        </el-form-item>
                                        <el-form-item label="driver内存上限：">
                                            <el-input v-model="vData.form.spark_standalone_config.driver_memory" clearable />
                                        </el-form-item>
                                        <el-form-item label="结果集的最大大小：">
                                            <el-input v-model="vData.form.spark_standalone_config.driver_max_result_size" clearable />
                                        </el-form-item>
                                        <el-form-item label="每个executor的内存上限：">
                                            <el-input v-model="vData.form.spark_standalone_config.executor_memory" clearable />
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="11">
                                    </el-col>
                                </el-row>
                            </el-card>
                            <el-card v-show="vData.form.calculation_engine_config.backend === 'FC'" style="min-height:350px">
                                <el-row :gutter="24">
                                    <el-col :span="11">
                                        <el-form-item label="云服务供应商：">
                                            <el-radio-group v-model="vData.form.function_compute_config.cloud_provider" size="small">
                                                <el-radio label="aliyun">阿里云</el-radio>
                                                <el-radio label="tencentcloud">腾讯云</el-radio>
                                            </el-radio-group>
                                        </el-form-item>
                                        <el-form-item label="每日费用上限：">
                                            <el-input type="number" v-model="vData.form.function_compute_config.max_cost_in_day" clearable style="width:80%" />&nbsp;<span style="color: #999">¥</span>
                                        </el-form-item>
                                        <el-form-item label="每月费用上限：">
                                            <el-input type="number" v-model="vData.form.function_compute_config.max_cost_in_month" clearable style="width:80%" />&nbsp;<span style="color: #999">¥</span>
                                        </el-form-item>
                                        <div v-if="vData.form.function_compute_config.cloud_provider==='aliyun'">
                                            <el-form-item label="账号ID：">
                                                <el-input v-model="vData.form.aliyun_function_compute_config.account_id" clearable />
                                            </el-form-item>
                                            <el-form-item label="AccessKeyId：">
                                                <el-input v-model="vData.form.aliyun_function_compute_config.access_key_id" clearable />
                                            </el-form-item>
                                            <el-form-item label="AccessKeySecret：">
                                                <el-input
                                                    v-model="vData.form.aliyun_function_compute_config.access_key_secret"
                                                    clearable
                                                    type="password"
                                                    placeholder="请输入密码"
                                                    autocomplete="new-password"
                                                    @contextmenu.prevent
                                                    @change="methods.accessKeySecretChange"
                                                />
                                            </el-form-item>
                                        </div>
                                        <div v-if="vData.form.function_compute_config.cloud_provider==='tencentcloud'">
                                            <el-form-item label="账号ID：">
                                                <el-input v-model="vData.form.tencent_serverless_cloud_function_config.account_id" clearable />
                                            </el-form-item>
                                            <el-form-item label="AccessKeyId：">
                                                <el-input v-model="vData.form.tencent_serverless_cloud_function_config.access_key_id" clearable />
                                            </el-form-item>
                                            <el-form-item label="AccessKeySecret：">
                                                <el-input
                                                    v-model="vData.form.tencent_serverless_cloud_function_config.access_key_secret"
                                                    clearable
                                                    type="password"
                                                    placeholder="请输入密码"
                                                    autocomplete="new-password"
                                                    @contextmenu.prevent
                                                    @change="methods.tencentAccessKeySecretChange"
                                                />
                                            </el-form-item>
                                        </div>
                                    </el-col>
                                    <el-col :span="10" v-if="vData.form.function_compute_config.cloud_provider==='aliyun'">
                                        <el-form-item label="账号类型：">
                                            <el-radio-group v-model="vData.form.aliyun_function_compute_config.account_type" size="small">
                                                <el-radio label="admin"></el-radio>
                                                <el-radio label="api"></el-radio>
                                            </el-radio-group>
                                        </el-form-item>
                                        <el-form-item label="云服务所在区域：">
                                            <el-input v-model="vData.form.aliyun_function_compute_config.region" clearable />
                                        </el-form-item>
                                        <el-form-item label="OSS bucket名称：">
                                            <el-input v-model="vData.form.aliyun_function_compute_config.oss_bucket_name" clearable />
                                        </el-form-item>
                                        <el-form-item label="专有网络ID（可选）：">
                                            <el-input v-model="vData.form.aliyun_function_compute_config.vpc_id" clearable />
                                        </el-form-item>
                                        <el-form-item label="交换机ID（可选）：">
                                            <el-input v-model="vData.form.aliyun_function_compute_config.v_switch_ids" clearable />
                                        </el-form-item>
                                        <el-form-item label="安全组ID（可选）：">
                                            <el-input v-model="vData.form.aliyun_function_compute_config.security_group_id" clearable />
                                        </el-form-item>
                                        <el-form-item label="版本号：">
                                            <el-input v-model="vData.form.aliyun_function_compute_config.qualifier" clearable />
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="10" v-if="vData.form.function_compute_config.cloud_provider==='tencentcloud'">
                                        <el-form-item label="SCF 服务地址：">
                                            <el-input
                                                v-model="vData.form.tencent_serverless_cloud_function_config.scf_server_url"
                                                placeholder="https://service-xxx-xxx.gz.apigw.tencentcs.com/release/invoke"
                                                clearable />
                                        </el-form-item>
                                        <el-form-item label="云服务所在区域：">
                                            <el-input v-model="vData.form.tencent_serverless_cloud_function_config.region" clearable />
                                        </el-form-item>
                                        <el-form-item label="COS bucket名称：">
                                            <el-input v-model="vData.form.tencent_serverless_cloud_function_config.cos_bucket_name" clearable />
                                        </el-form-item>
                                        <el-form-item label="版本号：">
                                            <el-input v-model="vData.form.tencent_serverless_cloud_function_config.qualifier" clearable />
                                        </el-form-item>
                                    </el-col>
                                </el-row>

                            </el-card>
                        </el-col>

                    </div>
                </el-row>
                <el-divider content-position="left">视觉处理相关配置</el-divider>
                <el-row :gutter="24">
                    <el-col :span="24">
                        <div style="width: 50%">
                            <el-form-item label="芯片：">
                                <el-select v-model="vData.form.deep_learning_config.device" placeholder="请选择芯片">
                                    <el-option label="CPU" value="cpu"></el-option>
                                    <el-option label="GPU" value="gpu"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="VisualFL服务地址：" class="mt10">
                                <el-input v-model="vData.form.deep_learning_config.paddle_visual_dl_base_url" style="width: 82%; margin-right: 5px;" placeholder="请输入地址" clearable />
                                <!-- <el-button type="primary" @click="methods.jumpToNewPage">跳转</el-button> -->
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
    import Rsa from '@/utils/rsa.js';
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
                        cloud_provider:    '',
                    },
                    aliyun_function_compute_config: {
                        account_type:      '',
                        account_id:        '',
                        region:            '',
                        access_key_id:     '',
                        access_key_secret: '',
                        oss_bucket_name:   '',
                        vpc_id:            '',
                        v_switch_ids:      '',
                        security_group_id: '',
                        qualifier:         '',
                    },
                    tencent_serverless_cloud_function_config: {
                        account_type:      '',
                        account_id:        '',
                        region:            '',
                        access_key_id:     '',
                        access_key_secret: '',
                        cos_bucket_name:   '',
                        qualifier:         '',
                        scf_server_url:    '',
                    },
                    // deeplearning
                    deep_learning_config: {
                        device:                    'cpu',
                        paddle_visual_dl_base_url: '',
                    },
                    spark_standalone_config: {
                        driver_memory:          '',
                        driver_max_result_size: '',
                        executor_memory:        '',
                    },
                },
                loading:                           false,
                isChangeAccessKeySecretPwd:        false,
                isChangeTencentAccessKeySecretPwd: false,
                publicKey:                         '',
            });
            const methods = {
                async getData() {
                    vData.loading = true;
                    const { code, data } = await $http.post({
                        url:  '/global_config/get',
                        data: { groups: [
                            'function_compute_config',
                            'deep_learning_config',
                            'calculation_engine_config',
                            'spark_standalone_config',
                            'aliyun_function_compute_config',
                            'tencent_serverless_cloud_function_config',
                        ] },
                    });

                    if (code === 0) {
                        vData.form = data;
                    }
                    vData.loading = false;
                },
                accessKeySecretChange() {
                    vData.isChangeAccessKeySecretPwd = true;
                },
                tencentAccessKeySecretChange() {
                    vData.isChangeTencentAccessKeySecretPwd = true;
                },
                async getGenerate_rsa_key_pair() {
                    const { code, data } = await $http.get('/crypto/generate_rsa_key_pair');

                    if (code === 0 && data && data.public_key) {
                        const { public_key } = data;

                        vData.publicKey = public_key;
                    }
                },
                async update() {
                    // 判断是否修改过密码
                    if (vData.isChangeAccessKeySecretPwd || vData.isChangeTencentAccessKeySecretPwd) {
                        await methods.getGenerate_rsa_key_pair();
                        if (vData.isChangeAccessKeySecretPwd && vData.isChangeTencentAccessKeySecretPwd) {
                            vData.form.aliyun_function_compute_config.access_key_secret = Rsa.encrypt(vData.publicKey, vData.form.aliyun_function_compute_config.access_key_secret);
                            vData.form.tencent_serverless_cloud_function_config.access_key_secret = Rsa.encrypt(vData.publicKey, vData.form.tencent_serverless_cloud_function_config.access_key_secret);
                        }
                        if (vData.isChangeAccessKeySecretPwd && !vData.isChangeTencentAccessKeySecretPwd) {
                            vData.form.aliyun_function_compute_config.access_key_secret = Rsa.encrypt(vData.publicKey, vData.form.aliyun_function_compute_config.access_key_secret);
                            vData.form.tencent_serverless_cloud_function_config.access_key_secret = null;
                        }
                        if(!vData.isChangeAccessKeySecretPwd && vData.isChangeTencentAccessKeySecretPwd) {
                            vData.form.aliyun_function_compute_config.access_key_secret = null;
                            vData.form.tencent_serverless_cloud_function_config.access_key_secret = Rsa.encrypt(vData.publicKey, vData.form.tencent_serverless_cloud_function_config.access_key_secret);
                        }
                    } else {
                        vData.form.aliyun_function_compute_config.access_key_secret = null;
                        vData.form.tencent_serverless_cloud_function_config.access_key_secret = null;
                    }

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
                        vData.isChangeAccessKeySecretPwd = false;
                        vData.isChangeTencentAccessKeySecretPwd = false;
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
        margin-bottom: 8px;
        width: 100%;
    }
}
</style>
