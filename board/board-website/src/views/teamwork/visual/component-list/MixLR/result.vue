<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.results.length"
                    title="特征权重"
                    name="2"
                >
                    <template
                        v-for="(result, index) in vData.results"
                        :key="index"
                    >
                        <p class="mb10"><strong>{{ result.title }} :</strong></p>
                        <el-table
                            :data="result.tableData"
                            style="max-width:355px;"
                            max-height="600px"
                            class="mb20"
                            stripe
                            border
                        >
                            <el-table-column
                                type="index"
                                label="序号"
                                width="60"
                            />
                            <el-table-column
                                prop="feature"
                                label="特征"
                                width="80"
                            />
                            <el-table-column
                                prop="weight"
                                label="权重"
                            />
                        </el-table>
                        <el-divider v-if="index === 0"></el-divider>
                    </template>
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
    import {
        ref, reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'MixLR',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                results:             [],
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map(data => {
                        const result = {
                            title:     data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            tableData: [],
                        };
                        const {
                            model_param: {
                                intercept,
                                weight,
                            },
                        } = data.result;

                        for(const key in weight) {
                            result.tableData.push({
                                feature: key,
                                weight:  weight[key],
                            });
                        }
                        result.tableData.push({
                            feature: 'b',
                            weight:  intercept,
                        });

                        return result;
                    });
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
