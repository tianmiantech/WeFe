<template>
    <el-form
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
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.members.length"
                    title="成员信息"
                    name="2"
                >
                    <el-tabs v-model="vData.tabName">
                        <el-tab-pane
                            v-for="member in vData.members"
                            :key="`${member.member_id}-${member.member_role}`"
                            :name="`${member.member_id}-${member.member_role}`"
                            :label="`${member.member_name} (${member.member_role === 'provider' ? '协作方' : '发起方'})`"
                        >
                            <el-table
                                :data="member.features"
                                class="mid-width"
                                stripe
                                border
                            >
                                <el-table-column type="index" />
                                <el-table-column
                                    label="列名"
                                    prop="name"
                                />
                                <el-table-column
                                    label="CV"
                                    prop="cv"
                                />
                                <el-table-column
                                    label="IV"
                                    prop="iv"
                                />
                            </el-table>
                        </el-tab-pane>
                    </el-tabs>
                </el-collapse-item>
            </el-collapse>
        </template>
        <div
            v-else
            class="data-empty"
        >
            查无结果!
        </div>
    </el-form>
</template>

<script>
    import {
        ref, reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'FeatureCalculation',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                resultTypes: ['model_calculate_result'],
                members:     [],
                tabName:     '',
            });

            let methods = {
                showResult(data) {
                    if(data.result) {
                        vData.members = data.result.result;
                        vData.tabName = `${vData.members[0].member_id}-${vData.members[0].member_role}`;
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
