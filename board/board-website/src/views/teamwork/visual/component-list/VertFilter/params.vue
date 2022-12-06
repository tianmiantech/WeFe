<template>
    <el-form
        v-loading="vData.loading"
        class="flex-form"
        @submit.prevent
    >
        <el-form-item label="案例:">
            <p class="f12"><span class="color-danger">date</span><span>>2022-01-01</span>或<span class="color-danger">x1</span>>2<span class="strong">&</span><span class="color-danger">x1</span>&lt;50<span class="strong">&</span><span class="color-danger">x3</span>=100<span class="strong">&</span><span class="color-danger">x5</span>!=30</p>
        </el-form-item>
        <el-form-item label="含义:">
            <p class="f12">其中 <span class="color-danger">x1, x3, x5</span> 为特征名称, 满足 x1>2 <span class="strong">并且</span> x1&lt;50 <span class="strong">并且</span> x3 = 100 <span class="strong">并且</span> x5!=30 的数据样本保留，其他的样本将被删除</p>
        </el-form-item>
        <el-form-item label="支持的操作符:">
            <p class="f12">>, &lt;, >=, &lt;=, =, !=</p>
        </el-form-item>
        <el-form-item label="支持的运算符:">
            <p class="f12">&</p>
        </el-form-item>
        <el-form-item label="注意:">
            <p class="f12 color-danger">操作符两边只能有一个特征</p>
        </el-form-item>

        <el-form
            v-for="member in vData.members"
            :key="`${member.member_id}-${member.member_role}`"
            :disabled="disabled"
            class="flex-form li"
            label-width="80px"
            :model="member"
            @submit.prevent
        >
            <h4 class="f14 mb10">{{ member.member_role === 'promoter' ? '发起方' : '协作方' }}</h4>
            <el-form-item label="成员名称:">
                {{ member.member_name }}
            </el-form-item>
            <el-form-item label="特征列表:">
                <div class="mt5 mb10">
                    <template v-for="(item, $index) in member.features" :key="$index">
                        <el-tag
                            v-if="$index < 20"
                            :label="item"
                            :value="item"
                        >
                            {{ item }}
                        </el-tag>
                    </template>
                    <el-button
                        v-if="member.features.length > 20"
                        size="small"
                        type="primary"
                        class="check-features"
                        @click="methods.checkFeatures(member)"
                    >
                        查看更多
                    </el-button>
                </div>
            </el-form-item>
            <el-form-item
                label="过滤规则:"
                prop="filter_rules"
                :rules="formRules.filterRule"
            >
                <el-input
                    type="textarea"
                    v-model="member.filter_rules"
                    clearable
                />
            </el-form-item>
        </el-form>
    </el-form>
</template>

<script>
    import { reactive, getCurrentInstance,toRaw } from 'vue';
    import { replace } from '../common/utils';
    
    const { $message } = window.$app.config.globalProperties;

    const validator = (value, features = []) => {
        const str = replace(value);
        // const set = new Set();
        const array = str.split('&');

        if(/&$/.test(value)){
            $message.error('不能以&结尾');
            return false;
        }
        for(const i of array){
            const tmp = i.split(/>=|<=|!=|=|>|</g);

            /**
             * 切割后只能为两个
             */
            if(tmp.length !== 2){
                $message.error(`${i}不符合规则`);
                return false;
            }
            const [l, r] = tmp;
            // const reg = /^\d+$|^'.*'$/;
            const reg = /'|"/;
            /**
             * 一方为数字或字符串，一方为特征
             */
            const a = features.includes(l);
            const b = features.includes(r);

            if(!l || !r){
                $message.error(`操作符两边不能为空【${i}】`);
                return false;
            }
            if(!a && !b){
                $message.error(`找不到特征【${i}】`);
                return false;
            } else if(a && b){
                $message.error(`操作符两边不能同时为特征【${i}】`);
                return false;
            } else if(a && reg.test(r)){
                $message.error(`过滤规则中不应该包含引号【'"】【${i}】中【${r}】`);
                return false;
            } else if(b && reg.test(l)){
                $message.error(`过滤规则中不应该包含引号【'"】【${i}】中【${l}】`);
                return false;
            }
        }
        // /**
        //  * 特征不重复
        //  */
        // if(set.size !== array.length){
        //     $message.error('相同特征不能用两次');
        //     return false;
        // }

        return true;

    };

    export default {
        name:  'VertFilter',
        props: {
            isCreator:          Boolean,
            ootModelFlowNodeId: String,
            ootJobId:           String,
            projectId:          String,
            flowId:             String,
            disabled:           Boolean,
            learningType:       String,
            currentObj:         Object,
            jobId:              String,
            class:              String,
        },

        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $alert, $http } = appContext.config.globalProperties;
            const formRules = {
                filterRule: [
                    { required: true, message: '请输入过滤规则' },
                ],
            };
            const vData = reactive({
                loading: false,
                members: [],
            });
            const methods = {
                async readData (model) {
                    vData.loading = true;
                    const { code, data } = await $http.get({
                        url:    '/flow/job/task/feature',
                        params: {
                            job_id:       props.jobId,
                            flow_id:      props.flowId,
                            flow_node_id: model.id,
                        },
                    });

                    if(code === 0) {
                        vData.members = data.members.map(member => {
                            const features = member.features.map(feature => feature.name);

                            return {
                                ...member,
                                features,
                                filter_rules: '',
                            };
                        });
                        await methods.getNodeDetail(model);
                    }
                    vData.loading = false;
                },

                async getNodeDetail(model) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    if (code === 0 && data && data.params && Object.keys(data.params).length) {
                        vData.members.forEach(member => {
                            const item = data.params.members.find(item => item.member_id === member.member_id && item.member_role === member.member_role);

                            if(item) {
                                member.filter_rules = item.filter_rules || '';
                            }
                        });
                        vData.inited = true;
                    }
                },

                checkFeatures({ features }) {
                    $alert('所有特征:', {
                        title:                    '所有特征:',
                        message:                  `<div style="max-height: 80vh;overflow:auto;">${features.join(',')}</div>`,
                        dangerouslyUseHTMLString: true,
                    });
                },

                checkParams() {
                    const members = [];

                    for(const i in vData.members) {
                        const member = vData.members[i];

                        if(!member.filter_rules) {
                            $alert('警告:', {
                                type:                     'warning',
                                title:                    '警告:',
                                message:                  '<div class="color-danger">过滤规则必填!</div>',
                                dangerouslyUseHTMLString: true,
                            });
                            return false;
                        }

                        if(!validator(member.filter_rules, toRaw(member.features))){
                            return false;
                        }

                        members.push({
                            member_id:    member.member_id,
                            member_role:  member.member_role,
                            member_name:  member.member_name,
                            filter_rules: replace(member.filter_rules),
                        });
                    }

                    return {
                        params: {
                            members,
                        },
                    };
                },
            };

            return {
                vData,
                formRules,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .li{margin-top: 20px;
        padding-top: 15px;
        border-top: 1px solid $border-color-base;
    }
    .board-form-item{margin-bottom:0;}
    .strong{color:$--color-success;}
</style>
