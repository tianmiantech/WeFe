<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form>
            <div class="step-wrap pb30">
                <span class="step">1</span>
                <h3 class="mb20">校验任务</h3>
                <el-form>
                    <el-form-item
                        label="任务名称："
                        label-width="100px"
                    >
                        {{ task.name }}
                    </el-form-item>

                    <el-form-item
                        v-if="task.description"
                        label="任务描述："
                        label-width="100px"
                    >
                        {{ task.description }}
                    </el-form-item>

                    <el-form-item
                        v-if="task.psi_actuator_role === 'server'"
                        label="对方数据量："
                        label-width="100px"
                    >
                        {{ task.data_count }}
                    </el-form-item>

                    <el-form-item
                        label="创建时间："
                        label-width="100px"
                    >
                        {{ task.created_time | dateFormat }}
                    </el-form-item>
                </el-form>
            </div>

            <div class="step-wrap pb30">
                <span class="step">2</span>
                <h3 class="mb20">选择数据资源</h3>
                <el-form>
                    <el-form-item
                        label-width="15px"
                    >
                        <el-alert
                            v-if="bloom_filter_display"
                            :title="'注意: 对方已选择过滤器，我方只能选数据集'"
                            :closable="false"
                            type="warning"
                        />
                        <el-alert
                            v-if="data_set_display"
                            :title="'注意: 对方已选择数据集，我方只能选过滤器'"
                            :closable="false"
                            type="warning"
                        />
                    </el-form-item>

                    <el-form-item
                        label="样本类型："
                        label-width="100px"
                    >
                        <template>
                            <el-radio
                                v-model="task.data_resource_type"
                                label="DataSet"
                                :disabled="data_set_display"
                                @change="task.data_resource_id='',
                                         task.data_resource_name='',
                                         task.row_count='',
                                         fieldInfoList=[],
                                         dataSetList=[]"
                            >
                                数据集
                            </el-radio>
                            <el-radio
                                v-model="task.data_resource_type"
                                :disabled="bloom_filter_display"
                                label="BloomFilter"
                                @change="task.data_resource_id='',
                                         task.data_resource_name='',
                                         task.row_count='',
                                         fieldInfoList=[],
                                         dataSetList=[]"
                            >
                                布隆过滤器
                            </el-radio>
                        </template>
                    </el-form-item>

                    <el-form-item
                        label="对齐样本："
                        label-width="100px"
                        required
                    >
                        <el-button
                            @click="task.data_resource_type =='DataSet'?addDataSet():addBloomFilter()"
                        >
                            + 选择对齐样本
                        </el-button>
                    </el-form-item>

                    <el-table
                        v-if="dataSetList.length>0"
                        :data="dataSetList"
                        stripe
                        border
                        label-width="15px"
                    >
                        <el-table-column
                            label="名称 / Id"
                            min-width="200"
                        >
                            <template slot-scope="scope">
                                <div :title="scope.row.description">
                                    {{ scope.row.name }}
                                    <p class="id">{{ scope.row.id }}</p>
                                </div>
                            </template>
                        </el-table-column>

                        <el-table-column
                            label="列数"
                            min-width="50"
                        >
                            <template slot-scope="scope">
                                {{ rowsFormatter(scope.row.rows) }}
                            </template>
                        </el-table-column>
                        <el-table-column
                            label="数据量"
                            prop="row_count"
                            min-width="80"
                        />
                        <el-table-column
                            label="使用次数"
                            prop="used_count"
                            min-width="80"
                        />

                        <el-table-column
                            label="上传时间"
                            min-width="120"
                        >
                            <template slot-scope="scope">
                                {{ scope.row.created_time | dateFormat }}
                            </template>
                        </el-table-column>
                        <el-table-column
                            fixed="right"
                            label="操作"
                            width="55px"
                        >
                            <template slot-scope="scope">
                                <el-tooltip
                                    content="预览数据"
                                    placement="top"
                                >
                                    <el-button
                                        circle
                                        type="info"
                                        @click="showDataSetPreview(scope.row)"
                                    >
                                        <i class="el-icon-view" />
                                    </el-button>
                                </el-tooltip>
                            </template>
                        </el-table-column>
                    </el-table>


                    <el-table
                        v-if="bloomFilterList.length>0"
                        :data="bloomFilterList"
                        stripe
                        border
                    >
                        <el-table-column
                            label="名称 / Id"
                            min-width="200"
                        >
                            <template slot-scope="scope">
                                <div :title="scope.row.description">
                                    {{ scope.row.name }}
                                    <p class="id">{{ scope.row.id }}</p>
                                </div>
                            </template>
                        </el-table-column>


                        <el-table-column
                            label="列数"
                            min-width="80"
                        >
                            <template slot-scope="scope">
                                {{ rowsFormatter(scope.row.rows) }}
                            </template>
                        </el-table-column>
                        <el-table-column
                            label="数据量"
                            prop="row_count"
                            min-width="80"
                        />
                        <el-table-column
                            label="使用次数"
                            prop="used_count"
                            min-width="80"
                        />


                        <!-- <el-table-column
                                    fixed="right"
                                    label="操作"
                                    width="140px"
                                >
                                    <template slot-scope="scope">
                                        <el-tooltip
                                            content="预览数据"
                                            placement="top"
                                        >
                                            <el-button
                                                circle
                                                type="info"
                                                @click="showBloomFilterPreview(scope.row)"
                                            >
                                                <i class="el-icon-view" />
                                            </el-button>
                                        </el-tooltip>
                                    </template>
                                </el-table-column> -->
                    </el-table>

                    <div class="members mt20">
                        <el-form-item
                            v-if="task.data_resource_type ==='DataSet' && dataSetList.length"
                            label="设置主键："
                            label-width="100px"
                            required
                        >
                            <el-button
                                :disabled="fieldInfoList.length > 4"
                                @click="addFieldInfo"
                            >
                                + 添加主键
                            </el-button>
                        </el-form-item>

                        <div
                            v-for="(item, index) in fieldInfoList"
                            :key="index"
                            class="pl10"
                        >
                            <div class="inlineblock f12 mb10">
                                字段：
                                <el-select
                                    v-model="item.column_arr"
                                    no-data-text="请先选择样本"
                                    multiple
                                    @change="keyFormater"
                                >
                                    <el-option
                                        v-for="value in dataResource.rows"
                                        :key="value"
                                        :value="value"
                                        :label="value"
                                    />
                                </el-select>
                            </div>

                            <div class="inlineblock f12 mb10">
                                处理方式：
                                <el-select
                                    v-model="item.options"
                                    clearable
                                    @change="keyFormater"
                                >
                                    <el-option
                                        v-for="options in optionsList"
                                        :key="options.value"
                                        :value="options.value"

                                        :label="options.name"
                                    />
                                </el-select>

                                <i
                                    v-if="fieldInfoList.length"
                                    class="el-icon-remove-outline f16"
                                    @click="removeFieldInfo({$index: index })"
                                />
                            </div>
                        </div>

                        <div v-if="keyRes">
                            <el-alert
                                :closable="false"
                                class="inlineblock"
                                type="success"
                            >
                                主键生成规则:   {{ keyRes }}
                            </el-alert>
                        </div>
                    </div>

                    <el-form-item
                        v-if="task.data_resource_type =='DataSet' && dataSetList.length>0"
                        label-width="100px"
                        label="是否追溯："
                        required
                    >
                        <div>
                            <el-radio-group
                                v-model="task.is_trace"
                                @change="changeRadio"
                            >
                                <el-radio
                                    :label="true"
                                >
                                    是
                                </el-radio>
                                <el-radio
                                    :label="false"
                                >
                                    否
                                </el-radio>
                            </el-radio-group>
                        </div>
                    </el-form-item>

                    <el-form-item
                        v-if="task.is_trace"
                        label-width="100px"
                        label="追溯字段："
                        required
                    >
                        <el-select
                            v-model="task.trace_column"
                            no-data-text="请先选择样本"
                        >
                            <el-option
                                v-for="value in dataResource.rows"
                                :key="value"
                                :value="value"
                                :label="value"
                            />
                        </el-select>
                    </el-form-item>
                </el-form>
            </div>

            <div class="step-wrap pb30">
                <span class="step">3</span>
                <h3 class="mb20">合作伙伴</h3>
                <el-form>
                    <el-table
                        v-if="task.partner_list.length"
                        :data="task.partner_list"
                        stripe
                        border
                    >
                        <el-table-column
                            label="合作伙伴"
                            width="250"
                        >
                            <template slot-scope="scope">
                                <div>
                                    {{ scope.row.partner_member_name }}
                                    <p class="id">{{ scope.row.partner_member_id }}</p>
                                </div>
                            </template>
                        </el-table-column>

                        <el-table-column
                            label="调用域名"
                            prop="base_url"
                            min-width="200px"
                        />

                        <el-table-column
                            label="操作"
                            min-width="55px"
                        >
                            <el-button
                                type="success"
                                @click="check"
                            >
                                check
                            </el-button>
                        </el-table-column>
                    </el-table>
                </el-form>
            </div>
        </el-form>

        <el-button
            class="save-btn"
            type="primary"
            size="medium"
            @click="handleTask"
        >
            确定
        </el-button>

        <el-dialog
            title="数据预览"
            :visible.sync="show_data_set_preview_dialog"
            append-to-body
        >
            <DataSetPreview ref="DataSetPreview" />
        </el-dialog>

        <SelectDatasetDialog
            ref="SelectDatasetDialog"
            @selectDataSet="selectDataSet"
        />
        <SelectBloomFilterDialog
            ref="SelectBloomFilterDialog"
            @selectBloomFilter="selectBloomFilter"
        />
    </el-card>
