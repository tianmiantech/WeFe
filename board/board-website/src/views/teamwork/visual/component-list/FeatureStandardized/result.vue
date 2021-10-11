<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <el-collapse v-model="activeName">
            <el-collapse-item title="基础信息" name="1">
                <CommonResult
                    v-if="vData.commonResultData.task"
                    :result="vData.commonResultData"
                    :currentObj="currentObj"
                    :jobDetail="jobDetail"
                />
            </el-collapse-item>
            <el-collapse-item
                v-if="vData.datasetList.length"
                title="数据信息"
                name="2"
            >
                <el-table
                    :data="vData.datasetList"
                    border
                    stripe
                >
                    <el-table-column v-for="row in vData.header" :key="row" :prop="row" :label="row" :min-width="row === 'id' ? 140 : 80"></el-table-column>
                </el-table>
            </el-collapse-item>
        </el-collapse>
        <div
            v-if="vData.datasetList.length === 0"
            class="data-empty"
        >
            <p v-if="myRole === 'promoter'">查无结果!</p>
            <el-alert
                v-else
                title="!!! 协作方无法查看结果"
                style="width:250px;"
                :closable="false"
                type="warning"
                effect="dark"
                class="mb10"
                show-icon
            />
        </div>
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'FeatureStandardized',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            const activeName = ref('1');
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;

            let vData = reactive({
                hasResult:   false,
                header:      [],
                datasetList: [],
            });

            let methods = {
                async showResult() {
                    const { code, data } = await $http.get({
                        url:    '/job/data_set/view',
                        params: {
                            nodeId:     props.flowNodeId,
                            jobId:      props.jobId,
                            memberRole: props.myRole,
                        },
                    });

                    if (code === 0) {
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

            return {
                vData,
                activeName,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .c-grid{
        border: 1px solid #EBEEF5;
        position: relative;
        z-index: 1;
    }
</style>
