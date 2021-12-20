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

            <div class="form-inline">
                <el-form-item
                    prop="query_param"
                    label="参数名称"
                >
                    <el-input
                        v-model="form.query_param"
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
                currentItem: {},
                testLoading: false,
                saveLoading: false,
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
                    }
                }
            },
            async add(id = '') {
                if (!this.form.name || !this.form.database_type || !this.form.host || !this.form.port || !this.form.user_name || !this.form.password || !this.form.database_name) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                } else if (this.form.name.length < 4) {
                    this.$message.error('源名称长度不能少于4，不能大于30');
                    return;
                }

                this.saveLoading = true;
                const { code } = await this.$http.post({
                    url:     id ? '/service/update' : '/service/add',
                    timeout: 1000 * 60 * 24 * 30,
                    data:    this.form,
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.$router.replace({ name: 'service-list', query: {} });

                }
                this.saveLoading = false;
            },
            async testConnection() {
                if (!this.form.query_params || !this.form.data_source || !this.form.params) {
                    this.$message.error('请将必填项填写完整！');
                    return;
                }
                this.testLoading = true;
                const { code, data } = await this.$http.post({
                    url:     '/service/sql_test',
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
