<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            :model="form"
            :rules="rules"
            class="form-box"
        >
            <p class="mb10">基本信息：</p>
            <div style="display: flex; margin-bottom: -10px;">
                <el-form-item
                    prop="service_type"
                    label="服务类型:"
                    style="min-width: 280px;"
                >
                    <el-select
                        v-model="form.service_type"
                        size="medium"
                        clearable
                        @change="serviceTypeChange"
                        :disabled="serviceId !== undefined"
                    >
                        <el-option
                            v-for="item in serviceTypeList"
                            :key="item.value"
                            :value="item.value"
                            :label="item.name"
                        />
                    </el-select>
                </el-form-item>
                <div
                    class="ml10"
                    style="font-size: 13px; color: #666; line-height: 20px;"
                >
                    <i
                        class="el-icon-info"
                        style="margin-right: 4px"
                    />
                    <span>{{ currentDesc }}</span>
                </div>
            </div>

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

            <template v-if="form.service_type">
                <template v-if="form.service_type === 4 || form.service_type === 5 || form.service_type === 6">
                    <el-divider/>
                    <p class="mb10">配置联邦服务：</p>
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
                        <p v-if="item.params && item.params.length"><strong>Param:</strong></p>
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
                            class="dashed-btn"
                        >
                            + 添加联邦服务
                        </el-button>
                        <div
                            v-if="form.service_type === 4 && service_config.length > 0"
                            style="margin-top: 10px"
                        >
                            <label style="color: #6C757D;">
                                <span>服务算子:</span>
                            </label>
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
                </template>
                <template
                    v-if="form.service_type !== 2 && form.service_type !== 5 && form.service_type !== 7 && form.service_type !== 8">
                    <el-divider/>
                    <p class="mb10">查询参数配置：</p>
                    <el-button
                        v-if="form.paramsArr.length === 0"
                        class="icons el-icon-circle-plus-outline"
                        @click="add_params"
                    >
                    </el-button>
                    <el-form-item
                        v-for="(item, index) in form.paramsArr"
                        :key="`paramsArr-${index}`"
                        :prop="`paramsArr.${index}.value`"
                        :rules="{ required: true, message: '参数名称不能为空', trigger: 'blur' }"
                    >
                        <label style="color: #6C757D;">
                            <span>参数名称：</span>
                            <el-input
                                v-model.trim="item.value"
                                style="width: 230px;"
                                clearable
                                @input="paramsValidate(index)"
                            />
                        </label>
                        <label style="margin-left: 10px; color: #6C757D;">
                            <span>参数描述：</span>
                            <el-input
                                v-model="item.desc"
                                style="width: 230px;"
                                clearable
                            />
                        </label>
                        <i
                            class="icons el-icon-delete color-danger"
                            @click="deleteParams(index, form.paramsArr)"
                        />
                        <el-button
                            v-if="index + 1 === form.paramsArr.length"
                            class="icons el-icon-circle-plus-outline"
                            @click="add_params"
                        >
                        </el-button>
                    </el-form-item>
                    <!--                    <el-form-item>-->
                    <!--                        <el-button-->
                    <!--                            type="primary"-->
                    <!--                            @click="add_params"-->
                    <!--                            class="dashed-btn"-->
                    <!--                        >-->
                    <!--                            + 新增-->
                    <!--                        </el-button>-->
                    <!--                    </el-form-item>-->
                </template>

                <template
                    v-if="form.service_type !== 4 && form.service_type !== 5 && form.service_type !== 6 && form.service_type !== 7 && form.service_type !== 8">
                    <el-divider/>
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
                            :placeholder="form.service_type !== 1 ? '单选' : '支持多选'"
                            :multiple="form.service_type === 1"
                            value-key="value"
                            clearable
                            @change="sqlShow"
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
                        <el-form-item
                            v-for="(item, $index) in form.data_source.condition_fields"
                            :key="`condition_field-${$index}`"
                            class="condition_fields"
                            label="查询条件:"
                        >
                            <el-select
                                v-model="sqlOperator"
                                class="ml10 no-arrow"
                                style="width:40px;"
                                @change="sqlShow"
                            >
                                <el-option
                                    label="AND"
                                    value="and"
                                />
                                <el-option
                                    label="OR"
                                    value="or"
                                />
                            </el-select>
                            <el-select
                                v-model="item.field_on_table"
                                class="ml10"
                                clearable
                                @change="sqlShow"
                            >
                                <el-option
                                    v-for="each in data_fields"
                                    :key="each.name"
                                    :label="`${each.name} (${each.type})`"
                                    :value="each.name"
                                />
                            </el-select>

                            <el-select
                                v-model="item.condition"
                                class="ml10 no-arrow"
                                style="width:40px;"
                                @change="sqlShow"
                            >
                                <el-option
                                    label="="
                                    value="="
                                />
                                <el-option
                                    label=">"
                                    value="gt"
                                />
                                <el-option
                                    label="<"
                                    value="lt"
                                />
                            </el-select>

                            <el-select
                                v-model="item.field_on_param"
                                placeholder="从查询参数配置中选择"
                                class="ml10"
                                clearable
                                @change="sqlShow"
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
                            <el-button
                                v-if="$index + 1 === form.data_source.condition_fields.length"
                                class="icons el-icon-circle-plus-outline"
                                @click="addConditionFields"
                            >
                            </el-button>
                        </el-form-item>
                        <div
                            v-if="form.service_type !== 3"
                            class="mt5 mb20"
                        >
                            <el-button
                                size="mt10"
                                @click="sqlTest"
                            >
                                在线测试
                            </el-button>
                            <span style="font-size:12px;padding-left: 5px">{{ show_sql_result }}</span>
                        </div>
                        <el-divider/>
                    </template>
                </template>
                <template v-if="form.service_type === 7 || form.service_type === 8">

                    <el-form-item v-if="!form.model_data.model_id"
                                  label="选择文件："
                                  required
                    >
                        <uploader
                            ref="uploaderRef"
                            :options="file_upload_options"
                            :list="file_upload_options.files"
                            :file-status-text="fileStatusText"
                            @file-complete="fileUploadComplete"
                            @file-removed="fileRemoved"
                            @file-added="fileAdded"
                        >
                            <uploader-unsupport/>
                            <uploader-drop v-if="file_upload_options.files.length === 0">
                                <p class="mb10">将文件（.txt/.zip）拖到此处</p>或
                                <uploader-btn
                                    :attrs="{accept: ['.txt','.zip']}"
                                    :single="true"
                                >
                                    点击上传
                                </uploader-btn>
                            </uploader-drop>
                            <uploader-list :file-list="file_upload_options.files.length"/>
                        </uploader>
                    </el-form-item>

                    <el-divider/>
                    <p class="mb10" v-if="form.model_data.model_id">模型概览：</p>
                    <el-form-item
                        class="service-list"
                        v-if="form.model_data.model_id"
                    >
                        <p><strong>Id: </strong> {{ form.model_data.model_id }}</p>
                        <p><strong>算法: </strong> {{ form.model_data.model_algorithm }}</p>
                        <p><strong>训练类型: </strong> {{ form.model_data.model_fl_type }}</p>
                        <p><strong>我的角色: </strong>
                            <el-tag v-for="each in form.model_data.model_roles"
                                    :key="each"
                            >{{ each }}
                            </el-tag>
                        </p>
                        <p><strong>模型结构: </strong>
                            <el-button size="mini"
                                       round
                                       @click="show_model_overview"
                            >
                                展示
                            </el-button>
                        </p>
                    </el-form-item>

                    <p class="mb10" v-if="modelStatusVisible && form.model_data.model_id">合作方模型状态：
                        <el-button
                            size="medium"
                            icon="el-icon-refresh"
                            type="text"
                            :loading="checkLoading"
                            @click="refreshPartnerStatus"></el-button>
                    </p>
                    <el-form-item v-if="modelStatusVisible && form.model_data.model_id"
                                  class="service-list"
                                  style="width: 60%"
                    >
                        <el-table
                            :loading="partnerTableLoading"
                            :data="partnerData"
                            style="width: 100%">
                            <el-table-column
                                :min-width="150"
                                label="合作者ID"
                                prop="member_id">
                            </el-table-column>
                            <el-table-column
                                :min-width="60"
                                label="合作者名称"
                                prop="member_name">
                            </el-table-column>
                            <el-table-column
                                label="URL"
                                :min-width="100"
                            >
                                <template slot-scope="scope">
                                    {{ scope.row.url }}
                                    <el-popover
                                        v-if="scope.row.status === 'online'"
                                        placement="top-start"
                                        width="100"
                                        trigger="hover"
                                        content="合作者已联通">
                                        <el-button slot="reference"
                                                   type="text"
                                                   icon="el-icon-check"></el-button>
                                    </el-popover>
                                    <el-popover
                                        v-if="scope.row.status === 'offline'"
                                        placement="top-start"
                                        title="⚠️警告"
                                        width="200"
                                        trigger="hover"
                                        content="该合作者模型失联">
                                        <el-button slot="reference" type="text" icon="el-icon-warning"></el-button>
                                    </el-popover>


                                </template>

                            </el-table-column>
                            <el-table-column
                                label="操作"
                                align="right">
                                <template slot-scope="scope">
                                    <el-button
                                        size="mini"
                                        icon="el-icon-refresh"
                                        type="text"
                                        :loading="checkLoading"
                                        @click="refreshPartnerStatus( scope.row.member_id)"></el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>


                    <el-dialog title="模型结构"
                               :visible.sync="model_show_flag"
                               :destroy-on-close="true"
                               :width="'70%'"
                    >
                        <div
                            v-if="form.model_data.model_algorithm === 'XGBoost'"
                            id="canvas"
                            ref="canvas"
                            class="mb20"
                            style="background: #f9f9f9;"
                        />
                    </el-dialog>

                    <p class="mb10" v-if="form.model_data.model_id">特征配置：</p>

                    <el-form-item v-if="form.model_data.model_id">
                        <el-tabs type="border-card" @tab-click="handleTabClick" v-model="activeName">
                            <el-tab-pane label="代码配置" name="api">
                                <el-row :span="24">
                                    <el-col :span="2">
                                        <p class="mb10"><strong>处理器：</strong></p>
                                    </el-col>
                                    <el-col :span="6" style="margin-right: 10px;">
                                        <el-input :disabled="true" v-model="form.processor"></el-input>
                                        <p></p>
                                    </el-col>
                                </el-row>
                            </el-tab-pane>

                            <el-tab-pane label="SQL配置" name="sql">
                                <el-form-item
                                    label="数据源："
                                    :rules="[{required: true, message: '数据源必填!'}]"
                                >
                                    <el-select
                                        v-model="form.model_data.model_sql_config.data_source_id"
                                        placeholder="请选择数据源"
                                        clearable
                                    >
                                        <el-option
                                            v-for="(item) in dataBaseOptions"
                                            :key="item.value"
                                            :value="item.value"
                                            :label="item.label"
                                        />
                                    </el-select>
                                </el-form-item>

                                <el-form-item
                                    label="SQL："
                                    :rules="[{required: true, message: 'SQL必填!'}]"
                                >
                                    <el-input
                                        v-model="form.model_data.model_sql_config.sql_script"
                                        type="textarea"
                                        placeholder="如：select x0,x1,x2 form table where user_id = ?"
                                        clearable
                                        rows="4"
                                    />
                                </el-form-item>

                                <el-form-item label="主键字段："
                                              :rules="[{required: true, message: '主键字段必填!'}]"
                                >
                                    <el-input
                                        v-model="form.model_data.model_sql_config.sql_condition_field"
                                        type="text"
                                        placeholder="例如：id"
                                        class="user-tips"
                                        clearable
                                    />

                                </el-form-item>
                            </el-tab-pane>
                        </el-tabs>
                    </el-form-item>

                    <el-card class="model-test-result-card" v-if="form.model_data.model_id">
                        <div style="margin-bottom: 10px;">样本ID:</div>
                        <div>
                            <el-input v-model="form.model_data.check_data.sample_id" type="text"
                                      class="checkButton"></el-input>
                            <el-button type="primary"
                                       :disabled="form.model_data.check_data.sample_id === ''"
                                       @click="testModel"
                                       style="margin-right: 10px;"
                            >
                                可用性校验
                            </el-button>
                            <el-tooltip>
                                <div slot="content">输入样本ID后才可进行预测</div>
                                <i class="el-icon-info"/>
                            </el-tooltip>
                        </div>

                        <div v-if="predictResult !== ''" style="margin-top: 20px; margin-bottom: 10px;">结果：</div>
                        <div>
                            <p>
                                {{ predictResult.length > 150 ? predictResult.substring(0, 151) + '...' : predictResult }}</p>
                        </div>
                        <el-row>
                            <el-button
                                v-if="predictResult.length > 150"
                                type="text"
                                @click="showRequest(predictResult)"
                            >
                                查看更多
                            </el-button>
                        </el-row>

                        <el-dialog
                            :title="title"
                            :visible.sync="requestDataDialog"
                        >
                            <JsonViewer
                                :value="jsonData"
                                :expand-depth="5"
                                copyable
                            />
                        </el-dialog>

                    </el-card>


                </template>
            </template>
            <el-button
                class="mt10"
                type="primary"
                size="medium"
                @click="save"
                :disabled="!form.service_type"
            >
                保存
            </el-button>
            <el-link
                type="primary"
                :disabled="!api.id"
                @click="export_sdk"
                style="margin-left: 10px"
            >
                点击下载工具包
            </el-link>
            <div class="api-preview">
                <el-divider/>
                <p class="mb20 f16">API 预览:</p>
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
                    <el-input v-model="item.value"/>
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
import {mapGetters} from 'vuex';
import ServiceConfigs from './service_config';
import DataSourceEditor from '../data_source/data-source-edit';
import {Grid, Minimap, Tooltip, TreeGraph} from "@antv/g6";

