<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-form-item
                prop="name"
                label="源名称:"
                :rules="[
                    { required: true, message: '源名称必填!' }
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
                label="数据库类型:"
                prop="databaseType"
                :rules="[
                    { required: true, message: '数据库类型必填!' }
                ]"
            >
                <el-select
                    v-model="form.databaseType"
                    size="medium"
                    clearable
                >
                    <el-option
                        v-for="item in DatabaseTypeList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.name"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                prop="host"
                label="Host"
                :rules="[{ required: true, message: 'Host必填！' }]"
            >
                <el-input
                    v-model="form.host"
                    size="medium"
                />
            </el-form-item>
            <el-form-item
                prop="port"
                label="Port"
                :rules="[{ required: true, message: 'Port必填！' }]"
            >
                <el-input
                    v-model="form.port"
                    size="medium"
                />
            </el-form-item>
            <el-form-item
                prop="databaseName"
                label="目标数据库名"
                :rules="[{ required: true, message: '数据库名必填！' }]"
            >
                <el-input
                    v-model="form.databaseName"
                    size="medium"
                />
            </el-form-item>
            <el-form-item
                label="数据库用户名"
                prop="userName"
                :rules="[
                    { required: true, message: '用户名必填！' }
                ]"
            >
                <el-input
                    v-model="form.userName"
                    size="medium"
                />
            </el-form-item>
            <div class="form-inline">
                <el-form-item
                    prop="password"
                    label="密码"
                    :rules="[
                        { required: true, message: '密码必填！' }
                    ]"
                >
                    <el-input
                        v-model="form.password"
                        size="medium"
                    />
                </el-form-item>
                <el-button
                    :loading="testLoading"
                    size="small"
                    @click="testConnection"
                >
                    测试连接
                </el-button>
                </el-col>
            </div>
            <el-button
                class="save-btn mt20"
                type="primary"
                size="medium"
                :loading="saveLoading"
                @click="add(currentItem.id)"
            >
                保存
            </el-button>
        </el-form>
    </el-card>
</template>

<script>
    export default {
        data() {
            return {
                loading: false,
                // model
                form:    {
                    publicLevel:  'Public',
                    name:         '',
                    host:         '',
                    port:         '',
                    databaseName: '',
                    userName:     '',
                    password:     '',
                    databaseType: '',
                },
                DatabaseTypeList: [{
                    name:  'MySql',
                    value: 'MySql',
                }, {
                    name:  'Hive',
                    value: 'Hive',
                }, {
                    name:  'Impala',
                    value: 'Impala',
                }],
                currentItem: {},
                testLoading: false,
                saveLoading: false,
            };
        },
        created() {
            this.currentItem.id = this.$route.query.id;
            this.currentItem.name = this.$route.query.name;
            if (this.currentItem.id) {
                this.getSqlConfigDetail();
            }
        },

        methods: {

            async getSqlConfigDetail() {
                const { code, data } = await this.$http.post({
                    url:  '/data_source/query',
                    data: { id: this.currentItem.id, name: this.currentItem.name },
                });

                if (code === 0) {
                    console.log(data);
                    if (data.list) {
                        const resData = data.list[0];

                        this.form = resData;
                        this.form.databaseType = resData.database_type;
                        this.form.databaseName = resData.database_name;
                        this.form.userName = resData.user_name;
                    }
                }
            },
            async add(id = '') {
                if (!this.form.name || !this.form.databaseType || !this.form.host || !this.form.port || !this.form.userName || !this.form.password || !this.form.databaseName) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                } else if (this.form.name.length < 4) {
                    this.$message.error('源名称长度不能少于4，不能大于30');
                    return;
                }

                this.saveLoading = true;
                const { code } = await this.$http.post({
                    url:     id ? '/data_source/update' : '/data_source/add',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    this.form,
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.$router.replace({ name: 'data-resouce-list', query: {} });

                }
                this.saveLoading = false;
            },
            async testConnection() {
                if (!this.form.name || !this.form.databaseType || !this.form.host || !this.form.port || !this.form.userName || !this.form.password || !this.form.databaseName) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                } else if (this.form.name.length < 4) {
                    this.$message.error('源名称长度不能少于4，不能大于30');
                    return;
                }
                this.testLoading = true;
                const { code, data } = await this.$http.post({
                    url:     '/data_source/test_db_connect',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    this.form,

                });

                if (code === 0) {
                    this.$message.success('数据库可连接!');
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
