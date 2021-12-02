<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                    <el-form
                        v-if="vData.commonResultData.task.result"
                        class="flex-form"
                    >
                        <el-form-item label="数据量/特征量：">
                            {{ vData.commonResultData.task.result.table_create_count }} / {{ vData.commonResultData.task.result.header.length }}
                        </el-form-item>
                    </el-form>
                </el-collapse-item>
                <el-collapse-item title="我方数据集预览" name="2">
                    <el-table
                        :data="vData.datasetList"
                        border
                        stripe
                    >
                        <el-table-column v-for="row in vData.header" :key="row" :prop="row" :label="row" :min-width="row === 'id' ? 140 : 80"></el-table-column>
                    </el-table>
                </el-collapse-item>
            </el-collapse>
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
    import { ref, reactive, getCurrentInstance, onMounted } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'DataIO',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const activeName = ref('1');

            let vData = reactive({
                header:      [],
                datasetList: [],
            });

            let methods = {
                async datasetPreview() {
                    const { code, data } = await $http.get({
                        url:    '/job/data_set/view',
                        params: {
                            jobId:      props.jobId,
                            nodeId:     props.flowNodeId,
                            memberRole: props.myRole,
                        },
                    });

                    if(code === 0) {
                        vData.header = data.header;
                        vData.datasetList = data.list.map(row => {
                            const item = {};

                            data.header.forEach((x, index) => {
                                item[x] = row[index];
                            });

                            return item;
                        });
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

            onMounted(_ => {
                methods.datasetPreview();
            });

            return {
                vData,
                activeName,
                methods,
            };
        },
    };
</script>
