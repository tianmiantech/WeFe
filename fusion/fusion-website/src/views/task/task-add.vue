<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form>
            <div class="step-wrap pb30">
                <span class="step">1</span>
                <h3 class="mb20">发起任务</h3>
                <el-form>
                    <el-form-item
                        label="任务名称："
                        label-width="100px"
                        required
                    >
                        <el-input
                            v-model="task.name"
                            maxlength="40"
                            show-word-limit
                            style="max-width:400px;"
                        />
                    </el-form-item>
                    <el-form-item
                        label="任务描述："
                        label-width="100px"
                        required
                    >
                        <el-input
                            v-model="task.description"
                            type="textarea"
                            :rows="4"
                            style="max-width:400px;"
                        />
                    </el-form-item>
                </el-form>
            </div>


            <div class="step-wrap pb30">
                <span class="step">2</span>
                <h3 class="mb20">数据资源</h3>
                <el-form>
                    <el-form-item
                        label="样本类型："
                        label-width="100px"
                    >
                        <div>
                            <el-radio
                                v-model="task.data_resource_type"
                                label="DataSet"
                                @change="task.data_resource_id='',
                                         task.data_resource_name='',
                                         task.row_count='',
                                         fieldInfoList=[],
                                         dataSetList=[],
                                         bloomFilterList=[]"
                            >
                                数据集
                            </el-radio>
                            <el-radio
                                v-model="task.data_resource_type"
                                label="BloomFilter"
                                @change="task.data_resource_id='',
                                         task.data_resource_name='',
                                         task.row_count='',
                                         fieldInfoList=[],
                                         dataSetList=[],
                                         bloomFilterList=[]"
                            >
                                布隆过滤器
                            </el-radio>
                        </div>
                    </el-form-item>


                    <el-form-item
                        label="对齐样本："
                        label-width="100px"
                        required
                    >
                        <el-button
                            @click="task.data_resource_type =='DataSet'?addDataSet():addBloomFilter()"
                        >
                            + 选择样本
                        </el-button>
                    </el-form-item>

                    <el-table
                        v-if="dataSetList.length>0"
                        :data="dataSetList"
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


                    <ul class="members mb30">
                        <li class="mt20">
                            <el-form-item
                                v-if="task.data_resource_type === 'DataSet' && dataSetList.length"
                                label="设置主键："
                                label-width="100px"
                                required
                            >
                                <el-button
                                    :disabled="fieldInfoList.length > 4"
                                    @click="addFieldInfo"
                                >
                                    +  添加主键
                                </el-button>
                            </el-form-item>

                            <el-form-item
                                v-for="(item, index) in fieldInfoList"
                                :key="index"
                                label-width="15px"
                            >
                                <i
                                    class="id"
                                    style="margin-left : 5px"
                                >
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
                                </i>

                                <i
                                    class="id"
                                    style="margin-left : 5px"
                                >
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
                                </i>


                                <i
                                    v-if="item.options=='SUBSTRING'"
                                    style="margin-left : 5px"
                                >

                                    <el-input
                                        v-model="item.frist_index"
                                        style="max-width:50px;"
                                        oninput="value=value.replace(/[^\d]/g,'')"
                                    />
                                    ~
                                    <el-input
                                        v-model="item.end_index"
                                        style="max-width:50px;"
                                        oninput="value=value.replace(/[^\d]/g,'')"
                                    />
                                </i>
                                <i
                                    v-if="fieldInfoList.length > 0"
                                    style="margin-left : 5px"
                                    class="icon-operator el-icon-remove-outline"
                                    @click="removeFieldInfo({$index: index })"
                                />
                            </el-form-item>


                            <el-form-item
                                v-if="keyRes"
                                :key="index"
                                label-width="15px"
                            >
                                <el-alert
                                    :closable="false"
                                    type="success"
                                >
                                    主键生成规则:   {{ keyRes }}
                                </el-alert>
                            </el-form-item>
                        </li>
                    </ul>

                    <el-form-item
                        v-if="task.data_resource_type =='DataSet' && dataSetList.length>0"
                        label-width="100px"
                        label="是否追溯："
                        required
                    >
                        <div>
                            <el-radio-group v-model="task.is_trace">
                                <el-radio
                                    :label="true"
                                    @change="task.trace_column=''"
                                >
                                    是
                                </el-radio>
                                <el-radio
                                    :label="false"
                                    @change="task.trace_column=''"
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
                        <div>
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
                        </div>
                    </el-form-item>
                </el-form>
            </div>


            <div class="step-wrap pb30">
                <span class="step">3</span>
                <h3 class="mb20">合作方</h3>
                <el-form>
                    <el-form-item
                        label-width="20px"
                        required
                    >
                        <el-button
                            @click="addPartner()"
                        >
                            + 选择合作伙伴
                        </el-button>
                    </el-form-item>


                    <el-table
                        v-if="partnerList.length>0"
                        :data="partnerList"
                        stripe
                        border
                    >
                        <el-table-column
                            label="合作伙伴 / Id"
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
            @click="addTask"
        >
            保存
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
        <SelectPartnerDialog
            ref="SelectPartnerDialog"
            @selectPartner="selectPartner"
        />
    </el-card>
