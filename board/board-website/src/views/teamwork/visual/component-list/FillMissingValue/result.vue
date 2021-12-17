<template>
    <el-form
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
                    title="缺失值填充结果"
                    name="2"
                >
                    <template
                        v-for="(result, index) in vData.results"
                        :key="index"
                    >
                        <strong class="mb10">{{ result.title }} :
                        </strong>
                        <el-tabs v-model="result.tabName">
                            <el-tab-pane
                                v-for="(member, i) in result.members"
                                :key="`${member.member_id}-${member.member_role}-${i}`"
                                :name="`${member.member_id}-${member.member_role}`"
                                :label="`${member.member_name}-${member.member_role === 'promoter' ? '发起方' : '协作方'}`"
                            >
                                <el-table
                                    :data="member.list"
                                    border
                                    stripe
                                >
                                    <el-table-column type="index" />
                                    <el-table-column
                                        label="特征"
                                        prop="feature"
                                    />
                                    <el-table-column
                                        label="缺失数量"
                                        prop="missing_count"
                                    />
                                    <el-table-column label="填充方式">
                                        <template v-slot="scope">
                                            {{ vData.methodObj[scope.row.method] }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="填充值"
                                        prop="value"
                                    />
                                </el-table>
                            </el-tab-pane>
                        </el-tabs>
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
    </el-form>
</template>

<script>
    import { ref, reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'FillMissingValue',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                results:   [],
                methodObj: {
                    'max':    '最大值',
                    'min':    '最小值',
                    'const':  '常量',
                    'mean':   '平均值',
                    'median': '中位数',
                    // 'mode':   '众数',
                },
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map(data => {
                        const { members } = data.result;
                        const result = {
                            title:   data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            tabName: '',
                            members: [],
                        };

                        members.forEach((member, index) => {
                            const key = `${member.member_id}-${member.role}`;
                            const res = {
                                member_id:   member.member_id,
                                member_name: member.member_name,
                                member_role: member.role,
                                list:        [],
                                allList:     [],
                                pagination:  {
                                    page_index: 1,
                                    page_size:  10,
                                    total:      0,
                                },
                            };

                            let i = 0;

                            for(const $key in member.result) {
                                const val = member.result[$key];
                                const row = {
                                    feature: $key,
                                    ...val,
                                };

                                res.list.push(row);
                                res.allList.push(row);
                                i++;
                            }
                            res.pagination.total = i;
                            if(index === 0) {
                                result.tabName = key;
                            }

                            result.members.push(res);
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
