<template>
    <el-card
        v-loading="loading"
        class="page"
        shadow="never"
    >
        <div
            v-if="form.algorithm === 'XGBoost'"
            id="canvas"
            ref="canvas"
            class="mb20"
            style="background: #f9f9f9;"
        />

        <el-form
            :model="form"
            inline
        >
            <el-row
                v-if="form.algorithm === 'LogisticRegression'"
                :gutter="200"
                style="margin-bottom: 20px;"
            >
                <el-col
                    :span="24"
                    style="margin:20px;"
                >
                    <el-divider content-position="center">
                        特征权重
                    </el-divider>
                    <!-- <span>特征权重</span> -->
                    <!--   <fieldset> -->
                    <!-- <legend>特征权重</legend> -->

                    <el-table
                        :data="modelingResult"
                        stripe
                        border
                        lass="infinite-list-wrapper"
                        style="overflow:auto;min-height:200px;max-height:300px;margin-top:20px"
                    >
                        <el-table-column
                            label="序号"
                            type="index"
                        />
                        <el-table-column
                            label="值"
                            prop="name"
                        />
                        <el-table-column
                            label="权重"
                            prop="weight"
                        />
                    </el-table>
                </el-col>
            </el-row>

            <el-divider content-position="center">
                特征配置
            </el-divider>
            <el-row :gutter="40">
                <el-col
                    :span="10"
                    style="margin-left: 20px"
                >
                    <el-form
                        ref="form"
                        :model="form"
                        label-width="120px"
                    >
                        <el-form-item
                            label="角色标签："
                            style="word-break: break-all;"
                        >
                            {{ form.my_role.join(', ') }}
                        </el-form-item>
                        <el-form-item
                            label="模型 ID："
                            style="word-break: break-all;"
                        >
                            {{ form.model_id }}
                        </el-form-item>

                        <div>
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

                            <el-form-item label="特征来源：">
                                <el-radio
                                    v-model="form.feature_source"
                                    label="api"
                                >
                                    API入参
                                </el-radio>
                                <el-radio
                                    v-model="form.feature_source"
                                    label="code"
                                >
                                    代码配置
                                </el-radio>
                                <el-radio
                                    v-model="form.feature_source"
                                    label="sql"
                                >
                                    SQL配置
                                </el-radio>
                            </el-form-item>
                        </div>
                    </el-form>
                </el-col>

                <el-col :span="12">
                    <form v-if="form.feature_source=='api'">
                        <fieldset>
                            <legend>调试</legend>
                            <el-form-item label="特征值：">
                                <el-input
                                    v-model="api.feature_data"
                                    type="textarea"
                                    rows="5"
                                    style="color: #333333;width:400px;"
                                    placeholder="如:{&quot;x0&quot;:0.2323,&quot;x1&quot;:0.1}"
                                    clearable
                                />
                            </el-form-item>

                            <el-row>
                                <el-col :span="18">
                                    <el-form-item label="用户标识：">
                                        <el-input
                                            v-model="api.user_id"
                                            placeholder="如：15555555555"
                                            clearable
                                        />
                                    </el-form-item>
                                </el-col>
                                <el-col :span="6">
                                    <el-form-item>
                                        <el-button
                                            :disabled="!api.feature_data || !api.user_id"
                                            type="primary"
                                            plain
                                            @click="testApi"
                                        >
                                            预测
                                        </el-button>
                                        <el-tooltip>
                                            <div slot="content">输入用户标识后才可进行预测</div>
                                            <i class="el-icon-info" />
                                        </el-tooltip>
                                    </el-form-item>
                                </el-col>
                            </el-row>

                            <el-row
                                v-if="apiPredictResult.algorithm"
                                :gutter="200"
                            >
                                <el-col
                                    :span="24"
                                    class="mt20"
                                >
                                    <el-form-item label="预测结果：">
                                        <JsonViewer
                                            :value="apiPredictResult"
                                            copyable
                                        />
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </fieldset>
                    </form>
                </el-col>

                <el-col :span="12">
                    <form v-if="form.feature_source=='code'">
                        <fieldset>
                            <legend>调试</legend>
                            <el-row>
                                <el-form-item label="处理器：">
                                    <el-input
                                        v-model="form.processor"
                                        :disabled="true"
                                    />
                                </el-form-item>

                                <el-col :span="18">
                                    <el-form-item label="用户标识：">
                                        <el-input
                                            v-model="code.user_id"
                                            placeholder="如：15555555555"
                                            clearable
                                        />
                                    </el-form-item>
                                </el-col>
                                <el-col :span="6">
                                    <el-form-item>
                                        <el-button
                                            :disabled="!code.user_id"
                                            type="primary"
                                            plain
                                            @click="testCode"
                                        >
                                            预测
                                        </el-button>
                                        <el-tooltip>
                                            <div slot="content">输入用户标识后才可进行预测</div>
                                            <i class="el-icon-info" />
                                        </el-tooltip>
                                    </el-form-item>
                                </el-col>
                            </el-row>

                            <el-row
                                v-if="codePredictResult.algorithm"
                                :gutter="200"
                            >
                                <el-col
                                    :span="24"
                                    class="mt20"
                                >
                                    <el-form-item label="预测结果：">
                                        <JsonViewer
                                            :value="codePredictResult"
                                            copyable
                                        />
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </fieldset>
                    </form>
                </el-col>

                <el-col :span="12">
                    <form v-if="form.feature_source=='sql'">
                        <fieldset>
                            <legend>sql配置</legend>
                            <el-form-item
                                label="DB类型："
                                :rules="[{required: true, message: 'DB类型必填!'}]"
                            >
                                <el-select
                                    v-model="model_sql_config.type"
                                    placeholder="请选择DB类型"
                                    clearable
                                >
                                    <el-option
                                        v-for="(item) in databaseOptions"
                                        :key="item.value"
                                        :value="item.value"
                                        :label="item.label"
                                    />
                                </el-select>
                            </el-form-item>
                            <el-form-item
                                label="链接地址："
                                :rules="[{required: true, message: '链接地址必填!'}]"
                            >
                                <el-input v-model="model_sql_config.url" />
                            </el-form-item>
                            <el-form-item label="用户名：">
                                <el-input v-model="model_sql_config.username" />
                            </el-form-item>
                            <el-form-item label="密码：">
                                <el-input
                                    v-model="model_sql_config.password"
                                    show-password
                                    @paste.native.prevent
                                    @copy.native.prevent
                                    @contextmenu.native.prevent
                                />
                            </el-form-item>
                            <el-form-item
                                label="SQL："
                                :rules="[{required: true, message: 'SQL必填!'}]"
                            >
                                <el-input
                                    v-model="model_sql_config.sql_context"
                                    type="textarea"
                                    autosize
                                    placeholder="如：select x0,x1,x2 form table where user_id = ?"
                                    clearable
                                />
                            </el-form-item>

                            <el-row>
                                <el-col :span="18">
                                    <el-form-item label="用户标识：">
                                        <el-input
                                            v-model="model_sql_config.user_id"
                                            placeholder="如：15555555555"
                                            clearable
                                        />
                                    </el-form-item>
                                </el-col>
                                <el-col :span="6">
                                    <el-form-item>
                                        <el-button
                                            :disabled="!model_sql_config.user_id"
                                            type="primary"
                                            plain
                                            @click="testSql"
                                        >
                                            预测
                                        </el-button>
                                        <el-tooltip>
                                            <div slot="content">输入用户标识后才可进行预测</div>
                                            <i class="el-icon-info" />
                                        </el-tooltip>
                                    </el-form-item>
                                </el-col>
                            </el-row>

                            <el-row
                                v-if="sqlPredictResult.algorithm"
                                :gutter="200"
                            >
                                <el-col
                                    :span="24"
                                    class="mt20"
                                >
                                    <el-form-item label="预测结果：">
                                        <JsonViewer
                                            :value="sqlPredictResult"
                                            copyable
                                        />
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </fieldset>
                    </form>
                </el-col>
            </el-row>
        </el-form>

        <el-button
            style="margin: 0 20px;"
            class="save-btn"
            type="primary"
            size="medium"
            :disabled="form.feature_source==='sql' && (!model_sql_config.type || !model_sql_config.url || !model_sql_config.sql_context)"
            @click="saveConfig"
        >
            保存
        </el-button>
    </el-card>
