<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-form-item label="Parnter Id：">
                {{ form.partner_id }}
            </el-form-item>
            <el-form-item
                :rules="[{required: true, message: '名称必填!'}]"
                label="名称："
            >
                <el-input
                    v-model="form.partner_name"
                    placeholder="仅支持中文名称"
                    :disabled="is_update"
                />
            </el-form-item>

            <el-form-item
                v-if="is_display"
                label="私钥："
            >
                <el-input
                    v-model="form.rsa_private_key"
                    :disabled="is_update"
                    type="textarea"
                    autosize
                />
            </el-form-item>

            <el-form-item
                label="公钥："
            >
                <el-input
                    v-model="form.rsa_public_key"
                    type="textarea"
                    :disabled="is_update"
                    autosize
                />
            </el-form-item>

            <el-button
                v-loading="loading"
                class="save-btn"
                type="primary"
                size="medium"
                :disabled="is_update"
                @click="update"
            >
                更新
            </el-button>
        </el-form>
    </el-card>
</template>

<script>

    export default {
        data() {
            return {
                loading:    false,
                is_update:  true,
                is_display: false,
                // model
                form:       {
                    partner_id:      '',
                    partner_name:    '',
                    rsa_private_key: '',
                    rsa_public_key:  '',
                },

            };
        },
        created() {
            this.getData();
        },
        methods: {
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url: '/system/global_setting/detail',
                });

                if (code === 0) {
                    this.form = data;
                }
                this.loading = false;
            },
            async update() {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/global_setting/update',
                    data: this.form,
                });

                if (code === 0) {
                    this.$message.success('保存成功!');
                }
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form {width: 400px;}
</style>
