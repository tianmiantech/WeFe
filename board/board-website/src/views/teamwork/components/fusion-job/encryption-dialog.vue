<template>
    <el-dialog
        v-model="vData.showDialog"
        title="设置主键"
        width="510px"
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
            :dateRecall="true"
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
    import { ref, reactive } from 'vue';
    import EncryptionGenerator from './encryption-generator';

    export default {
        emits:      ['confirmCheck'],
        components: {
            EncryptionGenerator,
        },
        setup(props, context) {
            const encryptionGeneratorRef = ref(null);
            const vData = reactive({
                role:         '',
                showDialog:   false,
                columns:      [],
                is_trace:     true,
                trace_column: [],
                key2str:      '',
            });
            const methods = {
                init(role, data = {}) {
                    const $ref = encryptionGeneratorRef.value.vData;

                    vData.role = role;
                    vData.showDialog = true;
                    vData.is_trace = data.is_trace != null ? data.is_trace : true;
                    vData.trace_column = data.trace_column || [];
                    vData.key2str = data.key2str || '';
                    vData.columns = data.columns || [];
                    $ref.encryptionList = data.encryptionList || [{
                        features:   '',
                        encryption: '',
                    }];
                },
                confirm() {
                    const $ref = encryptionGeneratorRef.value.vData;

                    for(let i = 0; i < $ref.encryptionList.length; i++) {
                        const item = $ref.encryptionList[i];

                        if(!item.feature) {
                            return false;
                        }
                    }

                    vData.showDialog = false;
                    context.emit('confirmCheck', vData);
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
