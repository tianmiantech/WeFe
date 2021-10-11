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
                    <el-tabs v-model="vData.tabName">
                        <el-tab-pane
                            v-for="(member, i) in vData.members"
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
        emits: [...mixin.emits],
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                tabName:   '',
                members:   {},
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
                showResult(data) {
                    if(data.result) {
                        const { members } = data.result;

                        vData.members = {};
                        members.length && members.forEach((member, index) => {
                            const key = `${member.member_id}-${member.role}`;

                            vData.members[key] = {
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

                                /* if(i < vData.members[key].pagination.page_size) {
                                    vData.members[key].list.push(row);
                                } */
                                vData.members[key].list.push(row);
                                vData.members[key].allList.push(row);
                                i++;
                            }
                            vData.members[key].pagination.total = i;

                            if(index === 0) {
                                vData.tabName = key;
                            }
                        });
                    }
                },
                currentPageChange(val, key) {
                    const member = vData.members[key];

                    vData.members[key].list = [];

                    for(let i = 0; i < val * member.pagination.page_size; i++) {
                        if(i >= (val - 1) * member.pagination.page_size) {
                            member.allList[i] && member.list.push(member.allList[i]);
                        }
                    }
                },
                pageSizeChange(val, key) {
                    const member = vData.members[key];

                    member.list = [];

                    for(let i = 0; i < val * member.pagination.page_index; i++) {
                        if(i >= val * (member.pagination.page_index - 1)) {
                            member.allList[i] && member.list.push(member.allList[i]);
                        }
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
