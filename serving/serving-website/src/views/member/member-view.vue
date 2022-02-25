<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-form-item label="Model Id：">
                {{ form.model_id }}
            </el-form-item>

            <el-form-item label="算法类型：">
                <div v-if="form.algorithm === 'LogisticRegression'">
                    逻辑回归
                </div>
                <div v-else>
                    安全决策树
                </div>
            </el-form-item>
            <el-form-item label="联邦类型：">
                <div v-if="form.fl_type === 'horizontal'">
                    横向
                </div>
                <div v-else>
                    纵向
                </div>
            </el-form-item>
        </el-form>

        <el-form
            :model="form"
            :label-position="form_label_position"
            label-width="220px"
        >
            <el-row
                v-if="form.algorithm === 'LogisticRegression'"
                :gutter="200"
            >
                <el-col
                    :span="24"
                >
                    <fieldset>
                        <legend>特征权重</legend>

                        <el-table
                            :data="modelingResult"
                            stripe
                            border
                        >
                            <el-table-column
                                label="序号"
                                type="index"
                            />
                            <el-table-column
                                label="值"
                                prop="value"
                            />
                            <el-table-column
                                label="权重"
                                prop="weight"
                            />
                        </el-table>
                    </fieldset>
                </el-col>
            </el-row>
        </el-form>

        <el-row :gutter="100">
            <el-col
                :span="12"
                class="mt20"
            >
                <el-button
                    class="save-btn"
                    type="primary"
                    size="medium"
                    @click="predict"
                >
                    预测
                </el-button>
            </el-col>
        </el-row>
    </el-card>
</template>

<script>
    export default {
        data() {
            return {
                form_label_position: 'right',
                modelingResult:      [],

                // model
                form: {
                    model_id:    '',
                    algorithm:   '',
                    fl_type:     '',
                    model_param: '',
                },

            };
        },
        created() {
            this.getData();
        },
        methods: {
            async getData() {
                const { code, data } = await this.$http.get({
                    url:    '/model/detail',
                    params: {
                        id: this.$route.query.id,
                    },
                });

                if (code === 0) {
                    this.form = data;

                    if(data.model_param && data.model_param.iters) {
                        this.iters = data.model_param.iters;
                        this.isConverged = data.model_param.isConverged;

                        this.modelingResult = [];
                        for(let i = 0; i < data.model_param.header.length; i++) {
                            const value = data.model_param.header[i];

                            this.modelingResult.push({
                                value,
                                weight: data.model_param.weight[value],
                            });
                        }
                        this.modelingResult.push({
                            value:  'intercept',
                            weight: data.model_param.intercept,
                        });
                    }
                }
            },
            predict() {
                const obj = { modelingResult: this.modelingResult, form: this.form };

                localStorage.setItem('model', JSON.stringify(obj));
                this.$router.push({ name: 'model-predict-view', query: {} });
            },
        },
    };
</script>

<style lang="scss" scoped>

    .save-btn {
        width: 100px;
    }
</style>
