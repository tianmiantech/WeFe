<template>
    <el-form class="f14 mt20">
        <el-form-item v-for="(li, i) in vData.encryptionList" :key="i">
            <el-select
                v-model="li.features"
                :class="{ 'error': li.encryption && li.features.length === 0 }"
                placeholder="特征列, 支持多选"
                style="min-width:300px;"
                clearable
                multiple
            >
                <el-option
                    v-for="(item, index) in columns"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                />
            </el-select>
            <el-select
                v-model="li.encryption"
                :class="['ml10', 'mr20', { 'error': li.features.length && !li.encryption }]"
                placeholder="加密方式"
                style="width:130px"
                clearable
            >
                <el-option
                    v-for="item in vData.encryptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
            </el-select>

            <i class="iconfont icon-circle-plus" @click="methods.addLi" />
            <i :class="['iconfont', 'icon-circle-minus', { disabled: vData.encryptionList.length === 1 }]" @click="methods.removeLi($event, i)" />
        </el-form-item>
        <p v-if="hash_func">主键组合方式: {{ hash_func }}</p>

        <div v-if="isTrace" class="mt20">
            <el-switch v-model="vData.is_trace" class="mr10" />是否需要回溯
            <el-tooltip placement="top" effect="light">
                <template #content>
                    可选回溯字段便于对融合进行追溯。
                    <p class="color-danger">注意：回溯字段必须为date类型</p>
                </template>
                <el-icon class="el-icon-warning color-danger">
                    <elicon-warning />
                </el-icon>
            </el-tooltip>
            <el-form class="flex-form mt10">
                <el-form-item label="回溯字段:">
                    <el-select
                        v-model="vData.trace_column"
                        :class="{'error': vData.is_trace && !vData.trace_column }"
                        :disabled="!vData.is_trace"
                        placeholder="必须为date类型"
                        clearable
                    >
                        <el-option
                            v-for="(item, index) in columns"
                            :key="index"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
            </el-form>
        </div>
    </el-form>
</template>

<script>
    import {
        computed,
        reactive,
        getCurrentInstance,
    } from 'vue';

    export default {
        props: {
            columns: Array,
            isTrace: Boolean,
        },
        setup() {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const vData = reactive({
                encryptions:    [],
                encryptionList: [{
                    features:   '',
                    encryption: '',
                }],
                is_trace:     false,
                trace_column: '',
            });
            const hash_func = computed(() => {
                return vData.encryptionList.reduce((a, b) => {
                    const { features: f, encryption: e } = b;

                    if(e) {
                        // md5(x0 + x1) + ...
                        const result = `${e === 'NONE' ? '' : `${e}(`}${Array.isArray(f) ? f.join(' + ') : f}${e === 'NONE' ? '' : ')'}`;

                        return `${a}${a && a !== 'NONE' ? ' + ' : ''}${result || ''}`;
                    }

                    return a;
                }, '');
            });
            const methods = {
                async getEncryptions() {
                    const { code, data } = await $http.get('/fusion/hash_options_enum');

                    if(code === 0) {
                        vData.encryptions = data.map(x => {
                            return {
                                label: x === 'NONE' ? '无' : x,
                                value: x,
                            };
                        });
                    }
                },

                addLi() {
                    vData.encryptionList.push({
                        features:   '',
                        encryption: '',
                    });
                },
                removeLi(event, i) {
                    vData.encryptionList.splice(i, 1);
                },
            };

            methods.getEncryptions();

            return {
                vData,
                methods,
                hash_func,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-select.error{
        :deep(.el-input__inner){
            border: 1px solid $--color-danger;
        }
    }
    .iconfont{
        font-size: 20px;
        vertical-align: middle;
        margin-left:5px;
        cursor: pointer;
    }
    .icon-circle-plus:hover{color:$--color-primary;}
    .icon-circle-minus{
        color: $--color-danger;
        &.disabled{
            cursor: not-allowed;
            color:$color-text-disabled;
        }
    }
    .el-switch{vertical-align: text-bottom;}
    .el-icon-warning{
        cursor:pointer;
        top:2px;
    }
</style>
