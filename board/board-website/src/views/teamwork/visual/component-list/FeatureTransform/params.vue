<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        label-position="top"
        @submit.prevent
    >
        <el-tabs>
            <el-tab-pane
                v-for="(member, memberIndex) in vData.members"
                :key="`${member.member_id}-${member.member_role}`"
                :label="`${member.member_name} (${member.member_role === 'promoter' ? '发起方' : '协作方'})`"
                :name="`${memberIndex}`"
            >
                <el-table
                    :data="member.features"
                    border
                    stripe
                >
                    <el-table-column label="特征名称" prop="name" width="100" />
                    <el-table-column label="类型" prop="data_type" width="70" />
                    <el-table-column label="特征类型转换" min-width="200">
                        <template v-slot="scope">
                            <div
                                v-for="(item, index) in scope.row.transforms"
                                :key="index"
                                class="mb10"
                            >
                                <el-input
                                    v-model="item[0]"
                                    placeholder="枚举"
                                    style="width:80px;"
                                /> ：
                                <el-input
                                    v-model="item[1]"
                                    placeholder="数值"
                                    style="width:66px;"
                                    class="ml5"
                                    @keyup.enter="methods.addTransform($event, member, scope.$index)"
                                />
                                <el-icon
                                    class="ml10 color-danger"
                                    style="cursor: pointer;"
                                    @click="methods.removeTransform($event, member, scope.$index, index)"
                                >
                                    <elicon-delete />
                                </el-icon>
                            </div>
                            <el-button
                                size="small"
                                type="primary"
                                icon="eliconCirclePlus"
                                @click="methods.addTransform($event, member, scope.$index)"
                            >
                                转换类型
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>
        </el-tabs>
    </el-form>
</template>

<script>
    import {
        reactive,
        nextTick,
        getCurrentInstance,
        computed,
    } from 'vue';
    import { useStore } from 'vuex';

    export default {
        name:  'FeatureTransform',
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const store = useStore();

            const featureType = computed(() => store.state.base.featureType);

            const vData = reactive({
                members: [],
            });

            const methods = {
                async readData (model) {
                    if(vData.loading) return;
                    vData.loading = true;

                    const { code, data }  = await $http.post({
                        url:  '/flow/job/task/feature',
                        data: {
                            job_id:       props.jobId,
                            flow_id:      props.flowId,
                            flow_node_id: model.id,
                        },
                    });

                    nextTick(_ => {
                        vData.loading = false;
                        if(code === 0) {
                            vData.members = data.members.map(member => {
                                const { data_set_id } = member;
                                const features = member.features.map(feature => {
                                    return {
                                        name:       feature.name,
                                        data_type:  (featureType.value[data_set_id] || {})[feature.name],
                                        transforms: [],
                                    };
                                });

                                return {
                                    ...member,
                                    features,
                                };
                            });
                            methods.getNodeDetail(model);
                        }
                    });
                },

                async getNodeDetail(model) {
                    vData.loading = true;
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    vData.loading = false;
                    if (code === 0 && data && data.params && Object.keys(data.params).length) {
                        const { params: { members } } = data;

                        members.forEach(item => {
                            const member = vData.members.find(member => member.member_id === item.member_id && member.member_role === item.member_role);

                            if(member) {
                                const { features } = item;

                                member.features.forEach(feature => {
                                    const target = features.find(x => x.name === feature.name);

                                    if(target) {
                                        feature.transforms = target.transforms;
                                    }
                                });
                            }
                        });
                        vData.inited = true;
                    }
                },

                addTransform(event, member, featureIndex) {
                    member.features[featureIndex].transforms.push(['', '']);
                },

                removeTransform(event, member, featureIndex, transformIndex) {
                    member.features[featureIndex].transforms.splice(transformIndex, 1);
                },

                checkParams() {
                    const members = vData.members.map(member => {
                        const features = [];

                        member.features.forEach(({ name, transforms }) => {
                            if(transforms && transforms.length) {
                                const maps = transforms.map(x => {
                                    return [x[0], +x[1]];
                                });

                                features.push({
                                    name,
                                    transforms: maps,
                                });
                            }
                        });

                        return {
                            ...member,
                            features,
                        };
                    });

                    return {
                        params: {
                            members,
                        },
                    };
                },
            };

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>

</style>
