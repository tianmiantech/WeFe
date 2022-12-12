<template>
    <el-dialog
        :visible.sync="visible"
        :title="`${ id ? '编辑' : '新增' }数据源`"
    >
        <el-form
            ref="form"
            :model="form"
        >
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
                prop="database_type"
                :rules="[
                    { required: true, message: '数据库类型必填!' }
                ]"
            >
                <el-select
                    v-model="form.database_type"
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
                prop="database_name"
                label="目标数据库名"
                :rules="[{ required: true, message: '数据库名必填！' }]"
            >
                <el-input
                    v-model="form.database_name"
                    size="medium"
                />
            </el-form-item>
            <el-form-item
                label="数据库用户名"
                prop="user_name"
                :rules="[
                    { required: true, message: '用户名必填！' }
                ]"
            >
                <el-input
                    v-model="form.user_name"
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
                        type="password"
                        size="medium"
                        @paste.native.prevent
                        @copy.native.prevent
                        @contextmenu.native.prevent
                    >
                        <template slot="append">
                            <el-button
                                size="small"
                                @click="testConnection"
                            >
                                测试连接
                            </el-button>
                        </template>
                    </el-input>
                </el-form-item>
            </div>
            <div class="mt20 text-r">
                <el-button
                    class="save-btn"
                    @click="visible = false;"
                >
                    取消
                </el-button>
                <el-button
                    class="save-btn"
                    type="primary"
                    @click="save"
                >
                    保存
                </el-button>
            </div>
        </el-form>
    </el-dialog>
</template>

<script>
    export default {
        props: {
            id: String,
        },
        data() {
            return {
                loading:          false,
                visible:          false,
                // model
                form:             {},
                DatabaseTypeList: [{
                    name:  'MySql',
                    value: 'MySql',
                }],
            };
        },

        methods: {
            show(row) {
                this.visible = true;
                this.$nextTick(_ => {
                    this.form = row || {};
                    this.$refs['form'].resetFields();
                });
            },
            async save() {
                if (!this.form.name || !this.form.database_type || !this.form.host || !this.form.port || !this.form.user_name || !this.form.password || !this.form.database_name) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                } else if (this.form.name.length < 4) {
                    this.$message.error('源名称长度不能少于4，不能大于30');
                    return;
                }

                const { code } = await this.$http.post({
                    url:     this.id ? '/data_source/update' : '/data_source/add',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    this.form,
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.$emit('data-source-add');
                    this.visible = false;
                }
            },
            async testConnection(event) {
                if (!this.form.name || !this.form.database_type || !this.form.host || !this.form.port || !this.form.user_name || !this.form.password || !this.form.database_name) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                } else if (this.form.name.length < 4) {
                    this.$message.error('源名称长度不能少于4，不能大于30');
                    return;
                }

                const { code } = await this.$http.post({
                    url:      '/data_source/test_db_connect',
                    timeout:  1000 * 60 * 24 * 30,
                    data:     this.form,
                    btnState: {
                        target: event,
                    },
                });

                if (code === 0) {
                    this.$message.success('数据库可连接!');
                }
            },

        },
    };
</script>

<style lang="scss" scoped>
    .el-form{
        .el-form-item{
            margin-bottom: 10px;
        }
    }
    .save-btn {
        width: 100px;
    }
</style>
