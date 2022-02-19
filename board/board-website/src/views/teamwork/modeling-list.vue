<template>

    <el-card
        v-loading="vData.loading"
        class="page-card"
        shadow="never"
        name="模型列表"
    >
        <h3 class="mb10">模型列表</h3>
        <el-form inline>
            <el-form-item label="来源组件：">
                <el-select v-model="vData.search.component_type">
                    <el-option
                        v-for="item in vData.component_types"
                        :key="item.value"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="来源流程 id：">
                <el-input
                    v-model="vData.search.flow_id"
                    placeholder="选填"
                />
            </el-form-item>
            <el-form-item label="任务 id：">
                <el-input
                    v-model="vData.search.job_id"
                    placeholder="选填"
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="getList"
                >
                    搜索
                </el-button>
            </el-form-item>
        </el-form>
    </el-card>

    <el-dialog
        title="模型导出"
        v-model="vData.selectLanguage"
        destroy-on-close
        append-to-body
        width="400px"
    >
        <p class="mb10 f14">点击任意语言可下载对应的模型:</p>
        <p class="color-danger mb10 f12">请使用浏览器默认下载器, 否则下载的文件格式可能有误</p>
        <div v-loading="vData.loading" class="select-lang">
            <el-tag
                v-for="item in vData.languages"
                :key="item"
                size="small"
                @click="modelExport($event, item)"
            >
                {{ item }}
            </el-tag>
        </div>
    </el-dialog>
</template>

<script>
    import {
        reactive,
        getCurrentInstance,
        onMounted,
    } from 'vue';
    import { useRoute } from 'vue-router';
    import table from '@src/mixins/table';
    // import RoleTag from '@src/components/views/role-tag';

    export default {
        components: {
            // RoleTag,
        },
        mixins: [table],
        setup(props, context) {
            const route = useRoute();
            const { ctx } = getCurrentInstance();
            const vData = reactive({
                loading:    true,
                isPromoter: false,
                search:     {
                    name:           '',
                    flow_id:        '',
                    component_type: '',
                    project_id:     '',
                    job_id:         '',
                },
                getListApi:      '/project/modeling/query',
                component_types: [{
                    label: '纵向 XGBoost',
                    value: 'VertSecureBoost',
                }, {
                    label: '纵向 LR',
                    value: 'VertLR',
                }, {
                    label: '横向 XGBoost',
                    value: 'HorzSecureBoost',
                }, {
                    label: '横向 LR',
                    value: 'HorzLR',
                }],
                list:       [],
                isShowTopN: true, // show topn
                languages:  [
                    'c',
                    'cSharp',
                    'dart',
                    'go',
                    'haskell',
                    'java',
                    'javaScript',
                    'php',
                    'powerShell',
                    'python',
                    'r',
                    'ruby',
                    'visualBasic',
                    'pmml',
                ],
                selectLanguage: false,
            });
            const modelExport = async(event, language) => {
                const href = `${window.api.baseUrl}/data_output_info/model_export?jobId=${this.selectedRow.job_id}&modelFlowNodeId=${this.selectedRow.flow_node_id}&role=${this.selectedRow.role}&language=${language}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = href;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
            };
            const afterTableRender = () => {};

            onMounted(async () => {
                vData.search.project_id = route.query.project_id;
                await ctx.getList();
            });

            return {
                vData,
                modelExport,
                afterTableRender,
            };
        },
    };
</script>

<style lang="scss" scoped>
.page-card{min-height: calc(100vh - 40px);}
</style>
