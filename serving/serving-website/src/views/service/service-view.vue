<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-form-item
                prop="name"
                label="服务名称:"
                :rules="[
                    { required: true, message: '服务名称必填!' }
                ]"
            >
                <el-input
                    v-model="form.name"
                    :maxlength="30"
                    :minlength="4"
                    show-word-limit
                    size="medium"
                />
            </el-form-item>

            <el-form-item
                prop="url"
                label="服务地址:"
                :rules="[
                    { required: true, message: '服务地址必填!' }
                ]"
            >
                <el-input
                    v-model="form.url"
                    :maxlength="100"
                    :minlength="4"
                    show-word-limit
                    size="medium"
                />
            </el-form-item>

            <el-form-item
                label="服务类型:"
                :rules="[
                    { required: true, message: '服务类型必填!' }
                ]"
            >
                <el-select
                    v-model="form.service_type"
                    size="medium"
                    clearable
                >
                    <el-option
                        v-for="item in ServiceTypeList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
                    />
                </el-select>
            </el-form-item>
            查询参数配置：
            <el-form-item  v-for="(item, index) in query_param_arr"
                  :key="`query_param_arr-${index}`"
                   label="参数名称"
                   label-width="100px"
            >
                <el-input v-model="item.key" />
                <el-button type="primary" size="mini" @click="delete_params(index)">删除</el-button>
            </el-form-item>

            <el-button
                        type="primary"
                        @click="add_params()"
                    >新增参数</el-button>
            <br/><br/><br/>
            SQL 配置：
            <el-form-item  v-for="(item, index) in data_source"
                :key="`data_source-${index}`"
            >
            数据源:
                <el-input v-model="item.db" />
            数据表:
                <el-input v-model="item.table" />
            返回字段:
                <el-form-item  v-for="(return_field, index1) in item.return_fields"
                  :key="`return_field-${index1}`"
                   label-width="100px"
                >
                    <el-input v-model="return_field.name" />
                </el-form-item>
                <el-button
                        type="primary"
                        @click="add_return_fields(index)"
                    >新增返回字段</el-button>
            <br/>
            查询字段:
                <el-form-item  v-for="(condition_field, index2) in item.condition_fields"
                  :key="`condition_field-${index2}`"
                   label-width="100px"
                >
                    <el-input v-model="condition_field.operator" />
                    <el-input v-model="condition_field.field_on_table" />
                    <el-input v-model="condition_field.field_on_param" />
                </el-form-item>

            </el-form-item>

            <div class="form-inline">
                <el-button
                    :loading="testLoading"
                    size="small"
                    @click="sql_test.editor=true"
                >
                    SQL测试
                </el-button>
            </div>
            <el-dialog
                :visible.sync="sql_test.editor"
                title="SQL测试"
                width="500px"
            >
                <el-form>
                    参数输入 : 
                    <el-form-item  v-for="(item, index) in sql_test.params"
                          :key="`params-${index}`"
                           :label="item.key"
                           label-width="100px"
                    >
                        <el-input v-model="item.value" />
                    </el-form-item>
                    返回字段 : 
                    <el-form-item  v-for="(item, index) in sql_test.return_fields"
                          :key="`return_fields-${index}`"
                           :label="item.name"
                           label-width="100px"
                    >
                        <el-input v-model="item.value" />
                    </el-form-item>
                    
                </el-form>
                <span slot="footer">
                    <el-button @click="sql_test.editor=false">取消</el-button>
                    <el-button
                        type="primary"
                        @click="testConnection()"
                    >确定</el-button>
                </span>
            </el-dialog>

            <el-button
                class="save-btn mt20"
                type="primary"
                size="medium"
                :loading="saveLoading"
                @click="add(currentItem.id)"
            >
                保存
            </el-button>

            <el-button
                class="save-btn mt20"
                type="primary"
                size="medium"
                @click="export_sdk()"
            >
                SDK导出
            </el-button>
        </el-form>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    export default {
        computed: {
            ...mapGetters(['userInfo']),
        },
        data() {
            return {
                loading:          false,
                form:             {},
                ServiceTypeList: [{
                    name:  '匿踪查询',
                    value: 1,
                },
                {
                    name:  '交集查询',
                    value: 2,
                },
                {
                    name:  '安全聚合',
                    value: 3,
                }],
                data_source:[
                    {
                        "id":"",
                        "db":"",
                        "table":"",
                        "return_fields":[
                            {
                                "name":"",
                                "type":""
                            }
                        ],
                        "condition_fields":[
                            {
                                "field_on_param":"",
                                "field_on_table":"",
                                "operator":""
                            }
                        ]
                    }
                ],
                currentItem: {},
                testLoading: false,
                saveLoading: false,
                query_param_arr:[],
                sql_test: {
                    visible:    false,
                    editor:     false,
                    params:[],
                    params_json:{},
                    return_fields:[],
                },
            };
        },
        created() {
            this.currentItem.id = this.$route.query.id;
            if (this.currentItem.id) {
                this.getSqlConfigDetail();
            }
        },

        methods: {

            async getSqlConfigDetail() {
                const { code, data } = await this.$http.post({
                    url:  '/service/detail',
                    data: { id: this.currentItem.id},
                });

                if (code === 0) {
                    if (data) {
                        const resData = data;
                        this.form = resData;
                        this.query_param_arr = this.sql_test.params=this.form.query_params.split(",").map(x => {return {key: x,value:''}});
                        this.sql_test.return_fields = this.form.data_source[0].return_fields;
                        this.data_source = this.form.data_source;
                    }
                }
            },
            async add(id = '') {
                if (!this.form.name || !this.form.url || !this.form.service_type || !this.query_param_arr) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                }
                let a = []
                for(let j = 0; j < this.query_param_arr.length; j++) {
                    a[j] = this.query_param_arr[j].key;
                }
                this.form.query_params = a.join();
                this.saveLoading = true;
                const { code } = await this.$http.post({
                    url:     id ? '/service/update' : '/service/add',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    this.form,
                });

                if (code === 0) {
                    this.$message.success(JSON.stringify(this.form));
                    this.query_param_arr = this.sql_test.params=this.form.query_params.split(",").map(x => {return {key: x,value:''}});
                }
                this.saveLoading = false;
            },
            async add_params(){
                this.query_param_arr.push({key:"",value:""})
            },
            async add_return_fields(index){
                this.query_data_source();
                this.query_tables("f563de38a60e4be8aa5286af6db052c3");
                this.query_tables_fields("f563de38a60e4be8aa5286af6db052c3","account");
            },
            async query_data_source(){
                const { code, data } = await this.$http.post({
                    url:  '/data_source/query'
                });
                if (code === 0) {
                    console.log(data);
                }
                else{
                    this.$message.success('获取数据源失败');
                }
            },
            async query_tables(data_source_id){
                const { code, data } = await this.$http.post({
                    url:  '/data_source/query_table_fields',
                    data: { id: data_source_id},
                });

                if (code === 0) {
                    console.log(data);
                }
                else{
                    this.$message.success('获取数据表失败');
                }
            },
            async query_tables_fields(data_source_id, table_name){
                const { code, data } = await this.$http.post({
                    url:  '/data_source/query_table_fields',
                    data: { id: data_source_id,table_name:table_name},
                });

                if (code === 0) {
                    console.log(data);
                }
                else{
                    this.$message.success('获取数据表字段失败');
                }
            },
            async delete_params(index){
                this.query_param_arr.splice(index, 1);
            },
            async export_sdk(){
                const api = `${window.api.baseUrl}/service/export_sdk?serviceId=${this.currentItem.id}&token=${this.userInfo.token}`;
                const link = document.createElement('a');
                link.href = api;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
            },
            async testConnection() {
                if (!this.form.data_source || !this.sql_test.params) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                }
                this.testLoading = true;
                for(let j = 0; j < this.sql_test.params.length; j++) {
                    this.sql_test.params_json[this.sql_test.params[j].key] = this.sql_test.params[j].value;
                }
                let sql_test_data = {
                    params : this.sql_test.params_json,
                    data_source:this.form.data_source,
                }
                const { code, data } = await this.$http.post({
                    url:     '/service/sql_test',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    sql_test_data,

                });

                if (code === 0) {
                    for(let j = 0; j < this.sql_test.return_fields.length; j++) {
                        this.sql_test.return_fields[j].value = data.result[this.sql_test.return_fields[j].name];
                    }
                    this.$message.success('success');
                }
                this.testLoading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{
        max-width: 300px;
        ::v-deep .el-form-item__label{
            text-align: left;
            display: block;
            float: none;
        }
    }
    .form-inline{
        .el-form-item{
            display: inline-block;
            vertical-align: top;
            margin-right: 10px;
            width: 200px;
        }
        .el-button{
            margin-top: 33px;
        }
    }
    .save-btn {
        width: 100px;
    }
</style>
