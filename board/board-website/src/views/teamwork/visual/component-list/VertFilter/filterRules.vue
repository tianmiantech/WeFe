<template>
    <el-form :model="ruleForm" ref="ruleFormRef" size="small">
        <el-row gutter="10">
            <el-col :span="7" class="f12">特征</el-col>
            <el-col :span="7" class="f12">操作符</el-col>
            <el-col :span="7" class="f12">值</el-col>
        </el-row>
        <el-row gutter="10" class="mb5" v-for="(item, index) in ruleForm" :key="index">
            <el-col :span="7">
                <el-form-item
                    :prop="'['+index+'].feature'"
                    :rules="[{required: true, message: '请选择', trigger: 'change'}]"
                >
                    <el-select v-model="item.feature">
                        <el-option
                            v-for="(item, index) in featureList"
                            :key="index"
                            :value="item"
                        >
                            {{ item }} ({{ memberFeatureType[item] }})
                        </el-option>
                    </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="7">
                <el-form-item
                    :prop="`${index}.operator`"
                    :rules="[{required: true, message: '请选择', trigger: 'change'}]"
                >
                    <el-select v-model="item.operator">
                        <template v-if="item.feature">
                            <el-option label=">" value=">" v-show="memberFeatureType[item.feature] != 'String' && memberFeatureType[item.feature] != 'Enum'" />
                            <el-option label="<" value="<" v-show="memberFeatureType[item.feature] != 'String' && memberFeatureType[item.feature] != 'Enum'" />
                            <el-option label=">=" value=">=" v-show="memberFeatureType[item.feature] != 'String' && memberFeatureType[item.feature] != 'Enum'" />
                            <el-option label="<=" value="<=" v-show="memberFeatureType[item.feature] != 'String' && memberFeatureType[item.feature] != 'Enum'" />
                            <el-option label="=" value="=" />
                            <el-option label="!=" value="!=" />
                        </template>
                    </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="7">
                <el-form-item
                    :prop="`${index}.value`"
                    :rules="[{required: true, message: '请输入', trigger: 'blur'}]"
                >
                    <el-input v-model="item.value" />
                </el-form-item>
            </el-col>
            <el-col :span="3">
                <el-icon
                    v-if="(index == ruleForm.length - 1)"
                    size="18"
                    color="#67C23A"
                    class="filter-plus-btn"
                    @click="addRule"
                >
                    <elicon-circle-plus />
                </el-icon>
                <el-icon
                    v-if="(ruleForm.length > 1 && index != ruleForm.length - 1)"
                    size="18"
                    color="#F56C6C"
                    class="filter-plus-btn"
                    @click="delRule(index)"
                >
                    <elicon-remove />
                </el-icon>
            </el-col>
        </el-row>
    </el-form>
    <div class="rule-txt">
        <template v-for="(item, index) in ruleForm" :key="index">
            <span class="color-danger">{{item.feature}}</span>
            <span>{{item.operator}}</span>
            <span>{{item.value}}</span>
            <span class="strong" v-if="(ruleForm.length > 0 && index != ruleForm.length - 1)">&</span>
        </template>
    </div>
</template>

<script>
    import { defineComponent, ref, reactive, watch, computed } from 'vue';
    import { useStore } from 'vuex';

    export default defineComponent({
        name:  'filterRules',
        props: {
            memberData: {
                type:    Array,
                default: () => {},
            },
        },
        emits: ['update'],
        setup(props, { emit }) {
            const store = useStore();
            const ruleFormRef = ref();
            const ruleForm = reactive([{
                feature:  '',
                operator: '',
                value:    '',
            }]);
            const featureList = ref([]);
            const dataSetId = ref('');
            const featureType = computed(() => store.state.base.featureType);
            const memberFeatureType = ref({});

            watch(() => props.memberData, (member) => {
                if (member) {
                    memberFeatureType.value = featureType.value[member.data_set_id];
                    featureList.value = member.features;
                    dataSetId.value = member.data_set_id;
                    if (member.filter_rules) {
                        const reg = /==|!=|>=|>|<=|<|=/;
                        ruleForm.length = 0;
                        const ruleSplit = member.filter_rules.split('&');
                        ruleSplit.forEach(rule => {
                            const operatorMatch = rule.match(reg);
                            const operatorPos = operatorMatch.index;
                            ruleForm.push({
                                feature:  rule.substr(0, operatorPos),
                                operator: operatorMatch[0],
                                value:    rule.substr(operatorPos + operatorMatch[0].length, rule.length),
                            });
                        });
                    }
                }
            }, { deep: true, immediate: true });

            // 新增规则
            const addRule = () => {
                ruleForm.push({
                    feature:  '',
                    operator: '',
                    value:    '',
                });
            };
            // 删除规则
            const delRule = (index) => {
                ruleForm.splice(index, 1);
            };


            const validate = async() => {
                await ruleFormRef.value.validate();
            };
            // 回传规则数据
            const getRule = () => {
                validate();
                let filterRule = '';
                ruleForm.forEach((item, index) => {
                    if (index > 0) filterRule += '&';
                    filterRule += item.feature;
                    filterRule += item.operator;
                    filterRule += item.value;
                });

                for(const i in ruleForm) {
                    if (!ruleForm[i].feature || !ruleForm[i].operator || !ruleForm[i].value) {
                        return '';
                    }
                }
                return filterRule;
            };

            return {
                ruleFormRef,
                ruleForm,
                addRule,
                featureList,
                memberFeatureType,
                getRule,
                delRule,
            };
        },
    });
</script>

<style lang="scss" scoped>
.filter-plus-btn {
    cursor: pointer;
}
.operator-txt {
    display: inline-block;
    color: #909399;
    font-size: 14px;
}
.strong{color:$--color-success;}
</style>
