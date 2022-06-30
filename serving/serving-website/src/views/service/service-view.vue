<template>
    <el-card
        v-loading="loading"
        class="page service_view"
        shadow="never"
    >
        <div class="left_box">
            <el-form
                :model="form"
                :rules="rules"
                class="form-box"
            >
                <p
                    name="基本信息"
                    class="nav-title mb10"
                >
                    基本信息：
                </p>
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
                            :disabled="serviceId !== undefined"
                            @change="serviceTypeChange"
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
                        v-if="currentDesc"
                        class="ml5"
                        style="font-size: 13px; color: #666; line-height: 36px;"
                    >
                        <el-tooltip
                            class="item"
                            effect="light"
                            :content="currentDesc"
                            placement="right"
                            popper-class="service_tips"
                        >
                            <i
                                class="el-icon-info"
                                style="margin-right: 4px"
                            />
                        </el-tooltip>
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

                <!-- <el-form-item
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
                </el-form-item> -->

                <template v-if="form.service_type">
                    <template v-if="form.service_type === 4 || form.service_type === 5 || form.service_type === 6">
                        <el-divider />
                        <p
                            class="mb10 nav-title"
                            name="配置联邦服务"
                        >
                            配置联邦服务：
                        </p>
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
                                class="dashed-btn"
                                @click="addService"
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
                        v-if="form.service_type !== 2 && form.service_type !== 5 && form.service_type !== 7 && form.service_type !== 8"
                    >
                        <el-divider />
                        <p
                            name="查询参数配置"
                            class="mb10 nav-title"
                        >
                            查询参数配置：
                        </p>
                        <el-button
                            v-if="form.paramsArr.length === 0"
                            class="icons el-icon-circle-plus-outline"
                            @click="add_params"
                        />
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
                            />
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
                        v-if="form.service_type !== 4 && form.service_type !== 5 && form.service_type !== 6 && form.service_type !== 7 && form.service_type !== 8"
                    >
                        <el-divider />
                        <p
                            name="SQL 配置"
                            class="mb10 nav-title"
                        >
                            SQL 配置：
                        </p>
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
                                class="ml10"
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
                                />
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
                            <el-divider />
                        </template>
                    </template>
                    <template v-if="form.service_type === 7 || form.service_type === 8">
                        <el-form-item
                            v-if="!form.model_data.model_id"
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
                                <uploader-unsupport />
                                <uploader-drop v-if="file_upload_options.files.length === 0">
                                    <p class="mb10">将文件（.txt/.zip）拖到此处</p>或
                                    <uploader-btn
                                        :attrs="{accept: ['.txt','.zip']}"
                                        :single="true"
                                    >
                                        点击上传
                                    </uploader-btn>
                                </uploader-drop>
                                <uploader-list :file-list="file_upload_options.files.length" />
                            </uploader>
                        </el-form-item>

                        <el-divider />
                        <div v-if="form.service_type === 7 && form.model_data.model_id">
                            <p
                                name="模型概览"
                                class="nav-title mb10"
                            >
                                模型概览：
                            </p>
                            <el-form-item class="service-list">
                                <p><strong>Id: </strong> {{ form.model_data.model_id }}</p>
                                <p><strong>算法: </strong> {{ form.model_data.model_algorithm }}</p>
                                <p><strong>训练类型: </strong> {{ flType(form.model_data.model_fl_type) }}</p>
                                <p>
                                    <strong>我的角色: </strong>
                                    <el-tag
                                        v-for="each in form.model_data.model_roles"
                                        :key="each"
                                    >
                                        {{ each }}
                                    </el-tag>
                                </p>
                                <p>
                                    <strong>模型结构: </strong>
                                    <el-button
                                        v-if="algorithm === 'XGBoost'"
                                        size="mini"
                                        round
                                        @click="show_model_overview"
                                    >
                                        展示
                                    </el-button>
                                    <el-button
                                        v-else
                                        size="mini"
                                        @click="showTableData"
                                    >
                                        查看
                                    </el-button>
                                </p>
                            </el-form-item>
                        </div>

                        <p
                            v-if="modelStatusVisible && form.model_data.model_fl_type !== 'horizontal'"
                            class="mb10 nav-title"
                            name="合作方模型状态"
                        >
                            合作方模型状态：
                            <el-button
                                size="medium"
                                icon="el-icon-refresh"
                                type="text"
                                :loading="checkLoading"
                                @click="refreshPartnerStatus('')"
                            />
                        </p>
                        <el-form-item
                            v-if="modelStatusVisible && form.model_data.model_fl_type !== 'horizontal'"
                            class="service-list"
                        >
                            <el-table
                                :loading="partnerTableLoading"
                                :data="partnerData"
                                style="width: 100%"
                            >
                                <el-table-column
                                    label="合作者ID"
                                    prop="member_id"
                                />
                                <el-table-column
                                    label="合作者名称"
                                    prop="member_name"
                                    width="160"
                                />
                                <el-table-column
                                    label="URL"
                                    prop="url"
                                />
                                <el-table-column label="状态">
                                    <template slot-scope="scope">
                                        <el-popover
                                            v-if="scope.row.status === 'available'"
                                            placement="top-start"
                                            width="100"
                                            trigger="hover"
                                            content="模型已上线"
                                        >
                                            <el-button
                                                slot="reference"
                                                size="middle"
                                                type="text"
                                                icon="el-icon-check"
                                            />
                                        </el-popover>

                                        <el-popover
                                            v-if="scope.row.status === 'unavailable'"
                                            placement="top-start"
                                            title="⚠️警告"
                                            width="200"
                                            trigger="hover"
                                            content="模型未上线"
                                        >
                                            <el-button
                                                slot="reference"
                                                size="middle"
                                                type="text"
                                                icon="el-icon-warning"
                                            />
                                        </el-popover>

                                        <el-popover
                                            v-if="scope.row.status === 'offline'"
                                            placement="top-start"
                                            title="⚠️警告"
                                            width="200"
                                            trigger="hover"
                                            content="模型不可用"
                                        >
                                            <el-button
                                                slot="reference"
                                                type="text"
                                                icon="el-icon-warning"
                                            />
                                        </el-popover>
                                    </template>
                                </el-table-column>

                                <el-table-column
                                    label="操作"
                                    align="right"
                                    width="60"
                                >
                                    <template slot-scope="scope">
                                        <el-button
                                            size="middle"
                                            icon="el-icon-refresh"
                                            type="text"
                                            :loading="checkLoading"
                                            @click="refreshPartnerStatus( scope.row.member_id)"
                                        />
                                    </template>
                                </el-table-column>
                            </el-table>
                        </el-form-item>


                        <el-dialog
                            title="模型结构"
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

                        <p
                            v-if="form.model_data.model_id"
                            name="特征配置"
                            class="mb10 nav-title"
                        >
                            特征配置：
                        </p>

                        <el-form-item v-if="form.model_data.model_id">
                            <div class="config_box_left">
                                <div class="mb10">
                                    <el-radio-group
                                        v-model="activeName"
                                        class="ml-4"
                                    >
                                        <el-radio
                                            label="code"
                                            size="small"
                                        >
                                            代码配置
                                        </el-radio>
                                        <el-radio
                                            label="sql"
                                            size="small"
                                        >
                                            SQL配置
                                        </el-radio>
                                    </el-radio-group>
                                </div>
                                <div>
                                    <el-row
                                        v-if="activeName === 'code'"
                                        :span="24"
                                    >
                                        <el-col :span="3">
                                            <p class="mb10"><strong>处理器：</strong></p>
                                        </el-col>
                                        <el-col
                                            :span="12"
                                            style="margin-right: 10px;"
                                        >
                                            <el-input
                                                v-model="form.processor"
                                                :disabled="true"
                                            />
                                            <p />
                                        </el-col>
                                    </el-row>
                                    <div v-if="activeName === 'sql'">
                                        <el-form-item
                                            label="数据源："
                                            label-width="121px"
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
                                            label="SQL脚本："
                                            label-width="121px"
                                            :rules="[{required: true, message: 'SQL脚本必填!'}]"
                                        >
                                            <el-input
                                                v-model="form.model_data.model_sql_config.sql_script"
                                                type="textarea"
                                                placeholder="如：select x0,x1,x2 form table where user_id = ?"
                                                clearable
                                                rows="4"
                                            />
                                        </el-form-item>
                                        <el-form-item
                                            label="查询条件字段："
                                            label-width="121px"
                                            :rules="[{required: true, message: '查询条件字段必填!'}]"
                                        >
                                            <el-input
                                                v-model="form.model_data.model_sql_config.sql_condition_field"
                                                type="text"
                                                placeholder="例如：id"
                                                class="user-tips"
                                                clearable
                                            />=
                                            <el-input
                                                v-model="form.model_data.model_sql_config.user_id"
                                                type="text"
                                                placeholder="查询的字段对应的值"
                                                class="user-tips"
                                                style="width: 40%"
                                            />
                                            <el-button
                                                type="primary"
                                                style="width: 95px;"
                                                @click="sqlTestPreview"
                                            >
                                                测试预览
                                            </el-button>
                                        </el-form-item>
                                    </div>
                                </div>
                            </div>
                        </el-form-item>

                        <div
                            v-if="form.model_data.model_id"
                            name="可用性测试"
                            class="nav-title"
                        >
                            <p class="mt10">可用性测试：</p>
                            <div
                                class="mt10"
                                style="border: 1px solid #ccc; padding: 20px; border-radius: 4px;"
                            >
                                <div class="can_use_test">
                                    <div>
                                        <el-form-item
                                            label="输入特征："
                                            label-width="94px"
                                        >
                                            <el-switch v-model="isEnterCharacter" />
                                        </el-form-item>
                                        <el-form-item
                                            v-if="isEnterCharacter"
                                            label="样本特征："
                                            label-width="94px"
                                            :rules="[{required: true, message: '样本特征必填!'}]"
                                            :style="{marginBottom: isEnterCharacter && myRole === 'promoter' ? '10px' : 'unset'}"
                                        >
                                            <el-input
                                                v-model="form.model_data.check_data.feature_data"
                                                type="textarea"
                                                clearable
                                                rows="4"
                                                placeholder="如:{&quot;x0&quot;: 0.1, &quot;x1&quot;: 0.2}"
                                            />
                                        </el-form-item>
                                        <el-form-item
                                            v-if="!isEnterCharacter || (myRole === 'promoter' && form.model_data.model_fl_type !== 'horizontal')"
                                            label="样本ID："
                                            label-width="94px"
                                            :rules="[{required: true, message: '样本特征必填!'}]"
                                            style="margin-bottom: unset;"
                                        >
                                            <el-input
                                                v-model="form.model_data.check_data.sample_id"
                                                type="text"
                                                clearable
                                                style="width: 50%;"
                                            />
                                        </el-form-item>
                                    </div>
                                    <el-button
                                        type="primary"
                                        class="ml10 mr10"
                                        style="text-align: right;"
                                        @click="testModel"
                                    >
                                        可用性校验
                                    </el-button>
                                </div>

                                <div
                                    v-if="predictResult !== ''"
                                    class="test_result"
                                    style="margin-top: 30px; margin-left: 35px;"
                                >
                                    结果：
                                    <JsonViewer
                                        :value="predictResult"
                                        copyable
                                    />
                                </div>
                            </div>

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
                        </div>
                    </template>
                </template>
                <el-button
                    class="mt10"
                    type="primary"
                    size="medium"
                    :disabled="!form.service_type"
                    @click="save"
                >
                    保存
                </el-button>
                <el-link
                    type="primary"
                    :disabled="!api.id"
                    style="margin-left: 10px"
                    @click="export_sdk"
                >
                    点击下载工具包
                </el-link>
                <div
                    v-if="api.params || api.method || api.url"
                    class="api-preview"
                >
                    <el-divider />
                    <p
                        name="API 预览"
                        class="mb20 f16 nav-title"
                    >
                        API 预览:
                    </p>
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

            <el-dialog
                :visible.sync="isShowTable"
                title="模型结构"
                width="70%"
                top="10vh"
            >
                <el-table
                    :data="gridData"
                    style="width: 100%;"
                    height="500"
                    border
                >
                    <el-table-column
                        property="feature"
                        label="特征"
                    />
                    <el-table-column
                        property="weight"
                        label="权重"
                    />
                </el-table>
            </el-dialog>

            <el-dialog
                title="查询结果预览"
                :visible.sync="sqlResultDialog"
            >
                <p class="mb10">
                    <span class="code_1">{{ form.model_data.model_sql_config.sql_script }}</span> where <span class="code_2">{{ form.model_data.model_sql_config.sql_condition_field }}=</span><span class="code_2">{{ form.model_data.model_sql_config.user_id }}</span> limit 1
                </p>
                <el-table
                    :data="tableDataPreview"
                    style="width: 100%"
                    border
                    stripe
                >
                    <el-table-column
                        v-for="item in tableColumns"
                        :key="item.label"
                        :prop="item.label"
                        :label="item.label"
                    />
                </el-table>
            </el-dialog>
        </div>
        <div class="right_box">
            <!-- <el-divider content-position="center">配置说明</el-divider> -->
            <h3
                v-if="form.service_type"
                class="f16"
            >
                配置说明
            </h3>
            <div class="config_box">
                <div
                    v-if="currentDesc"
                    class="service_item"
                >
                    <h3>服务类型：</h3>
                    <div class="service_desc">{{ currentDesc }}</div>
                </div>
                <div
                    v-if="modelStatusVisible && form.model_data.model_fl_type !== 'horizontal'"
                    class="service_item"
                >
                    <h3>合作方模型状态：</h3>
                    <div class="service_desc">
                        联邦学习的纵向训练可能有多个参与方，发起
                        方在建立模型服务的时候需要依赖于协作方的模
                        型服务， 如果协作方的模型服务不可用或未上线
                        ，则发起方配置的服务无法进行正确预测。
                        <p class="highlight mt10">注意：需保证协作方的模型服务可用！</p>
                    </div>
                </div>
                <div
                    v-if="form.model_data.model_id"
                    class="service_item"
                >
                    <h3>特征配置：</h3>
                    <div class="service_desc">
                        <p>模型服务的入模特征需要进行配置，包含两种方式：</p>
                        <p>1、代码配置：使用者根据自己的数据源获取方式，编写一段通过输入的样本ID获取样本特征的代码。指定该模型服务使用代码配置。具体做法：</p>
                        <p>a) 继承AbstractFeatureDataProcessor类。</p>
                        <p>b) 添加FeatureProcessor注解，注解的值使用模型服务ID。</p>
                        <p>c) 实现processor方法，方法里面实现获取特征代码。</p>
                        <p>提示：默认处理器EmptyFeatureDataProcessor,不做任何处理返回为空。</p>
                        <p>2、SQL配置：使用指定已经配置好的数据源，编写相应的SQL查询语句，并选定样本的查询条件值。</p>
                        <p class="highlight mt10">注意：样本的特征也可以在调用时，由接口传入。</p>
                    </div>
                </div>
                <div
                    v-if="form.model_data.model_id"
                    class="service_item"
                >
                    <h3>可用性测试：</h3>
                    <div class="service_desc">
                        <p>模型的可用性测试的特征可自行填写(格式参照输入样例)或从已配置的来源中获取。</p>
                        <p class="mt10 mb10">预测返回参数说明：</p>
                        <el-table
                            :data="canUseTestList"
                            style="width: 100%"
                            border
                        >
                            <el-table-column
                                prop="field"
                                label="字段"
                                width="100"
                            />
                            <el-table-column
                                prop="type"
                                label="类型"
                                width="90"
                            />
                            <el-table-column
                                prop="desc"
                                label="描述"
                            />
                        </el-table>
                    </div>
                </div>
            </div>
        </div>
    </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import ServiceConfigs from './service_config';
