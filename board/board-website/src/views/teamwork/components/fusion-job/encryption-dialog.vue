<template>
    <el-dialog
        v-model="vData.showDialog"
        title="设置主键"
        width="550px"
    >
        <el-alert type="info" :closable="false">
            <template #title>
                对融合字段的处理方式，如 md5(id)+md5(tel) 规则是 id 字段的 md5 加上 tel 字段的 md5 处理
                <p class="color-danger">注意：融合主键需要与合作方做了相同处理，否则无法对齐</p>
            </template>
        </el-alert>

        <EncryptionGenerator
            ref="encryptionGeneratorRef"
            :columns="vData.columns"
            :is-trace="true"
        />

        <div class="text-r">
            <el-button type="primary" @click="methods.confirm">
                确定
            </el-button>
            <el-button @click="vData.showDialog = false;">
                取消
            </el-button>
        </div>
    </el-dialog>
</template>

<script>
    import {
        ref,
        getCurrentInstance,
        nextTick,
        reactive,
    } from 'vue';
    import EncryptionGenerator from './encryption-generator';

    export default {
        emits:      ['confirmCheck'],
        components: {
            EncryptionGenerator,
        },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $message } = appContext.config.globalProperties;
            const encryptionGeneratorRef = ref(null);
            const vData = reactive({
                role:       '',
                showDialog: false,
                columns:    [],
            });
            const methods = {
                init(role, data = {}, fields) {
                    vData.role = role;
                    vData.showDialog = true;
                    if(data.columns && data.columns.length) {
                        vData.columns = data.columns.split(',').map(x => {
                            return {
                                label: x,
                                value: x,
                            };
                        });
                    } else {
                        vData.columns = [];
                    }

                    nextTick(_ => {
                        const $ref = encryptionGeneratorRef.value;

                        if(fields && fields.length) {
                            $ref.vData.encryptionList = fields;
                        } else {
                            $ref.vData.encryptionList = [{
                                features:   '',
                                encryption: '',
                            }];
                        }
                    });
                },
                confirm() {
                    const $ref = encryptionGeneratorRef.value;
                    const { hash_func } = $ref;

                    for(let i = 0; i < $ref.vData.encryptionList.length; i++) {
                        const item = $ref.vData.encryptionList[i];

                        if(!item.features) {
                            return false;
                        }
                    }

                    if($ref.vData.is_trace && !vData.trace_column) {
                        return $message.error('请选择回溯字段');
                    }

                    vData.showDialog = false;
                    context.emit('confirmCheck', { ...$ref.vData, role: vData.role, hash_func });
                },
            };

            return {
                vData,
                methods,
                encryptionGeneratorRef,
            };
        },
    };
</script>