export default {
    components: {
        ServiceConfigs,
        DataSourceEditor,
    },
    data() {
        return {
            dataBaseOptions: [],
            predictResult: '',
            requestDataDialog: false,
            jsonData: '',
            title: '',
            sqlPredictResult: {
                data: '',
                algorithm: '',
                my_role: '',
                type: '',
            },
            loading: false,
            service_overview: {},
            fileStatusText: {
                success: '成功',
                error: '错误',
                uploading: '上传中',
                paused: '已暂停',
                waiting: '等待中',
            },
            form: {
                name: '',
                filename: '',
                url: '',
                service_type: '',
                operator: 'sum',
                data_source: {
                    id: '',
                    table: '',
                    return_fields: [],
                    condition_fields: [
                        {
                            condition: '=',
                            field_on_param: '',
                            field_on_table: '',
                        },
                    ],
                },
                paramsArr: [{
                    label: '',
                    value: '',
                    desc: '',
                }],
                key_calc_rules: [],
                stringResult: '',
                model_data: {
                    check_data: {
                        sample_id: ''
                    },
                    model_id: '',
                    model_param: '',
                    model_sql_config: {
                        feature_source: '',
                        data_source_id: '',
                        sql_script: '',
                        sql_condition_field: '',
                    },
                    model_member_status: [],
                    model_overview: '',
                    // 可能自己跟自己建模
                    model_roles: [],
                    model_algorithm: '',
                    model_fl_type: '',
                },
                processor: '',
            },
            partnerData: [],
            partnerTableLoading: false,
            file_upload_options: {
                files: [],
                target: window.api.baseUrl + '/file/upload',
                singleFile: true,
                // chunks check
                testChunks: true,
                chunkSize: 8 * 1024 * 1024,
                simultaneousUploads: 4,
                headers: {
                    token: '',
                },
                query: {
                    fileType: 'MachineLearningModelFile',
                },
                parseTimeRemaining(timeRemaining, parsedTimeRemaining) {
                    return parsedTimeRemaining
                        .replace(/\syears?/, '年')
                        .replace(/\days?/, '天')
                        .replace(/\shours?/, '小时')
                        .replace(/\sminutes?/, '分钟')
                        .replace(/\sseconds?/, '秒');
                },
            },
            keyMaps: {
                visible: false,
                encrypts: ['md5', 'sha256'],
                key_calc_rules: [],
                stringResult: '',
            },
            api: {
                id: '',
                params: '',
                method: '',
                url: '',
            },
            rules: {
                name: [{required: true, message: '服务名称必填!'}],
                url: [{required: true, message: '服务地址必填!'}],
                service_type: [{required: true, message: '服务类型必选!'}],
            },
            serviceId: '',
            serviceType:'',
            serviceTypeList: [
                {
                    name: '两方匿踪查询',
                    value: 1,
                },
                {
                    name: '两方交集查询',
                    value: 2,
                },
                {
                    name: '多方安全统计(被查询方)',
                    value: 3,
                },
                {
                    name: '多方安全统计(查询方)',
                    value: 4,
                },
                {
                    name: '多方交集查询',
                    value: 5,
                },
                {
                    name: '多方匿踪查询',
                    value: 6,
                },
                {
                    name: '机器学习模型服务',
                    value: 7,
                },
                {
                    name: '深度学习模型服务',
                    value: 8,
                }
            ],
            data_sources: [],
            data_tables: [],
            data_fields: [],
            service_config: [],
            sql_test: {
                visible: false,
                params: [],
                params_json: {},
                return_fields: [],
            },
            sqlOperator: 'and',
            show_sql_result: '',
            currentDesc: '',
            model_show_flag: false,
            graphData: {},
            checkLoading: false,
            activeName: 'api',
            modelStatusVisible: false,
        };
    },
    computed: {
        ...mapGetters(['userInfo']),
    },
    watch: {
        'form.service_type'() {
            this.setServiceDesc();
        },
    },
    created() {
        this.serviceId = this.$route.query.id;
        this.serviceType = this.$route.query.service_type;
        this.getDataSource();
        this.getDataResources();

        if (this.serviceId) {
            this.getSqlConfigDetail();
        }
    },
    methods: {
        handleTabClick(tab, event) {
            if (tab.name === 'sql') {
                this.activeName = 'sql'
            } else {
                this.activeName = 'api'
            }

        },
        showRequest(data) {
            this.requestDataDialog = true;
            this.title = '请求体';
            setTimeout(() => {
                this.jsonData = JSON.parse(data);
            });
        },

        async testModel() {
            // if (this.activeName === 'sql') {
            const {code, data} = await this.$http.post({
                url: 'predict/debug',
                data: {
                    model_id: this.form.model_data.model_id,
                    user_id: this.form.model_data.check_data.sample_id,
                    feature_source: this.activeName,
                    params: this.form.model_data.model_sql_config,
                    my_role: this.form.model_data.model_roles,
                    feature_data: this.form
                },
            });

            if (code === 0) {
                this.sqlPredictResult = data;
                this.predictResult = data;
            }
            // }

        },

        async getDataSource() {
            const {code, data} = await this.$http.get({
                url: '/data_source/query',
                params: {
                    id: '',
                    name: '',
                    page_index: '',
                    page_size: ''
                },
            });

            if (code === 0) {
                const data_list = data.list
                for (let i = 0; i < data_list.length; i++) {
                    this.dataBaseOptions.push({
                        label: data_list[i].name,
                        value: data_list[i].id
                    })
                }
            }
        },

        async refreshPartnerStatus(partner_id) {
            this.checkLoading = true
            const {code, data} = await this.$http.get({
                url: '/model/status/check',
                // timeout: 1000 * 60 * 2,
                params: {
                    member_id: partner_id,
                    model_id: this.form.model_data.model_id,
                },
            });
            if (code === 0) {
                this.partnerData = data
            }
            this.checkLoading = false
        },


        show_model_overview() {
            this.model_show_flag = true
            // let that = this;
            setTimeout(() => {
                this.createGraph(this.graphData);
            }, 200)

        },

        createGraph(data) {
            const canvas = this.$refs['canvas'];
            const grid = new Grid();
            const minimap = new Minimap();
            const tooltip = new Tooltip({
                getContent(e) {
                    const {data} = e.item.getModel();

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
                height: 420,
                modes: {
                    default: [{
                        type: 'collapse-expand',
                        onChange(item, collapsed) {
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

            // treeGraph.clear();
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

        fileAdded(file) {
            this.file_upload_options.files = [file];
        },
        fileRemoved() {
            this.file_upload_options.files = [];
        },
        async fileUploadComplete(e) {
            this.loading = true;

            const {code, data} = await this.$http.get({
                url: '/file/merge',
                timeout: 1000 * 60 * 2,
                params: {
                    filename: e.file.name,
                    uniqueIdentifier: e.uniqueIdentifier,
                    fileType: this.form.service_type === 8 ? 'MachineLearningModelFile' : 'DeepLearningModelFile',
                },
            });

            this.loading = false;
            if (code === 0) {
                this.form.filename = data.filename;
            } else {
                this.fileRemoved();
                this.$refs.uploaderRef.uploader.cancel();
            }
        },
        setServiceDesc() {
            const descList = ['两方匿踪查询是指查询方隐藏被查询对象关键词或客户ID信息，数据服务方提供匹配的查询结果却无法获知具体对应哪个查询对象。数据不出门且能计算，杜绝数据缓存、数据泄漏、数据贩卖的可能性。匿踪查询协议基于对称加密、不经意传输等密码学技术，数据服务方保持数据资源控制权，数据请求方不再使用明文查询，查询入参增加随机密钥比明文哈希后撞库查询安全性大大提高，确保仅仅得到匹配的查询结果却不留查询痕迹',
                '两方交集查询是指持有数据的两方能够计算得到双方数据集合的交集部分，而不暴露交集以外的任何数据集合信息。比如黑名单的查询：当A金融机构有一份完整的黑名单用户，而这个用户准备要去B银行借款，银行希望知道这位新客户是否在A机构有过不良记录。通过隐私保护集合求交技术，B银行发现这位用户在A机构并无不良记录，而且A机构并不知道银行前来查询的这位有着借钱需求的用户是谁。',
                '多方安全统计(被查询方)是协调方只能拿到最终的统计结果，但不能获取到特定参与方的统计结果。例如A公司想统计某个用户的信用卡数量，已知B，C两家公司有信用卡数据，但是为了安全起见，A只能获取信用卡总和而不能知道B，C两家公司的信用卡具体数据（用户在B，C各有几张信用卡），此时便可以使用多方安全统计算法，BC为被查询方，A为查询方',
                '多方安全统计(查询方)是协调方只能拿到最终的统计结果，但不能获取到特定参与方的统计结果。例如A公司想统计某个用户的信用卡数量，已知B，C两家公司有信用卡数据，但是为了安全起见，A只能获取信用卡总和而不能知道B，C两家公司的信用卡具体数据（用户在B，C各有几张信用卡），此时便可以使用多方安全统计算法，BC为被查询方，A为查询方',
                '多方交集查询是两方交集查询的再次封装',
                '多方匿踪查询是两方交集查询的再次封装',
                '深度学习模型--描述',
                '机器学习模型--描述'];
            this.currentDesc = descList[this.form.service_type - 1];
        },
        async getSqlConfigDetail() {
            const {code, data} = await this.$http.post({
                url: '/service/detail',
                data: {id: this.serviceId, service_type:this.serviceType},
            });

            if (code === 0) {
                if (data) {
                    const {
                        service_type: type,
                        service_config,
                        data_source,
                        preview,
                    } = data;
                    const params = data.query_params_config || data.query_params;

                    this.form.name = data.name;
                    this.form.url = data.url;
                    this.form.service_type = type;
                    this.form.processor = data.processor

                    console.log(data.model_id, 'data.model_id')
                    if (data.model_id) {
                        this.form.model_data.model_sql_config.model_id = data.model_id
                        // console.log(this.form.model_data.model_sql_config.model_id)
                        this.form.model_data.model_id = data.model_id
                        this.form.model_data.model_overview = data.xgboost_tree
                        this.form.model_data.model_member_status = data.model_status
                        if (data.model_sql_config) {
                            this.form.model_data.model_sql_config = data.model_sql_config
                        }
                        this.form.model_data.model_roles = data.my_role

                        if (data.my_role.includes('promoter')) {
                            this.modelStatusVisible = true
                        }

                        this.form.model_data.model_param = data.model_param
                        this.form.model_data.model_algorithm = data.algorithm
                        this.form.model_data.model_fl_type = data.fl_type
                    }

                    if (data.algorithm === 'XGBoost') {
                        if (data.xgboost_tree && data.xgboost_tree.length) {
                            this.$nextTick(() => {
                                this.graphData = {
                                    id: 'root',
                                    label: 'XGBoost',
                                    children: data.xgboost_tree,
                                }
                            });
                        }
                    }

                    if (params) {
                        this.form.paramsArr = params.map(x => {
                            return {
                                label: x.name ? x.name : x,
                                value: x.name ? x.name : x,
                                desc: x.desc ? x.desc : '',
                            };
                        });
                    }

                    if (data_source) {
                        this.form.data_source.id = data.data_source.id;
                        this.form.data_source.table = data.data_source.table;
                        await this.getDataTable();
                        await this.getTablesFields();

                        if (type === 2) {
                            const rules = data_source.key_calc_rules;

                            if (rules) {
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
                        } else if (type === 1 || type === 3) {
                            this.form.data_source.return_fields = data_source.return_fields.map(x => x.name);
                            this.form.data_source.condition_fields = data_source.condition_fields.map(x => {
                                this.sqlOperator = x.operator;
                                return {
                                    ...x,
                                    condition: x.condition || '=',
                                };
                            });
                        }
                    }
                    if (service_config) {
                        this.service_config = service_config.map(x => {
                            return {
                                ...x,
                                supplier_id: x.member_id,
                                supplier_name: x.member_name,
                                params: x.params ? x.params.split(',') : [],
                            };
                        });
                    }

                    if (data.model_status) {
                        this.partnerData = data.model_status
                    }

                    if (this.show_sql_result === '' && (this.form.service_type === 1 || this.form.service_type === 3)) {
                        await this.sqlShow();
                    }
                    this.api = preview || {};
                }
            }
        },
        serviceTypeChange() {
            this.form.data_source.table = '';
            this.form.data_source.return_fields = [];
            if (this.form.service_type <= 3) {
                this.getDataResources();
            }
            if (this.form.service_type === 7 || this.form.service_type === 8) {
                this.form.url = 'predict/promoter';
                this.file_upload_options.query.fileType = this.form.service_type === 8 ? 'MachineLearningModelFile' : 'DeepLearningModelFile';
            } else {
                this.form.url = '';
            }
        },
        add_params() {
            this.form.paramsArr.push({
                label: '',
                value: '',
                desc: '',
            });
        },
        paramsValidate(index) {
            const {value} = this.form.paramsArr[index];

            if (!value) return;
            for (const i in this.form.paramsArr) {
                const item = this.form.paramsArr[i];

                if (+i !== index && value === item.value) {
                    this.$message.error('参数名不能重复!');
                    break;
                }
            }
        },
        addDataResource() {
            this.$refs['DataSourceEditor'].show();
        },
        async getDataResources() {
            const {code, data} = await this.$http.post({
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
            this.form.stringResult = '';
            this.keyMaps.key_calc_rules = [];
            this.keyMaps.stringResult = '';
            this.form.key_calc_rules = [];
            this.form.data_source.condition_fields = [];
            this.getDataTable();
        },
        async getDataTable() {
            const {code, data} = await this.$http.post({
                url: '/data_source/query_tables',
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
        async getTablesFields() {
            const {code, data} = await this.$http.post({
                url: '/data_source/query_table_fields',
                data: {id: this.form.data_source.id, table_name: this.form.data_source.table},
            });

            if (code === 0) {
                this.data_fields = data.fields;
            }
        },
        deleteParams(index, array) {
            array.splice(index, 1);
        },
        addConditionFields() {
            this.form.data_source.condition_fields.push({
                field_on_param: '',
                field_on_table: '',
                condition: '=',
            });
        },
        addService() {
            const checkedIds = this.service_config.map(x => {
                return x.id;
            });

            this.$refs['serviceConfigs'].show(checkedIds);
        },
        addServiceRow(rows) {
            if (rows.length) {
                this.service_config.push(...rows);
            }
        },
        async sqlShow() {
            const {data_source: obj} = this.form;
            const $params = {
                data_source: {
                    id: obj.id,
                    table: obj.table,
                    return_fields: [],
                },
            };
            $params.data_source.return_fields = obj.return_fields.map(x => {
                return {
                    name: x,
                    value: '',
                };
            });
            $params.data_source.condition_fields = obj.condition_fields.map(x => {
                x.operator = this.sqlOperator;
                return x;
            });
            const {code, data} = await this.$http.post({
                url: '/service/show_sql',
                timeout: 1000 * 60 * 24 * 30,
                data: $params,
            });
            if (code === 0 && data) {
                this.show_sql_result = '预览:' + data.result['sql'];
            }
        },
        sqlTest() {
            for (const i in this.form.paramsArr) {
                const x = this.form.paramsArr[i];

                if (!x.value) {
                    return this.$message.error('缺少查询参数!');
                }
            }

            const {data_source: obj} = this.form;

            this.sql_test.params = [];
            for (const i in obj.condition_fields) {
                const item = obj.condition_fields[i];

                if (!item.field_on_param || !item.field_on_table) {
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
            const {params} = this.sql_test;

            for (let i = 0; i < params.length; i++) {
                paramsJson[params[i].label] = params[i].value;
            }

            const $params = {
                data_source: {
                    id: obj.id,
                    table: obj.table,
                },
            };

            if (type === 1 || type === 3) {
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

            const {code, data} = await this.$http.post({
                url: '/service/sql_test',
                timeout: 1000 * 60 * 24 * 30,
                data: $params,
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

            if (array.length === 0) {
                this.keyMaps.key_calc_rules.push({
                    operator: '',
                    field: [],
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
        calcKeyMaps(event, opt = {action: ''}) {
            const array = this.keyMaps.key_calc_rules;

            this.keyMaps.stringResult = '';
            for (const i in array) {
                const x = array[i];

                if (!x.field || !x.operator) {
                    opt.action === 'confirm' && this.$message.error('主键设置不能为空!');
                    return false;
                } else {
                    this.keyMaps.stringResult += `${i > 0 ? ' + ' : ''}${x.operator}(${x.field.join('+')})`;
                }
            }
            if (opt.action === 'confirm') {
                this.form.stringResult = this.keyMaps.stringResult;
                this.form.key_calc_rules = [...array];
                this.keyMaps.visible = false;
            }

            return true;
        },
        async save(event) {
            if (this.form.service_type < 7) {
                await this.saveService(event);
            } else {
                if (this.serviceId) {
                    if (this.form.service_type === 7) {
                        await this.saveModelConfig();
                    }

                } else {
                    if (this.form.service_type === 7 && !this.form.filename.endsWith(".zip")) {
                        this.$message.error('深度学习只能传zip格式文件');
                        return;
                    } else if (this.form.service_type === 8 && !this.form.filename.endsWith(".txt")) {
                        this.$message.error('机器学习只能传txt格式文件');
                        return;
                    }
                    // this.form.model_data.model_sql_config = this.model_sql_config
                    await this.saveModel(event)
                }
            }

        },

        async saveModelConfig() {
            const {code, data, message} = await this.$http.post({
                url: 'model/update',
                data: {
                    feature_source: 'sql',
                    sql_script: this.form.model_data.model_sql_config.sql_script,
                    data_source_id: this.form.model_data.model_sql_config.data_source_id,
                    model_id: this.form.model_data.model_id,
                    sql_condition_field: this.form.model_data.model_sql_config.sql_condition_field,
                }
            });

            if (code === 0) {

                this.$message.success('模型配置保存成功!');
                this.$router.push({
                    name: 'service-view',
                    query: {id: this.serviceId},
                });
                this.$router.go(0);
            } else {
                this.$message.error('模型配置保存失败: ' + message);
            }

        },

        async saveModel(event) {
            const {code, data} = await this.$http.post({
                url: '/model/import',
                data: {
                    name: this.form.name,
                    filename: this.form.filename,
                    model_type: this.form.service_type === 8 ? 'MachineLearning' : 'DeepLearning',
                },
                btnState: {
                    target: event,
                },
            });

            if (code === 0) {
                this.$message.success('模型导入成功!');
                this.$router.push({
                    name: 'service-view',
                    query: {id: data.id},
                });
                this.$router.go(0);
            }
        },
        async saveService(event) {
            if (!this.form.name || !this.form.url || !this.form.service_type) {
                this.$message.error('请将必填项填写完整！');
                return;
            }

            const {data_source: obj, operator} = this.form;
            const type = this.form.service_type;
            const $params = {
                name: this.form.name,
                url: this.form.url,
                service_type: type,
            };

            if (this.serviceId) {
                $params.id = this.serviceId;
            }

            if (type === 2) {
                if (!this.calcKeyMaps()) return;
                $params.data_source = {
                    id: obj.id,
                    table: obj.table,
                    key_calc_rules: this.form.key_calc_rules.map(x => {
                        return {
                            ...x,
                            field: x.field.join(','),
                        };
                    }),
                    key_calc_rule: this.form.stringResult,
                };
            } else {
                if (type !== 5) {
                    const params = [];

                    for (const i in this.form.paramsArr) {
                        const x = this.form.paramsArr[i];

                        if (!x.value) {
                            return this.$message.error('请将查询字段填写完整!');
                        } else {
                            params.push({
                                name: x.value,
                                desc: x.desc || '',
                            });
                        }
                    }

                    $params.query_params_config = params;
                }

                if (type === 4 || type === 5 || type === 6) {
                    $params.service_config = this.service_config.map(x => {
                        return {
                            id: x.id,
                            name: x.name,
                            member_id: x.supplier_id,
                            member_name: x.supplier_name,
                            url: x.base_url + x.api_name,
                            base_url: x.base_url,
                            api_name: x.api_name,
                            params: x.params ? x.params.join(',') : '',
                            key_calc_rule: x.key_calc_rule,
                        };
                    });
                    $params.operator = operator;

                    if ($params.service_config.length === 0) {
                        return this.$message.error('请选择服务配置');
                    }
                } else {
                    // 1 || 3
                    const return_fields = [];

                    if (type === 1) {
                        this.form.data_source.return_fields.forEach(x => {
                            const item = this.data_fields.find(y => y.name === x);

                            if (item) {
                                return_fields.push(item);
                            }
                        });
                    } else {
                        const item = this.data_fields.find(y => y.name === this.form.data_source.return_fields);

                        if (item) {
                            return_fields.push(item);
                        }
                    }

                    for (const i in obj.condition_fields) {
                        const item = obj.condition_fields[i];

                        if (!item.field_on_param || !item.field_on_table) {
                            return this.$message.error('请将查询字段填写完整!');
                        }
                    }

                    $params.data_source = {
                        id: obj.id,
                        table: obj.table,
                        condition_fields: obj.condition_fields.map(x => {
                            x.operator = this.sqlOperator;
                            return x;
                        }),
                        return_fields,
                    };
                }
            }

            const {code, data} = await this.$http.post({
                url: this.serviceId ? '/service/update' : '/service/add',
                timeout: 1000 * 60 * 24 * 30,
                data: $params,
                btnState: {
                    target: event,
                },
            });

            if (code === 0) {
                if (data) {
                    this.api = data;
                    this.serviceId = data.id;
                }
                this.$message.success('操作成功!');
            }
        },
        async export_sdk() {
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
.maxlength {
    max-width: 400px;
}

.icons {
    cursor: pointer;
    margin-left: 5px;
}

.condition_fields {
    margin-bottom: 10px;

    .el-select, .el-input {
        margin-bottom: 10px;
    }
}

.el-select {
    ::v-deep .el-tag__close {
        background: #fff;
    }
}

.no-arrow {
    ::v-deep .el-input__inner {
        padding: 0;
        text-align: center;
    }

    ::v-deep .el-input__suffix {
        display: none;
    }
}

.flex-form {
    .el-form-item {
        display: flex;
    }
}

.service-list {
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 10px 20px;
}

.el-select-dropdown__item {
    padding-right: 30px;

    &:after {
        right: 15px !important;
    }
}

.api-preview {
    .el-form-item {
        margin-bottom: 5px;
    }
}

.form-box {
    .el-textarea {
        width: 70%;
    }

    .user-tips {
        width: 30%;
        margin-right: 10px;
    }
}

.model-test-result-card {
    width: 620px;
    height: 210px;
}

.dashed-btn {
    background: transparent;
    border: 1px dashed #28c2d7;
    color: #28c2d7;
}

.checkButton {
    width: 60%;
    margin-right: 10px;
}
</style>
