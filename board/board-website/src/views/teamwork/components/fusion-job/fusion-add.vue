<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h3 class="mb30">新建融合任务</h3>
        <el-form @submit.prevent>
            <el-form-item label="任务名称:" required>
                <el-input
                    v-model="vData.task.name"
                    show-word-limit
                    maxlength="40"
                    clearable
                />
            </el-form-item>
            <el-form-item label="任务描述:">
                <el-input
                    v-model="vData.task.desc"
                    type="textarea"
                    rows="5"
                    clearable
                />
            </el-form-item>
            <el-form-item label="选择算法:" required>
                <el-select v-model="vData.task.algorithm">
                    <el-option
                        v-for="alg in vData.algorithms"
                        :key="alg.value"
                        :label="alg.label"
                        :value="alg.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item class="member-list">
                融合样本:
                <span class="f12 color-danger">当前已选RSA-PSI算法，发起方或协作方至少一方需要选择布隆过滤器资源</span>

                <div class="el-card p20">
                    <h4 class="f14 mt10">发起方:
                        <el-button type="primary" size="small">
                            添加数据资源
                        </el-button>
                    </h4>
                    <el-form-item :label="userInfo.member_name">

                    </el-form-item>

                    <h4 class="f14">选择协作方:</h4>
                    <el-form-item>
                        <el-radio-group v-model="vData.provider.member_id">
                            <el-radio :label="3">
                                Option A
                            </el-radio>
                            <el-radio :label="6">
                                Option B
                            </el-radio>
                            <el-radio :label="9">
                                Option C
                            </el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="name">
                        <el-button type="primary">
                            添加数据资源
                        </el-button>
                    </el-form-item>
                </div>
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    发起融合/审核通过并运行
                </el-button>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    重新发起融合
                </el-button>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    拒绝+理由
                </el-button>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script>
    import {
        ref,
        computed,
        reactive,
        getCurrentInstance,
    } from 'vue';
    import { useStore } from 'vuex';
    import { useRouter } from 'vue-router';

    export default {
        inject:     ['refresh'],
        components: {

        },
        setup() {
            const router = useRouter();
            const store = useStore();
            const { appContext } = getCurrentInstance();
            const { $http, $message } = appContext.config.globalProperties;
            const userInfo = computed(() => store.state.base.userInfo);

            const vData = reactive({
                task: {
                    name:      '',
                    desc:      '',
                    algorithm: '',
                },
                algorithms: [{
                    label: 'RSA-PSI',
                    value: 'RSA-PSI',
                }],
                form: {
                    project_type: '',
                },
                promoter: {
                    member_id: '',
                },
                provider: {
                    member_id: ref(),
                },
            });
            const methods = {
                deleteDataSetEmit(list, idx) {
                    list.splice(idx, 1);
                },
                async submit(event) {
                    const { code, data } = await $http.post({
                        url:      '/',
                        data:     {},
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        console.log(data);
                        $message.success('任务创建成功!');
                        router.push({
                            name:  '',
                            query: {

                            },
                        });
                    }
                },
            };

            return {
                vData,
                methods,
                userInfo,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{max-width: 400px;}
    .member-list{max-width: 540px;}
</style>
