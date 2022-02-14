<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h3 class="mb30">新建融合任务</h3>
        <el-form style="max-width: 400px;">
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

            <el-form-item>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    提交
                </el-button>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script>
    import {
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

            console.log(userInfo);
            const vData = reactive({
                task: {
                    name:      '',
                    desc:      '',
                    algorithm: '',
                },
                algorithms: [{
                    label: '',
                    value: '',
                }],
                form: {
                    project_type: '',
                },
                promoter: {

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
            };
        },
    };
</script>

<style lang="scss" scoped>

</style>