</template>

<script>
    import DataSetPreview from '@src/components/views/data-set-preview';
    import SelectDatasetDialog from '@comp/views/select-data-set-dialog';
    import SelectBloomFilterDialog from '@comp/views/select-bloom-filter-dialog';

    export default {
        components: {
            DataSetPreview,
            SelectDatasetDialog,
            SelectBloomFilterDialog,
        },
        data() {
            return {
                bloom_filter_display: false,
                data_set_display:     false,

                // task
                task: {
                    editor:              false,
                    id:                  '',
                    business_id:         '',
                    partner_member_id:   '',
                    partner_member_name: '',
                    name:                '',
                    data_resource_id:    '',
                    data_resource_name:  '',
                    data_resource_type:  '',
                    created_time:        '',
                    psi_actuator_role:   '',
                    description:         '',
                    partner_list:        [],
                    is_trace:            false,
                    trace_column:        '',
                    bloom_filter_list:   [],
                    data_set_list:       [],
                },


                optionsList: [{
                    name:  'md5',
                    value: 'MD5',
                }, {
                    name:  'sha',
                    value: 'SHA1',
                }, {
                    name:  '截取',
                    value: 'SUBSTRING',
                },
                {
                    name:  '不处理',
                    value: 'NONE',
                },
                ],


                // dataResource
                dataResource: {
                    visible:     false,
                    editor:      false,
                    id:          '',
                    name:        '',
                    type:        '',
                    description: '',
                    rows:        '',
                },

                // dataResource
                dataSetList:     [],
                bloomFilterList: [],

                 fieldInfo: {
                    columns:     '',
                    options:     '',
                    frist_index: '',
                    end_index:   '',
                },

                fieldInfoList: [],

                show_data_set_preview_dialog: false,

                keyRes: '',
            };
        },
        created() {
            this.getData();
        },
        methods: {
            async getData() {
                const { code, data } = await this.$http.get({
                    url:    '/task/detail',
                    params: {
                        id: this.$route.query.id,
                    },
                });

                if (code === 0) {
                    this.task = data;
                    if(this.task.psi_actuator_role==='client'){
                       this.bloom_filter_display = true;
                       this.dataSetList.push(this.task.data_set_list[0]);
                    }
                    if(this.task.psi_actuator_role==='server'){
                       this.data_set_display = true;
                       this.bloomFilterList.push(this.task.bloom_filter_list[0]);
                    }
                }
            },

            changeRadio () {
                if(!this.task.is_trace) this.task.trace_column='';
            },

            async handleTask () {
                this.fieldInfoList.forEach((item, index) => {
                    item.columns = item.column_arr.join(',');
                });

                const { code } = await this.$http.post({
                    url:  '/task/handle',
                    data: {
                        id:                 this.task.id,
                        data_resource_id:   this.task.data_resource_id,
                        data_resource_type: this.task.data_resource_type,
                        field_info_list:    this.fieldInfoList,
                        row_count:          this.task.row_count,
                        is_trace:           this.task.is_trace,
                        trace_column:       this.task.trace_column,
                    },
                });

                if (code === 0) {
                    this.task.editor = false;
                    this.$message('处理成功!');
                    this.$router.replace({
                        name: 'task-list',
                    });
                }
            },

            showDataSetPreview(item){
                this.show_data_set_preview_dialog = true;

                this.$nextTick(() =>{
                    this.$refs['DataSetPreview'].loadData(item.id);
                });
            },

            addDataSet() {
                const ref = this.$refs['SelectDatasetDialog'];

                ref.show = true;
                ref.loadDataList();
            },

            selectDataSet(item) {
                this.dataSetList = [];
                this.dataSetList.push(item);

                this.task.data_resource_id=item.id;
                this.task.data_resource_type=item.type;
                this.task.data_resource_name = item.name;
                this.task.row_count = item.row_count;
                this.dataResource.rows = item.rows.split(',');

            },

            addBloomFilter() {
                const ref = this.$refs['SelectBloomFilterDialog'];

                ref.show = true;
                ref.loadDataList();
            },

            selectBloomFilter(item) {
                this.bloomFilterList = [];
                this.bloomFilterList.push(item);

                this.task.data_resource_id=item.id;
                this.task.data_resource_type=item.type;
                this.task.data_resource_name = item.name;
                this.task.row_count = item.row_count;
            },

            addFieldInfo () {
                const fieldInfo = {
                    column_arr:  '',
                    columns:     '',
                    options:     '',
                    frist_index: '',
                    end_index:   '',
                };

                this.fieldInfoList.push(fieldInfo);
            },

            removeFieldInfo($index) {
                this.fieldInfoList.splice($index, 1);

                this.keyFormater();
            },

            keyFormater() {

                let res = '';

                if(this.fieldInfoList.length===0){
                    this.keyRes = res;
                    return;
                }

                const lastItem = this.fieldInfoList[this.fieldInfoList.length - 1];

                if(!lastItem.column_arr.length || !lastItem.options) {
                    return;
                }


                 this.fieldInfoList.forEach((item, index) => {
                     if(item.options === 'NONE'){
                        res = res + `${res?' + ': ''}${item.column_arr.join(' + ')}`;
                     }else{
                        res = res + `${res?' + ': ''}${item.options}(${item.column_arr.join(' + ')})`;
                     }
                });
                res = `Id = ${res}`;
                this.keyRes = res;
            },

            rowsFormatter(rows) {
                const count = rows.split(',');

                return count.length;
            },

            async check () {
                const { code } = await this.$http.get('/partner/check');

                if (code === 0) {
                    this.$message('校验成功!');
                }
            },
        },
    };
</script>

<style lang="scss" scoped>

    .save-btn {
        width: 100px;
    }

    .page{padding-left: 60px;}
    .step-wrap{
        position: relative;
        margin-top: 20px;
        .step,
        &:before{
            content: '';
            position: absolute;
        }
        .step{
            top: -3px;
            left:-52px;
            width: 24px;
            height:24px;
            font-size: 14px;
            line-height:24px;
            text-align: center;
            background:#438BFF;
            border-radius: 50%;
            color:#fff;
        }
        &:before{
            top:0;
            left:-40px;
            width: 1px;
            height:100%;
            border-left: 1px dashed #ccc;
        }
        &.last{
            padding-bottom: 20px;
            &:before{display:none;}
        }
    }
    .el-input, .el-textarea{max-width: 500px;}

    .el-icon-remove-outline{
        color:#ff5757;
        cursor: pointer;
        margin-left: 10px;
    }
    .el-alert{width:auto;}
    .el-select{
        ::v-deep .el-tag__close.el-icon-close{
            background:#fff;
            &:hover{background:#28c2d7;}
        }
    }

    .el-form{
        ::v-deep .el-form-item__label{
            font-weight: bold;
        }
    }
</style>
