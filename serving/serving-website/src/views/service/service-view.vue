<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            :model="form"
            :rules="rules"
        >
            <el-form-item
                prop="name"
                label="服务名称:"
                class="maxlength"
            >
                <el-input
                    v-model="form.name"
                    :maxlength="30"
                    :minlength="4"
                    size="medium"
                />
            </el-form-item>

            <el-form-item
                prop="url"
                label="服务地址:"
                class="maxlength"
            >
                <el-input
                    v-model="form.url"
                    :maxlength="100"
                    :minlength="4"
                    size="medium"
                >
                    <template #prepend>
                        /api/
                    </template>
                </el-input>
            </el-form-item>

            <el-form-item
                prop="service_type"
                label="服务类型:"
            >
                <el-select
                    v-model="form.service_type"
                    size="medium"
                    clearable
                    @change="serviceTypeChange"
                >
                    <el-option
                        v-for="item in serviceTypeList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
                    />
                </el-select>

                <div
                    v-if="form.service_type === 4"
                    class="ml10"
                >
                    <el-radio
                        v-model="form.operator"
                        label="sum"
                    >
                        SUM
                    </el-radio>
                    <el-radio
                        v-model="form.operator"
                        label="avg"
                    >
                        AVG
                    </el-radio>
                </div>
            </el-form-item>

            <template v-if="form.service_type">
                <template v-if="form.service_type === 4 || form.service_type === 5 || form.service_type === 6">
                    <el-divider />
                    <p class="mb10">服务配置：</p>
                    <el-form-item
                        v-for="(item, index) in service_config"
                        :key="index"
                        class="service-list"
                    >
                        <p>
                            <strong>服务:</strong> {{ item.name }}
                            <i
                                class="icons el-icon-delete color-danger"
                                @click="service_config.splice(index, 1)"
                            />
                        </p>
                        <p><strong>成员:</strong> {{ item.supplier_name }}</p>
                        <p><strong>URL:</strong> {{ item.base_url }}{{ item.api_name }}</p>
                        <p v-if="item.key_calc_rule"><strong>求交主键:</strong> {{ item.key_calc_rule }}</p>
                        <p v-if="item.params"><strong>Param:</strong></p>
                        <p
                            v-for="each in item.params"
                            :key="each"
                            style="padding-left:50px;"
                        >
                            参数名称: {{ each }}
                        </p>
                    </el-form-item>
                    <el-form-item>
                        <el-button
                            type="primary"
                            @click="addService"
                        >
                            添加服务
                        </el-button>
                    </el-form-item>
                </template>
                <template v-if="form.service_type !== 2 && form.service_type !== 5">
                    <el-divider />
                    <p class="mb10">查询参数配置：</p>
                    <el-form-item
                        v-for="(item, index) in form.paramsArr"
                        :key="`paramsArr-${index}`"
                        :prop="`paramsArr.${index}.value`"
                        :rules="{ required: true, message: '参数名称不能为空', trigger: 'blur' }"
                        label="参数名称:"
                    >
                        <el-input
                            v-model.trim="item.value"
                            style="width: 230px;"
                            clearable
                            @input="paramsValidate(index)"
                        />
                        <i
                            class="icons el-icon-delete color-danger"
                            @click="deleteParams(index, form.paramsArr)"
                        />
                    </el-form-item>
                    <el-form-item>
                        <el-button
                            type="primary"
                            @click="add_params"
                        >
                            新增参数
                        </el-button>
                    </el-form-item>
                </template>

                <template v-if="form.service_type !== 4 && form.service_type !== 5 && form.service_type !== 6">
                    <el-divider />
                    <p class="mb10">SQL 配置：</p>
                    <el-form-item label="数据源:">
                        <el-select
                            v-model="form.data_source.id"
                            clearable
                            @change="dbChange"
                        >
                            <el-option
                                v-for="item in data_sources"
                                :key="item.id"
                                :label="`${item.database_name} (${item.name})`"
                                :value="item.id"
                            />
                        </el-select>
                        <el-button
                            size="mini"
                            @click="addDataResource"
                        >
                            添加数据源
                        </el-button>
                    </el-form-item>
                    <el-form-item label="数据表:">
                        <el-select
                            v-model="form.data_source.table"
                            clearable
                            @change="tableChange"
                        >
                            <el-option
                                v-for="item in data_tables"
                                :key="item"
                                :label="item"
                                :value="item"
                            />
                        </el-select>
                    </el-form-item>

                    <template v-if="form.service_type === 2">
                        <el-form-item
                            label="求交主键:"
                            required
                        >
                            <el-button
                                type="primary"
                                @click="setKeyMap"
                            >
                                设置
                            </el-button>
                            <p v-if="form.stringResult">结果: {{ form.stringResult }}</p>
                        </el-form-item>
                    </template>

                    <el-form-item
                        v-else
                        label="返回字段:"
                    >
                        <el-select
                            v-model="form.data_source.return_fields"
                            :placeholder="form.service_type === 3 ? '' : '支持多选'"
                            :multiple="form.service_type !== 3"
                            value-key="value"
                            clearable
                        >
                            <el-option
                                v-for="item in data_fields"
                                :key="item.name"
                                :label="`${item.name} (${item.type})`"
                                :value="item.name"
                            />
                        </el-select>
                    </el-form-item>

                    <template v-if="form.service_type === 1 || form.service_type === 3">
                        <el-divider />
                        <el-form-item
                            v-for="(item, $index) in form.data_source.condition_fields"
                            :key="`condition_field-${$index}`"
                            class="condition_fields"
                            label="查询字段:"
                        >
                            <el-tag>{{ sqlOperator === 'and' ? 'And' : 'Or' }}</el-tag>
                            <el-select
                                v-model="item.field_on_table"
                                clearable
                            >
                                <el-option
                                    v-for="each in data_fields"
                                    :key="each.name"
                                    :label="`${each.name} (${each.type})`"
                                    :value="each.name"
                                />
                            </el-select>
                            <el-select
                                v-model="item.field_on_param"
                                placeholder="从查询参数配置中选择"
                                clearable
                            >
                                <el-option
                                    v-for="($item, index) in form.paramsArr"
                                    :key="index"
                                    :label="$item.label"
                                    :value="$item.value"
                                />
                            </el-select>
                            <i
                                v-if="form.data_source.condition_fields.length > 1"
                                class="icons el-icon-delete color-danger"
                                @click="deleteParams($index, form.data_source.condition_fields)"
                            />
                        </el-form-item>
                        <el-button
                            class="mb20"
                            type="danger"
                            icon="icons el-icon-circle-plus-outline"
                            @click="addConditionFields"
                        >
                            添加字段
                        </el-button>
                        <el-form-item label="参数逻辑符:">
                            <el-radio
                                v-model="sqlOperator"
                                label="and"
                            >
                                And
                            </el-radio>
                            <el-radio
                                v-model="sqlOperator"
                                label="or"
                            >
                                Or
                            </el-radio>
                        </el-form-item>

                        <div
                            v-if="form.service_type !== 3"
                            class="mt5 mb20"
                        >
                            <el-button
                                size="small"
                                @click="sqlTest"
                            >
                                SQL测试
                            </el-button>
                        </div>
                    </template>
                </template>
            </template>
            <el-button
                class="mt10"
                type="primary"
                size="medium"
                @click="save"
            >
                保存并生成 API
            </el-button>

            <div class="api-preview">
                <el-divider />
                <p class="color-danger mb20 f16">API 预览:</p>
                <el-form-item
                    v-if="api.params"
                    label="查询参数:"
                >
                    {{ api.params }}
                </el-form-item>
                <el-form-item label="请求方式:">
                    <el-tag v-if="api.method">
                        {{ api.method }}
                    </el-tag>
                </el-form-item>
                <el-form-item label="Url:">
                    <el-tag v-if="api.url">
                        {{ api.url }}
                    </el-tag>
                </el-form-item>
                <el-button
                    type="primary"
                    size="medium"
                    :disabled="!api.id"
                    @click="export_sdk"
                >
                    SDK导出
                </el-button>
            </div>
        </el-form>

        <DataSourceEditor
            ref="DataSourceEditor"
            @data-source-add="getDataResources"
        />

        <el-dialog
            :visible.sync="sql_test.visible"
            title="SQL测试"
            width="450px"
        >
            <el-form class="flex-form">
                <p class="mb10">参数输入 :</p>
                <el-form-item
                    v-for="(item, index) in sql_test.params"
                    :key="`params-${index}`"
                    :label="`${item.label}:`"
                    required
                >
                    <el-input v-model="item.value" />
                </el-form-item>
                <p class="mb10">返回字段 :</p>
                <el-form-item
                    v-for="(item, index) in sql_test.return_fields"
                    :key="`return_fields-${index}`"
                    :label="`${item.label}:`"
                    required
                >
                    {{ item.value }}
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="sql_test.visible=false">取消</el-button>
                <el-button
                    type="primary"
                    @click="testConnection"
                >
                    查询
                </el-button>
            </span>
        </el-dialog>

        <el-dialog
            :visible.sync="keyMaps.visible"
            title="设置求交主键:"
            width="500px"
        >
            <p class="mb10">示例: md5(mobile+name) + sha256(cnid)</p>
            <el-form>
                <el-form-item
                    v-for="(row, index) in keyMaps.key_calc_rules"
                    :key="index"
                >
                    <el-select
                        v-model="row.field"
                        placeholder="选择字段"
                        clearable
                        multiple
                        @change="calcKeyMaps"
                    >
                        <el-option
                            v-for="item in data_fields"
                            :key="item.value"
                            :label="item.name"
                            :value="item.name"
                        />
                    </el-select>
                    <el-select
                        v-model="row.operator"
                        placeholder="加密方式"
                        clearable
                        @change="calcKeyMaps"
                    >
                        <el-option
                            v-for="item in keyMaps.encrypts"
                            :key="item"
                            :label="item"
                            :value="item"
                        />
                    </el-select>
                    <i
                        class="icons el-icon-circle-plus-outline"
                        @click="keyMaps.key_calc_rules.push({
                            field: [],
                            operator: ''
                        })"
                    />
                    <i
                        v-if="keyMaps.key_calc_rules.length > 1"
                        class="icons el-icon-delete color-danger"
                        @click="deleteKeyMaps(index)"
                    />
                </el-form-item>
                <p v-if="keyMaps.stringResult">结果: {{ keyMaps.stringResult }}</p>
            </el-form>
            <template #footer>
                <el-button @click="cancelKeyMaps">
                    取消
                </el-button>
                <el-button
                    type="primary"
                    @click="calcKeyMaps($event, { action: 'confirm' })"
                >
                    确定
                </el-button>
            </template>
        </el-dialog>

        <ServiceConfigs
            ref="serviceConfigs"
            :service-type="`${form.service_type}`"
            @confirm-checked-rows="addServiceRow"
        />
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import ServiceConfigs from './service_config';
    import DataSourceEditor from '../data_source/data-source-edit';

    export default {
        components: {
            ServiceConfigs,
            DataSourceEditor,
        },
        data() {
            return {
                loading: false,
                form:    {
                    name:         '',
                    url:          '',
                    service_type: '',
                    operator:     'sum',
                    data_source:  {
                        id:               '',
                        table:            '',
                        return_fields:    [],
                        condition_fields: [
                            {
                                field_on_param: '',
                                field_on_table: '',
                            },
                        ],
                    },
                    paramsArr: [{
                        label: '',
                        value: '',
                    }],
                    key_calc_rules: [],
                    stringResult:   '',
                },
                keyMaps: {
                    visible:        false,
                    encrypts:       ['md5', 'sha256'],
                    key_calc_rules: [],
                    stringResult:   '',
                },
                api: {
                    id:     '',
                    params: '',
                    method: '',
                    url:    '',
                },
                rules: {
                    name:         [{ required: true, message: '服务名称必填!' }],
                    url:          [{ required: true, message: '服务地址必填!' }],
                    service_type: [{ required: true, message: '服务类型必选!' }],
                },
                serviceId:       '',
                serviceTypeList: [
                    {
                        name:  '两方匿踪查询',
                        value: 1,
                    },
                    {
                        name:  '两方交集查询',
                        value: 2,
                    },
                    {
                        name:  '多方安全统计(被查询方)',
                        value: 3,
                    },
                    {
                        name:  '多方安全统计(查询方)',
                        value: 4,
                    },
                    {
                        name:  '多方交集查询',
                        value: 5,
                    },
                    {
                        name:  '多方匿踪查询',
                        value: 6,
                    },
                ],
                data_sources:   [],
                data_tables:    [],
                data_fields:    [],
                service_config: [],
                sql_test:       {
                    visible:       false,
                    params:        [],
                    params_json:   {},
                    return_fields: [],
                },
                sqlOperator: 'and',
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.serviceId = this.$route.query.id;

            this.getDataResources();

            if (this.serviceId) {
                this.getSqlConfigDetail();
            }
        },
        methods: {
            async getSqlConfigDetail() {
                const { code, data } = await this.$http.post({
                    url:  '/service/detail',
                    data: { id: this.serviceId },
                });

                if (code === 0) {
                    if (data) {
                        const {
                            service_type: type,
                            query_params: params,
                            service_config,
                            data_source,
                            preview,
                        } = data;

                        this.form.name = data.name;
                        this.form.url = data.url;
                        this.form.service_type = type;

                        if(params) {
                            this.form.paramsArr = params.map(x => {
                                return {
                                    label: x,
                                    value: x,
                                };
                            });
                        }

                        if(data_source) {
                            this.form.data_source.id = data.data_source.id;
                            this.form.data_source.table = data.data_source.table;
                            this.getDataTable();
                            this.getTablesFields();

                            if(type === 2) {
                                const rules = data_source.key_calc_rules;

                                if(rules) {
                                    this.form.key_calc_rules = rules.map(x => {
                                        return {
                                            ...x,
                                            field: x.field.split(','),
                                        };
                                    });
                                    rules.forEach((x, i) => {
                                        this.form.stringResult += `${i > 0 ? ' + ' : ''}${x.operator}(${x.field.split(',').join('+')})`;
                                    });
                                }
                            } else if(type === 1 || type === 3){
                                if (type === 1) {
                                    this.form.data_source.return_fields = data_source.return_fields.map(x => x.name);
                                } else {
                                    this.form.data_source.return_fields = data_source.return_fields[0].name;
                                }
                                this.form.data_source.condition_fields = data_source.condition_fields;
                            }
                        }

                        if(service_config) {
                            this.service_config = service_config.map(x => {
                                return {
                                    ...x,
                                    supplier_id:   x.member_id,
                                    supplier_name: x.member_name,
                                    params:        x.params ? x.params.split(',') : [],
                                    base_url:      x.url,
                                };
                            });
                        }

                        this.api = preview || {};
                    }
                }
            },
            serviceTypeChange() {
                this.form.data_source.table = '';
                this.form.data_source.return_fields = [];
                if(this.form.service_type <= 3) {
                    this.getDataResources();
                }
            },
            add_params(){
                this.form.paramsArr.push({
                    label: '',
                    value: '',
                });
            },
            paramsValidate(index) {
                const { value } = this.form.paramsArr[index];

                if(!value) return;
                for(const i in this.form.paramsArr) {
                    const item = this.form.paramsArr[i];

                    if(+i !== index && value === item.value) {
                        this.$message.error('参数名不能重复!');
                        break;
                    }
                }
            },
            addDataResource() {
                this.$refs['DataSourceEditor'].show();
            },
            async getDataResources(){
                const { code, data } = await this.$http.post({
                    url: '/data_source/query',
                });

                if (code === 0) {
                    this.data_sources = data.list;
                }
            },
            dbChange() {
                this.data_tables = [];
                this.form.data_source.table = '';
                this.data_fields = [];
                this.form.data_source.return_fields = [];
                this.getDataTable();
            },
            async getDataTable(){
                const { code, data } = await this.$http.post({
                    url:  '/data_source/query_tables',
                    data: {
                        id: this.form.data_source.id,
                    },
                });

                if (code === 0) {
                    this.data_tables = data.tables;
                }
            },
            tableChange() {
                this.getTablesFields();
            },
            async getTablesFields(){
                const { code, data } = await this.$http.post({
                    url:  '/data_source/query_table_fields',
                    data: { id: this.form.data_source.id, table_name: this.form.data_source.table },
                });

                if (code === 0) {
                    this.data_fields = data.fields;
                }
            },
            deleteParams(index, array){
                array.splice(index, 1);
            },
            addConditionFields() {
                this.form.data_source.condition_fields.push({
                    field_on_param: '',
                    field_on_table: '',
                });
            },
            addService() {
                const checkedIds = this.service_config.map(x => {
                    return x.id;
                });

                this.$refs['serviceConfigs'].show(checkedIds);
            },
            addServiceRow(rows) {
                if(rows.length) {
                    this.service_config.push(...rows);
                }
            },
            sqlTest() {
                for(const i in this.form.paramsArr) {
                    const x = this.form.paramsArr[i];

                    if(!x.value) {
                        return this.$message.error('缺少查询参数!');
                    }
                }

                const { data_source: obj } = this.form;

                this.sql_test.params = [];
                for(const i in obj.condition_fields) {
                    const item = obj.condition_fields[i];

                    if(!item.field_on_param || !item.field_on_table) {
                        return this.$message.error('请将查询字段填写完整!');
                    } else {
                        this.sql_test.params.push({
                            label: item.field_on_param,
                            value: '',
                        });
                    }
                }

                this.sql_test.visible = true;

                this.sql_test.return_fields = this.form.data_source.return_fields.map(x => {
                    return {
                        label: x,
                        value: '',
                    };
                });
            },
            async testConnection(event) {
                const paramsJson = {};
                const {
                    service_type: type,
                    data_source: obj,
                } = this.form;
                const { params } = this.sql_test;

                for(let i = 0; i < params.length; i++) {
                    paramsJson[params[i].label] = params[i].value;
                }

                const $params = {
                    data_source: {
                        id:    obj.id,
                        table: obj.table,
                    },
                };

                if(type === 1 || type === 3) {
                    $params.params = paramsJson;
                    $params.data_source.return_fields = obj.return_fields.map(x => {
                        const item = this.data_fields.find(y => y.name === x);

                        return item;
                    });
                    $params.data_source.condition_fields = obj.condition_fields.map(x => {
                        x.operator = this.sqlOperator;
                        return x;
                    });
                } else if (type === 2) {
                    $params.key_calc_rules = this.form.key_calc_rules.map(x => {
                        return {
                            ...x,
                            field: x.field.join(','),
                        };
                    });
                }

                const { code, data } = await this.$http.post({
                    url:      '/service/sql_test',
                    timeout:  1000 * 60 * 24 * 30,
                    data:     $params,
                    btnState: {
                        target: event,
                    },
                });

                if (code === 0 && data) {
                    this.sql_test.return_fields.forEach(x => {
                        x.value = data.result[x.label] || '';
                    });
                    this.$message.success('测试成功!');
                }
            },
            setKeyMap() {
                const array = this.form.key_calc_rules;

                if(array.length === 0) {
                    this.keyMaps.key_calc_rules.push({
                        operator: '',
                        field:    [],
                    });
                } else {
                    this.keyMaps.key_calc_rules = [...array];
                    this.keyMaps.stringResult = '';
                    array.forEach((x, i) => {
                        this.keyMaps.stringResult += `${i > 0 ? ' + ' : ''}${x.operator}(${x.field.join('+')})`;
                    });
                }

                this.keyMaps.visible = true;
            },
            deleteKeyMaps(index) {
                this.keyMaps.key_calc_rules.splice(index, 1);
            },
            cancelKeyMaps() {
                this.keyMaps.key_calc_rules = [];
                this.keyMaps.stringResult = '';
                this.keyMaps.visible = false;
            },
            calcKeyMaps(event, opt = { action: '' }) {
                const array = this.keyMaps.key_calc_rules;

                this.keyMaps.stringResult = '';
                for(const i in array) {
                    const x = array[i];

                    if(!x.field || !x.operator) {
                        opt.action === 'confirm' && this.$message.error('主键设置不能为空!');
                        return false;
                    } else {
                        this.keyMaps.stringResult += `${i > 0 ? ' + ' : ''}${x.operator}(${x.field.join('+')})`;
                    }
                }
                if(opt.action === 'confirm') {
                    this.form.stringResult = this.keyMaps.stringResult;
                    this.form.key_calc_rules = [...array];
                    this.keyMaps.visible = false;
                }

                return true;
            },
            async save(event) {
                if (!this.form.name || !this.form.url || !this.form.service_type) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                }

                const { data_source: obj, operator } = this.form;
                const type = this.form.service_type;
                const $params = {
                    name:         this.form.name,
                    url:          this.form.url,
                    service_type: type,
                };

                if(this.serviceId) {
                    $params.id = this.serviceId;
                }

                if (type === 2) {
                    if(!this.calcKeyMaps()) return;
                    $params.data_source = {
                        id:             obj.id,
                        table:          obj.table,
                        key_calc_rules: this.form.key_calc_rules.map(x => {
                            return {
                                ...x,
                                field: x.field.join(','),
                            };
                        }),
                        key_calc_rule: this.form.stringResult,
                    };
                } else {
                    if(type !== 5) {
                        const params = [];

                        for(const i in this.form.paramsArr){
                            const x = this.form.paramsArr[i];

                            if(!x.value) {
                                return this.$message.error('请将查询字段填写完整!');
                            } else {
                                params.push(x.value);
                            }
                        }

                        $params.query_params = params;
                    }

                    if(type === 4 || type === 5 || type === 6) {
                        $params.service_config = this.service_config.map(x => {
                            return {
                                id:          x.id,
                                name:        x.name,
                                member_id:   x.supplier_id,
                                member_name: x.supplier_name,
                                url:         x.base_url + x.api_name,
                                base_url:	   x.base_url,
                                api_name:	   x.api_name,
                                params:      x.params.join(','),
                            };
                        });
                        $params.operator = operator;
                    } else {
                        // 1 || 3
                        let return_fields = [];

                        if(type === 1) {
                            this.form.data_source.return_fields.forEach(x => {
                                const item = this.data_fields.find(y => y.name === x);

                                if(item) {
                                    return_fields.push(item);
                                }
                            });
                        } else {
                            return_fields = [this.form.data_source.return_fields];
                        }

                        for(const i in obj.condition_fields) {
                            const item = obj.condition_fields[i];

                            if(!item.field_on_param || !item.field_on_table) {
                                return this.$message.error('请将查询字段填写完整!');
                            }
                        }

                        $params.data_source = {
                            id:               obj.id,
                            table:            obj.table,
                            condition_fields: obj.condition_fields.map(x => {
                                x.operator = this.sqlOperator;
                                return x;
                            }),
                            return_fields,
                        };
                    }
                }

                const { code, data } = await this.$http.post({
                    url:      this.serviceId ? '/service/update' : '/service/add',
                    timeout:  1000 * 60 * 24 * 30,
                    data:     $params,
                    btnState: {
                        target: event,
                    },
                });

                if (code === 0) {
                    if(data) {
                        this.api = data;
                        this.serviceId = data.id;
                    }
                    this.$message.success('操作成功!');
                }
            },
            async export_sdk(){
                const api = `${window.api.baseUrl}/service/export_sdk?serviceId=${this.api.id}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = api;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
            },
        },
    };
</script>

<style lang="scss" scoped>
    .maxlength{max-width: 400px;}
    .icons{cursor: pointer;margin-left:5px;}
    .condition_fields{margin-bottom: 10px;
        .el-select, .el-input{margin-bottom: 10px;}
    }
    .el-select{
        ::v-deep .el-tag__close {
            background:#fff;
        }
    }
    .flex-form{
        .el-form-item{display:flex;}
    }
    .service-list{
        border:1px solid #ccc;
        border-radius: 4px;
        padding:10px 20px;
    }
    .el-select-dropdown__item{
        padding-right: 30px;
        &:after{right:15px !important;}
    }
    .api-preview{
        .el-form-item{margin-bottom:5px;}
    }
</style>