</template>


<script>
    import { TreeGraph, Grid, Tooltip, Minimap } from '@antv/g6';

    export default {
        inject: ['refresh'],
        data () {
            return {
                modelingResult: [],

                my_role: '',

                // model
                form: {
                    model_id: '',
                    algorithm: '',
                    fl_type: '',
                    model_param: '',
                    feature_source: 'api',
                    processor: '',
                    my_role: [],
                },

                api: {
                    feature_data: '',
                    user_id: '',
                },

                code: {
                    user_id: '',
                },

                model_sql_config: {
                    type: '',
                    url: '',
                    username: '',
                    password: '',
                    sql_context: '',
                    user_id: '',
                },

                featureNameFidMapping: {},

                databaseOptions: [
                    { value: 'MySql', label: 'MySql' },
                    { value: 'PgSql', label: 'PgSql' },
                    { value: 'Impala', label: 'Impala' },
                    { value: 'Hive', label: 'Hive' },
                    { value: 'Cassandra', label: 'Cassandra' },
                ],

                apiPredictResult: {
                    data: '',
                    algorithm: '',
                    my_role: '',
                    type: '',
                },

                codePredictResult: {
                    data: '',
                    algorithm: '',
                    my_role: '',
                    type: '',
                },

                sqlPredictResult: {
                    data: '',
                    algorithm: '',
                    my_role: '',
                    type: '',
                },
                loading: false,
            };
        },
        created () {
            this.getData();

            const my_role = localStorage.getItem('my_role');

            this.my_role = my_role;
        },
        methods: {
            async getData () {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url: '/model/detail',
                    params: {
                        id: this.$route.query.id,
                    },
                });

                if (code === 0) {
                    this.form = data;

                    if (data.model_sql_config) {
                        this.model_sql_config = data.model_sql_config;
                    }

                    if (data.model_param && data.model_param.iters) {
                        this.modelingResult = [];
                        for (let i = 0; i < data.model_param.header.length; i++) {
                            const name = data.model_param.header[i];

                            this.modelingResult.push({
                                name,
                                weight: data.model_param.weight[name],
                            });
                        }
                        this.modelingResult.push({
                            name: 'intercept',
                            weight: data.model_param.intercept,
                        });
                    } else if (data.model_param && data.model_param.featureNameFidMapping) {
                        this.iters = data.model_param.iters;

                        this.featureNameFidMapping = data.model_param.featureNameFidMapping;
                    }

                    if (data.algorithm === 'XGBoost') {

                        if (data.xgboost_tree && data.xgboost_tree.length) {
                            this.$nextTick(() => {

                                this.createGraph({
                                    id: 'root',
                                    label: 'XGBoost',
                                    children: data.xgboost_tree,
                                });
                            });
                        }
                    }
                }
                this.loading = false;
            },
            createGraph (data) {
                const canvas = this.$refs['canvas'];
                const grid = new Grid();
                const minimap = new Minimap();
                const tooltip = new Tooltip({
                    getContent (e) {
                        const { data } = e.item.getModel();

                        if (data) {
                            if (data.leaf === true) {
                                return `                                <div>weight: ${data.weight}</div>`;
                            } else if (data.feature) {
                                return `<div>${data.feature} <= ${data.threshold}</div>`;
                            } else {
                                return `<div>${data.sitename}</div>`;
                            }
                        } else {
                            return '';
                        }
                    },
                    itemTypes: ['node'],
                });
                const treeGraph = new TreeGraph({
                    container: 'canvas',
                    width: canvas.offsetWidth,
                    height: 500,
                    modes: {
                        default: [{
                            type: 'collapse-expand',
                            onChange (item, collapsed) {
                                const data = item.get('model');

                                data.collapsed = collapsed;
                                return true;
                            },
                        },
                            'drag-canvas',
                            'zoom-canvas'],
                    },
                    defaultEdge: {
                        type: 'cubic-vertical',
                    },
                    layout: {
                        type: 'dendrogram',
                        direction: 'TB', // H / V / LR / RL / TB / BT
                        nodeSep: 40,
                        rankSep: 100,
                    },
                    plugins: [grid, tooltip, minimap],
                });

                treeGraph.node(node => {
                    let position = 'right';

                    let rotate = 0;

                    if (!node.children) {
                        position = 'bottom';
                        rotate = Math.PI / 2;
                    }

                    return {
                        label: node.id,
                        labelCfg: {
                            position,
                            offset: 5,
                            style: {
                                rotate,
                                textAlign: 'start',
                            },
                        },
                    };
                });

                treeGraph.read(data);
                treeGraph.fitView();
            },
            async saveConfig () {
                const { code } = await this.$http.post({
                    url: '/model/update_sql_config',
                    data: {
                        model_id: this.form.model_id,
                        feature_source: this.form.feature_source,
                        type: this.model_sql_config.type,
                        url: this.model_sql_config.url,
                        username: this.model_sql_config.username,
                        password: this.model_sql_config.password,
                        sql_context: this.model_sql_config.sql_context,
                    },
                });

                if (code === 0) {
                    this.$message('配置成功!');
                    this.refresh();
                }
            },
            async testSql () {
                const { code, data } = await this.$http.post({
                    url: 'predict/debug',
                    data: {
                        model_id: this.form.model_id,
                        user_id: this.model_sql_config.user_id,
                        feature_source: 'sql',
                        params: this.model_sql_config,
                        my_role: this.my_role,
                    },
                });

                if (code === 0) {
                    this.sqlPredictResult = data;
                }
            },
            async testApi () {

                const lines = JSON.parse(this.api.feature_data);

                const { code, data } = await this.$http.post({
                    url: 'predict/debug',
                    data: {
                        model_id: this.form.model_id,
                        user_id: this.api.user_id,
                        feature_source: 'api',
                        feature_data: lines,
                        my_role: this.my_role,
                    },
                });

                if (code === 0) {
                    this.apiPredictResult = data;
                }
            },
            async testCode () {
                const { code, data } = await this.$http.post({
                    url: 'predict/debug',
                    data: {
                        model_id: this.form.model_id,
                        user_id: this.code.user_id,
                        feature_source: 'code',
                        my_role: this.my_role,
                    },
                });

                if (code === 0) {
                    this.codePredictResult = data;
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    #canvas {
        ::v-deep .g6-minimap {
            border: 1px solid #eee;
            margin-left: auto;
        }
    }
    .el-icon-info{
        vertical-align: middle;
        margin-left: 10px;
    }
    .save-btn {
        width: 100px;
    }
</style>
