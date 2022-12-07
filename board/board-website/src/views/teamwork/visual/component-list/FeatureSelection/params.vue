<template>
    <div
        v-if="!(frontStatus.has_i_v && frontStatus.has_c_v)"
        :style="{ marginBottom: '24px' }"
    >
        <el-alert
            title="请在前置流程中使用“WOE编码”计算iv值。"
            type="warning"
            :closable="false"
            v-if="!frontStatus.has_i_v"
        />
        <el-alert
            title="请在前置流程中使用“特征统计”计算cv值及缺失率。"
            type="warning"
            :closable="false"
            v-if="!frontStatus.has_c_v"
        />
    </div>
    <el-space>
        <el-button @click="showDrawer" :disabled="disabled || loading">
            选择特征
        </el-button>
        <template v-if="selectedFeature.length">
            <template v-for="({ color, member }) in colorSet">
                <div :style="{ width: '22px', height: '16px', backgroundColor: color }" />{{ member }}
            </template>
        </template>
    </el-space>
    <FeatureFilter
        ref="tezhenRef"
        :allFeatures="allFeatures"
        :selectedFeature="selectedFeature"
        :selectedConditions="selectedConditions"
        :frontStatus="frontStatus"
        @submit="submitHandle"
    />
    <el-table :data="selectedFeature" v-loading="loading">
        <el-table-column property="name" label="特征" width="150">
            <template v-slot="scope">
                <el-tag
                    :style="{ height: '22px', color: 'white' }"
                    :color="calcColor(scope.row)"
                >
                    <span>{{ scope.row.name }}</span>
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column
            property="data_type"
            label="类型"
            width="120"
        />
        <el-table-column
            v-if="frontStatus.has_c_v"
            property="missing_rate"
            label="缺失率"
            width="120"
            sortable
        />
        <el-table-column property="iv" label="IV" v-if="frontStatus.has_i_v" />
        <el-table-column property="cv" label="CV" v-if="frontStatus.has_c_v" />
    </el-table>
</template>

<script>
import { ref, getCurrentInstance, computed } from 'vue';
import numeral from 'numeral';
import FeatureFilter from './FeatureFilter.vue';
import {useStore} from 'vuex';

const formatNumber = (num) =>
    num === null ? null : numeral(num).format('0.000');
const colors = ['#108ee9', '#87d068', '#2db7f5', '#f50'];

export default {
    name: 'FeatureSelection',
    components: {
        FeatureFilter,
    },
    props: {
        projectId: String,
        flowId: String,
        disabled: Boolean,
        learningType: String,
        currentObj: Object,
        jobId: String,
        class: String,
    },
    setup(props) {
        const { appContext } = getCurrentInstance();
        const { $http,$notify } = appContext.config.globalProperties;
        const store = useStore();
        const loading = ref(false);
        const tezhenRef = ref();
        const allFeatures = ref([]);
        const members = ref([]);
        const selectedFeature = ref([]);
        const selectedConditions = ref([]);
        const frontStatus = ref({});
        const colorSet = computed(() => {
            const temp = allFeatures.value.reduce(
                (acc, { member_name }) =>
                    acc.includes(member_name) ? acc : [...acc, member_name],
                []
            );
            return temp.map((each, index) => ({
                member: each,
                color: colors[index],
            }));
        });
        const featureType = computed(() => store.state.base.featureType);
        const calcColor = (item) =>
            colorSet.value.find((each) => each.member === item.member_name)
                ?.color;
        const readData = async (model) => {
            loading.value = true;
            $http
                .get({
                    url: '/project/flow/node/detail',
                    params: {
                        nodeId: model.id,
                        flow_id: props.flowId,
                    },
                })
                .then((NodeDetailReq) => {
                    if (NodeDetailReq.code === 0) {
                        const { members, conditions } =
                            NodeDetailReq.data.params;
                        selectedFeature.value = members.reduce(
                            (acc, cur) => [
                                ...acc,
                                ...cur.features.map((each) => ({
                                    ...each,
                                    member_name: cur.member_name,
                                    member_id: cur.member_id,
                                })),
                            ],
                            []
                        );
                        if (Array.isArray(conditions))
                            selectedConditions.value = conditions;
                    }
                });
            const { code, data } = await $http.post({
                url: '/flow/job/task/feature',
                data: {
                    job_id: props.jobId,
                    flow_id: props.flowId,
                    flow_node_id: model.id,
                },
            });

            const { has_i_v, has_c_v } = data;
            frontStatus.value = { has_i_v, has_c_v };
            if (code === 0) {
                members.value = data.members;
                const { data_set_id } = data.members[0];
                const response = await $http.get({
                    url: '/table_data_set/column/list',
                    params: {
                        data_set_id,
                    },
                });
                if (response.code === 0) {
                    const { list } = response.data;

                    if (list.length) {
                        allFeatures.value = data.members
                            .reduce(
                                (acc, cur) => [
                                    ...acc,
                                    ...cur.features.map((each) => ({
                                        ...each,
                                        member_name: cur.member_name,
                                        member_id: cur.member_id,
                                        data_set_id: cur.data_set_id,
                                    })),
                                ],
                                []
                            )
                            .map(({ cv, iv, name,data_set_id, ...other }) => ({
                                ...other,
                                cv: formatNumber(cv),
                                iv: formatNumber(iv),
                                name,
                                ...list.find((each) => each.name === name),
                                data_type: (featureType.value[data_set_id] || {})[name],
                            }));
                    }
                }
            }
            loading.value = false;
        };
        const submitHandle = (selected, conditions) => {
            selectedFeature.value = selected;
            selectedConditions.value = conditions;
            tezhenRef.value.open = false;
        };
        const checkParams = () => {
            const tipsArray = [];

            selectedFeature.value.forEach(item => {
                const isNumerical = ['Integer', 'Long']; 
                if(item.data_type && !isNumerical.includes(item.data_type)){
                    tipsArray.push({
                        name:      item.name,
                        data_type: item.data_type,
                    })
                }
            })

            if(tipsArray.length){
                $notify({
                    type:     'warning',
                    offset:   5,
                    duration: 2000,
                    title:    '提示',
                    message:  `请知悉：您当前选择的特征有${tipsArray.length}个不是数值型，部分组件不支持输入非数值型特征，必要时可以通过重新选择、热编码、特征转换等方式处理这些特征。非数值型特征：${tipsArray.reduce((pre,cur)=> pre + `${cur.name}(${cur.data_type}),`, '')}`,
                });
            }

            const temp = members.value.map((each) => ({
                ...each,
                features: selectedFeature.value.filter(
                    (value) => value.member_id === each.member_id
                ),
            }));
            return {
                params: {
                    members: temp,
                    conditions: selectedConditions.value,
                },
            };
        };
        const methods = { checkParams };
        return {
            submitHandle,
            readData,
            tezhenRef,
            allFeatures,
            methods,
            selectedFeature,
            selectedConditions,
            calcColor,
            loading,
            colorSet,
            frontStatus,
            showDrawer: () => {
                tezhenRef.value.open = true;
            },
        };
    },
};
</script>
