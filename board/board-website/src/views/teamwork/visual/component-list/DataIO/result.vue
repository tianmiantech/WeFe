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
                <el-collapse-item
                    v-loading="vData.gridLoading"
                    title="我方数据资源预览"
                    name="2"
                >
                    <c-grid
                        v-if="!vData.gridLoading"
                        :theme="vData.gridTheme"
                        :data="vData.table_data.rows"
                        :frozen-col-count="1"
                        font="12px sans-serif"
                        :style="{height:`${vData.gridHeight}px`}"
                    >
                        <c-grid-column
                            v-for="(item, index) in vData.table_data.header"
                            :key="index"
                            :field="item"
                            min-width="100"
                            :width="item === vData.table_data.header[0] ? 240 : 'auto'"
                            :column-style="{textOverflow: 'ellipsis'}"
                        >
                            {{ item }}
                        </c-grid-column>
                    </c-grid>
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
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const activeName = ref(['1', '2']);

            let vData = reactive({
                loading:    false,
                table_data: {
                    header: [],
                    rows:   [],
                },
                gridTheme: {
                    color:       '#6C757D',
                    borderColor: '#EBEEF5',
                },
                gridHeight: 0,
            });

            let methods = {
                async datasetPreview() {
                    vData.gridLoading = true;
                    const { code, data } = await $http.get({
                        url:    '/job/data_set/view',
                        params: {
                            jobId:      props.jobId,
                            nodeId:     props.flowNodeId,
                            memberRole: props.myRole,
                        },
                    });

                    if(code === 0 && data && data.list){
                        const rows = data.list;

                        methods.resize(rows.length);

                        setTimeout(() => {
                            vData.gridLoading = false;
                            vData.table_data.header = data.header;
                            vData.table_data.rows = rows.map(item => {
                                const obj = {};

                                data.header.forEach((x, index) => {
                                    obj[x] = item[index];
                                });

                                return obj;
                            });
                        });
                    }
                },
                resize(length) {
                    vData.gridHeight = 40 * length;
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