import DataSourceEditor from '../data_source/data-source-edit';
import { Grid, Minimap, Tooltip, TreeGraph } from '@antv/g6';

export default {
    components: {
        ServiceConfigs,
        DataSourceEditor,
    },
    data() {
        return {
            dataBaseOptions:   [],
            predictResult:     '',
            requestDataDialog: false,
            jsonData:          '',
            title:             '',
            sqlPredictResult:  {
                data:      '',
                algorithm: '',
                my_role:   '',
                type:      '',
            },
            loading:          false,
            service_overview: {},
            fileStatusText:   {
                success:   '成功',
                error:     '错误',
                uploading: '上传中',
                paused:    '已暂停',
                waiting:   '等待中',
            },
            form: {
                name:         '',
                filename:     '',
                url:          '',
                service_type: '',
                operator:     'sum',
                data_source:  {
                    id:               '',
                    table:            '',
                    return_fields:    [],
                    condition_fields: [
                        {
                            condition:      '=',
                            field_on_param: '',
                            field_on_table: '',
                        },
                    ],
                },
                paramsArr: [{
                    label: '',
                    value: '',
                    desc:  '',
                }],
                key_calc_rules: [],
                stringResult:   '',
                model_data:     {
                    check_data: {
                        sample_id: '',
                    },
                    model_id:         '',
                    model_param:      '',
                    model_sql_config: {
                        feature_source:      '',
                        data_source_id:      '',
                        sql_script:          '',
                        sql_condition_field: '',
                        user_id:             '',
                    },
                    model_member_status: [],
                    model_overview:      '',
                    // 可能自己跟自己建模
                    model_roles:         [],
                    model_algorithm:     '',
                    model_fl_type:       '',
                },
                processor: '',
            },
            partnerData:         [],
            partnerTableLoading: false,
            file_upload_options: {
                files:               [],
                target:              window.api.baseUrl + '/file/upload',
                singleFile:          true,
                // chunks check
                testChunks:          true,
                chunkSize:           8 * 1024 * 1024,
                simultaneousUploads: 4,
                headers:             {
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
                // url:          [{ required: true, message: '服务地址必填!' }],
                service_type: [{ required: true, message: '服务类型必选!' }],
            },
            serviceId:       '',
            serviceType:     this.$route.query.service_type,
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
                {
                    name:  '机器学习模型服务',
                    value: 7,
                },
                {
                    name:  '深度学习模型服务',
                    value: 8,
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
            sqlOperator:        'and',
            show_sql_result:    '',
            currentDesc:        '',
            model_show_flag:    false,
            graphData:          {},
            checkLoading:       false,
            activeName:         'code',
            modelStatusVisible: false,
            canUseTestList:     [
                {
                    field: 'algorithm',
                    type:  'String',
                    desc:  '算法类型',
                },
                {
                    field: 'type',
                    type:  'String',
                    desc:  '训练类型 横向/纵向',
                },
                {
                    field: 'my_role',
                    type:  'String',
                    desc:  '我的参与角色',
                },
                {
                    field: 'result',
                    type:  'Object',
                    desc:  '推理返回信息',
                },
                {
                    field: 'user_id',
                    type:  'String',
                    desc:  '样本ID',
                },
                {
                    field: 'score',
                    type:  'Double',
                    desc:  '逻辑回归概率分数',
                },
                {
                    field: 'scores',
                    type:  'Object',
                    desc:  'Xgboost算法概率',
                },
                {
                    field: 'xgboost_tree',
                    type:  'Map',
                    desc:  '协作方树节点走向',
                },
                {
                    field: 'feature_result',
                    type:  'Object',
                    desc:  '特征查询情况',
                },
                {
                    field: 'error',
                    type:  'String',
                    desc:  '特征查询错误',
                },
                {
                    field: 'found',
                    type:  'Boolean',
                    desc:  '特征查得、未查得',
                },
            ],
            isEnterCharacter: false,
            isShowTable:      false,
            gridData:         [],
            myRole:           '',
            sqlResultDialog:  false,
            tableDataPreview: [],
            tableColumns:     [],
        };
    },
    computed: {
        ...mapGetters(['userInfo']),
        flType(val) {
            return function(val) {
                return val === 'vertical' ? '纵向' : val === 'horizontal' ? '横向' : '混合';
            };
        },
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
        this.$nextTick(_ => {
            this.$bus.$emit('update-title-navigator');
        });
    },
    methods: {
        async sqlTestPreview($event) {
            this.tableDataPreview = [];
            this.tableColumns = [];
            if (
                this.form.model_data.model_sql_config.data_source_id
                && this.form.model_data.model_sql_config.sql_script
                && this.form.model_data.model_sql_config.sql_condition_field
                && this.form.model_data.model_sql_config.user_id
            ) {
                const { code, data } = await this.$http.get({
                    url:    '/predict/sql_config_test',
                    params: {
                        data_source_id:      this.form.model_data.model_sql_config.data_source_id,
                        sql_script:          this.form.model_data.model_sql_config.sql_script,
                        sql_condition_field: this.form.model_data.model_sql_config.sql_condition_field,
                        user_id:             this.form.model_data.model_sql_config.user_id,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if (code === 0 && data && data.feature_data_map) {
                    for (const key in data.feature_data_map) {
                        const val = data.feature_data_map[key];

                        this.tableColumns.unshift({
                            label: key,
                            value: val,
                        });
                    }
                    this.tableDataPreview.push(data.feature_data_map);
                    console.log(this.tableColumns);
                    console.log(this.tableDataPreview);

                    this.sqlResultDialog = true;
                }
            } else {
                this.$message.error('请填写必填字段！');
                return;
            }
        },
        showTableData() {
            this.isShowTable = true;
        },
        showRequest(data) {
            this.requestDataDialog = true;
            this.title = '请求体';
            setTimeout(() => {
                this.jsonData = JSON.parse(data);
            });
        },

        async testModel() {
            if (this.myRole === 'promoter' && this.form.model_data.model_fl_type !== 'horizontal') {
                // 发起方
                if (!this.form.model_data.check_data.sample_id || this.form.model_data.check_data.sample_id === '') {
                    this.$message.error('样本ID不能为空！');
                    return;
                } else if (this.isEnterCharacter && (!this.form.model_data.check_data.feature_data || this.form.model_data.check_data.feature_data === '')) {
                    this.$message.error('样本特征不能为空！');
                    return;
                }
            } else {
                if (this.isEnterCharacter && (!this.form.model_data.check_data.feature_data || this.form.model_data.check_data.feature_data === '')) {
                    this.$message.error('样本特征不能为空！');
                    return;
                }
            }
            const params = {
                model_id:       this.form.model_data.model_id,
                user_id:        this.form.model_data.check_data.sample_id || '',
                feature_source: this.activeName,
                params:         this.form.model_data.model_sql_config,
                my_role:        this.form.model_data.model_roles,
            };

            if (this.isEnterCharacter) {
                if (this.isJSON(this.form.model_data.check_data.feature_data)) {
                    const feature_data = JSON.parse(this.form.model_data.check_data.feature_data);

                    params.feature_data = feature_data;
                } else {
                    this.$message.error('样本特征格式有误！');
                    return;
                }

            }
            this.loading = true;
            const { code, data } = await this.$http.post({
                url:  'predict/debug',
                data: params,
            });

            if (code === 0) {
                this.sqlPredictResult = data;
                this.predictResult = data;
            }
            this.loading = false;
        },
        isJSON(str) {
            if (typeof str === 'string') {
                try {
                    const obj=JSON.parse(str);

                    if(typeof obj === 'object' && obj ){
                        return true;
                    }else{
                        return false;
                    }

                } catch(e) {
                    console.log('error：'+str+'!!!'+e);
                    return false;
                }
            }
        },

        async getDataSource() {
            const { code, data } = await this.$http.get({
                url:    '/data_source/query',
                params: {
                    id:         '',
                    name:       '',
                    page_index: '',
                    page_size:  '',
                },
            });

            if (code === 0) {
                const data_list = data.list;

                for (let i = 0; i < data_list.length; i++) {
                    this.dataBaseOptions.push({
                        label: data_list[i].name,
                        value: data_list[i].id,
                    });
                }
            }
        },

        async refreshPartnerStatus(partner_id) {
            console.log(partner_id);
            this.checkLoading = true;
            const params = {
                serviceId: this.form.model_data.model_id,
            };

            if (partner_id) params.member_id = partner_id;
            const { code, data } = await this.$http.get({
                url: '/model/status/check',
                params,
            });

            if (code === 0) {
                this.partnerData = data;
            }
            this.checkLoading = false;
        },


        show_model_overview() {
            this.model_show_flag = true;
            // let that = this;
            setTimeout(() => {
                this.createGraph(this.graphData);
            }, 200);

        },

        createGraph(data) {
            const canvas = this.$refs['canvas'];
            const grid = new Grid();
            const minimap = new Minimap();
            const tooltip = new Tooltip({
                getContent(e) {
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
                width:     canvas.offsetWidth,
                height:    420,
                modes:     {
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
                    type:      'dendrogram',
                    direction: 'TB', // H / V / LR / RL / TB / BT
                    nodeSep:   40,
                    rankSep:   100,
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
                    label:    node.id,
                    labelCfg: {
                        position,
                        offset: 5,
                        style:  {
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

            const { code, data } = await this.$http.get({
                url:     '/file/merge',
                timeout: 1000 * 60 * 2,
                params:  {
                    filename:         e.file.name,
                    uniqueIdentifier: e.uniqueIdentifier,
                    fileType:         this.form.service_type === 7 ? 'MachineLearningModelFile' : 'DeepLearningModelFile',
                },
            });

            this.loading = false;
            if (code === 0) {
                this.form.filename = data.filename;
            } else {
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
            this.loading = true;
            const { code, data } = await this.$http.post({
                url:  '/service/detail',
                data: { id: this.serviceId, service_type: this.serviceType },
            });

            if (code === 0) {
                this.loading = false;
                if (data) {
                    const {
                        service_type: type,
                        service_config,
                        data_source,
                        preview,
                        model_status,
                    } = data;
                    const params = data.query_params_config || data.query_params;

                    this.form.name = data.name;
                    this.form.url = data.url;
                    this.form.service_type = type;
                    this.form.processor = data.processor;
                    this.myRole = data.my_role[0];
                    this.activeName = data.feature_source;

                    console.log(data.model_id, 'data.model_id');
                    if (data.id) {
                        this.form.model_data.model_sql_config.model_id = data.model_id;
                        // console.log(this.form.model_data.model_sql_config.model_id)
                        this.form.model_data.model_id = data.service_id;
                        this.form.model_data.model_overview = data.xgboost_tree;
                        this.form.model_data.model_member_status = data.model_status;
                        if (data.model_sql_config) {
                            this.form.model_data.model_sql_config = data.model_sql_config;
                        }
                        this.form.model_data.model_roles = data.my_role;

                        if (model_status && model_status.length) {
                            this.modelStatusVisible = true;
                        }

                        this.form.model_data.model_param = data.model_param;
                        this.form.model_data.model_algorithm = data.algorithm;
                        this.form.model_data.model_fl_type = data.fl_type;
                    }
                    this.algorithm = data.algorithm;
                    if (data.algorithm && data.algorithm === 'XGBoost') {
                        if (data.xgboost_tree && data.xgboost_tree.length) {
                            this.$nextTick(() => {
                                this.graphData = {
                                    id:       'root',
                                    label:    'XGBoost',
                                    children: data.xgboost_tree,
                                };
                            });
                            this.gridData = [];
                            for (let i = 0; i < data.xgboost_tree.length; i++) {
                                const feature = data.xgboost_tree[i].data.feature,
                                    weight = data.xgboost_tree[i].data.weight;

                                this.gridData.push({
                                    feature,
                                    weight,
                                });
                            }
                        }
                    }
                    if (data.model_param && data.model_param.iters) {
                        this.gridData = [];
                        for (let i = 0; i < data.model_param.header.length; i++) {
                            const name = data.model_param.header[i];

                            this.gridData.push({
                                feature: name,
                                weight:  data.model_param.weight[name],
                            });
                        }
                    }

                    if (params) {
                        this.form.paramsArr = params.map(x => {
                            return {
                                label: x.name ? x.name : x,
                                value: x.name ? x.name : x,
                                desc:  x.desc ? x.desc : '',
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
                                supplier_id:   x.member_id,
                                supplier_name: x.member_name,
                                params:        x.params ? x.params.split(',') : [],
                            };
                        });
                    }

                    if (data.model_status) {
                        this.partnerData = data.model_status;
                    }

                    if (this.show_sql_result === '' && (this.form.service_type === 1 || this.form.service_type === 3)) {
                        await this.sqlShow();
                    }
                    this.api = preview || {};
                    this.$router.push({
                        name:  'service-view',
                        query: {
                            ...this.$route.query,
                            isRefresh: Math.random(),
                        },
                    });
                }
            }
            this.loading = false;
        },
        serviceTypeChange() {
            this.form.data_source.table = '';
            this.form.data_source.return_fields = [];
            if (this.form.service_type <= 3) {
                this.getDataResources();
            }
            if (this.form.service_type === 7 || this.form.service_type === 8) {
                this.form.url = 'predict/promoter';
                this.file_upload_options.query.fileType = this.form.service_type === 7 ? 'MachineLearningModelFile' : 'DeepLearningModelFile';
            } else {
                this.form.url = '';
            }
            this.$router.push({
                name:  'service-view',
                query: { isRefresh: Math.random() },
            });
        },
        add_params() {
            this.form.paramsArr.push({
                label: '',
                value: '',
                desc:  '',
            });
        },
        paramsValidate(index) {
            const { value } = this.form.paramsArr[index];

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
            this.form.stringResult = '';
            this.keyMaps.key_calc_rules = [];
            this.keyMaps.stringResult = '';
            this.form.key_calc_rules = [];
            this.form.data_source.condition_fields = [];
            this.getDataTable();
        },
        async getDataTable() {
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
        async getTablesFields() {
            const { code, data } = await this.$http.post({
                url:  '/data_source/query_table_fields',
                data: { id: this.form.data_source.id, table_name: this.form.data_source.table },
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
                condition:      '=',
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
            const { data_source: obj } = this.form;
            const $params = {
                data_source: {
                    id:            obj.id,
                    table:         obj.table,
                    return_fields: [],
                },
            };

            $params.data_source.return_fields = obj.return_fields.map(x => {
                return {
                    name:  x,
                    value: '',
                };
            });
            $params.data_source.condition_fields = obj.condition_fields.map(x => {
                x.operator = this.sqlOperator;
                return x;
            });
            const { code, data } = await this.$http.post({
                url:     '/service/show_sql',
                timeout: 1000 * 60 * 24 * 30,
                data:    $params,
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

            const { data_source: obj } = this.form;

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
            const { params } = this.sql_test;

            for (let i = 0; i < params.length; i++) {
                paramsJson[params[i].label] = params[i].value;
            }

            const $params = {
                data_source: {
                    id:    obj.id,
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

            if (array.length === 0) {
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
                    if (this.form.service_type === 7 && !this.form.filename.endsWith('.txt')) {
                        this.$message.error('机器学习只能传txt格式文件');
                        return;
                    } else if (this.form.service_type === 8 && !this.form.filename.endsWith('.zip')) {
                        this.$message.error('深度学习只能传zip格式文件');
                        return;
                    }
                    // this.form.model_data.model_sql_config = this.model_sql_config
                    await this.saveModel(event);
                }
            }

        },

        async saveModelConfig() {
            const { code, data, message } = await this.$http.post({
                url:  'model/update',
                data: {
                    feature_source:      'sql',
                    sql_script:          this.form.model_data.model_sql_config.sql_script,
                    data_source_id:      this.form.model_data.model_sql_config.data_source_id,
                    serviceId:           this.form.model_data.model_id,
                    sql_condition_field: this.form.model_data.model_sql_config.sql_condition_field,
                    serviceName:         this.form.name,
                },
            });

            if (code === 0) {

                this.$message.success('模型配置保存成功!');
                this.$router.push({
                    name:  'service-view',
                    query: {
                        ...this.$route.query,
                        id:        this.serviceId,
                        isRefresh: Math.random(),
                    },
                });
                this.$router.go(0);
            } else {
                this.$message.error('模型配置保存失败: ' + message);
            }

        },

        async saveModel(event) {
            const { code, data } = await this.$http.post({
                url:  '/model/import',
                data: {
                    name:         this.form.name,
                    filename:     this.form.filename,
                    service_type: this.form.service_type,
                },
                btnState: {
                    target: event,
                },
            });

            if (code === 0) {
                this.$message.success('模型导入成功!');
                this.$router.push({
                    name:  'service-view',
                    query: { id: data.id, service_type: this.form.service_type },
                });
                this.$router.go(0);
            }
        },
        async saveService(event) {
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

            if (this.serviceId) {
                $params.id = this.serviceId;
            }

            if (type === 2) {
                if (!this.calcKeyMaps()) return;
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
                            id:            x.id,
                            name:          x.name,
                            member_id:     x.supplier_id,
                            member_name:   x.supplier_name,
                            url:           x.base_url + x.api_name,
                            base_url:      x.base_url,
                            api_name:      x.api_name,
                            params:        x.params ? x.params.join(',') : '',
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
.service_view {
    margin-right: 80px;
    .left_box {
        width: 60%;
        .code_1 {
            color: #f00;
        }
        .code_2 {
            color: $--color-success;
        }
    }
    .right_box {
        max-width: 320px;
        >h3 {
            padding-left: 10px;
            border-left: 1px dashed #ccc;
        }
        .config_box {
            padding-left: 10px;
            border-left: 1px dashed #ccc;
        }
        .service_item {
            margin-bottom: 10px;
            font-family: Microsoft YaHei;
            h3 {
                font-size: 14px;
                line-height: 28px;
                color: #333;
                font-weight: bold;
            }
            .service_desc {
                font-size: 13px;
                line-height: 18px;
                text-align: justify;
                text-indent: 14px;
                color: #5A5A5A;
                .highlight {
                    color: #cc0000;
                    opacity: .8;
                }
            }
        }
    }
    .config_box_left {
        border: 1px solid #ccc;
        border-radius: 4px;
        padding: 10px 20px;
    }
    .can_use_test {
        display: flex;
        align-items: flex-end;
        >div:first-child {
            width: 100%;
        }
        .el-input, .el-textarea {
            width: 100% !important;
        }
    }
    // .test_result {
    //     .jv-container {
    //        .jv-code {
    //             padding: 30px 20px 0 !important;
    //         }
    //     }
    // }
}
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
    .el-textarea, .el-input {
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

<style lang="scss">
.service_tips {max-width: 400px !important;}
.service_view .el-card__body {
    display: flex;
    justify-content: space-between;
}
.test_result {
    .jv-container {
        .jv-code {
            padding: 30px 20px 0 !important;
        }
    }
}
</style>