</template>

<script>
    import DataSetPreview from '@src/components/views/data-set-preview';
    import SelectDatasetDialog from '@comp/views/select-data-set-dialog';
    import SelectBloomFilterDialog from '@comp/views/select-bloom-filter-dialog';
    import SelectPartnerDialog from '@comp/views/select-partner-dialog';

    export default {
        components: {
            DataSetPreview,
            SelectDatasetDialog,
            SelectBloomFilterDialog,
            SelectPartnerDialog,
        },
        data() {
            return {
                bloom_filter_display: false,

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
                   data_resource_type:  'DataSet',
                   row_count:           '',
                   created_time:        '',
                   psi_actuator_role:   '',
                   description:         '',
                   is_trace:            false,
                   trace_column:        '',
                },

                column: {
                   key:  '',
                   type: '',
                },

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
                partnerList:     [],

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
                }],

                columnList:                   [],
                show_data_set_preview_dialog: false,

                fieldInfo: {
                    columns:     '',
                    options:     '',
                    frist_index: '',
                    end_index:   '',
                },

                fieldInfoList: [],

                field_info_display: false,
                keyRes:             '',
            };
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
                       this.bloom_filter_display =true;
                    }
                }
            },

            async getDataSet () {
                const { code, data } = await this.$http.get(
                       '/data_set/query',{
                     },
                );

                if (code === 0) {
                    this.dataSetList = data.list;
                }
            },

            async getBloomFilter () {
                const { code, data } = await this.$http.get(
                       '/filter/query',{
                     },
                );

                if (code === 0) {
                    this.bloomFilterList = data.list;
                }
            },

            async addTask () {
                this.fieldInfoList.forEach((item, index) => {
                    item.columns=item.column_arr.join(',');
                });

                const { code } = await this.$http.post({
                    url:  '/task/add',
                    data: {
                        name:               this.task.name,
                        partner_member_id:  this.task.partner_member_id,
                        data_resource_id:   this.task.data_resource_id,
                        data_resource_type: this.task.data_resource_type,
                        field_info_list:    this.fieldInfoList,
                        row_count:          this.task.row_count,
                        description:        this.task.description,
                        is_trace:           this.task.is_trace,
                        trace_column:       this.task.trace_column,
                    },
                });

                if (code === 0) {
                    this.$message('发起成功!');

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

            addBloomFilter() {
                const ref = this.$refs['SelectBloomFilterDialog'];

                ref.show = true;
                ref.loadDataList();
            },

            addPartner() {
                const ref = this.$refs['SelectPartnerDialog'];

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

            selectBloomFilter(item) {
                this.dataSetList = [];
                this.bloomFilterList=[item];

                this.task.data_resource_id=item.id;
                this.task.data_resource_type=item.type;
                this.task.data_resource_name = item.name;
                this.task.row_count = item.row_count;
            },

            selectPartner(item) {
                this.task.partner_member_id=item.partner_member_id;
                this.task.partner_member_name=item.name;

                const partner = {
                    partner_member_id:   item.partner_member_id,
                    partner_member_name: item.name,
                    base_url:            item.base_url,
                };

                this.partnerList = [];
                this.partnerList.push(partner);
            },

            async addFieldInfo () {
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
                    } else {
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
    .save-btn {width: 100px;}
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
    .el-form{
        ::v-deep .el-form-item__label{
            font-weight: bold;
        }
    }
    .service-online{
        color: $color-success;
        cursor: pointer;
    }
    .service-offline{
        color: $color-danger;
        cursor: pointer;
    }
    .el-icon-remove-outline{
        color: $color-danger;
        margin-left: 10px;
        font-size:14px;
        cursor: pointer;
    }
</style>
