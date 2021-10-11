<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-row :gutter="100">
                <el-col :span="12">
                    <p>这里显示折线图</p>
                </el-col>
            </el-row>
        </el-form>
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

                charts: {
                    day: {
                        show:       true,
                        loading:    true,
                        columns:    [],
                        rows:       [],
                        thresholds: [],
                        apis:       'accuracy',
                        settings:   {
                            xAxisType: 'value',
                        },
                    },
                },
            };
        },
        created() {
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
