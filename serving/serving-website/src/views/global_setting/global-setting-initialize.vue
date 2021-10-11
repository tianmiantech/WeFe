<template>
    <div class="page register-wrapper">
        <div class="sign-box">
            <div class="logo">
                <img src="../../assets/images/x-logo.png">
            </div>
            <h4 class="sign-title">初始化系统 · 配置系统变量</h4>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.native.prevent
            >
                <el-form-item
                    label="联邦成员id："
                    required="true"
                >
                    <el-input
                        v-model.trim="form.member_id"
                        maxlength="40"
                        clearable
                    />
                </el-form-item>

                <el-form-item
                    label="联邦成员名称："
                    required="true"
                >
                    <el-input
                        v-model.trim="form.member_name"
                        placeholder="仅支持中文"
                        maxlength="12"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    label="私钥："
                    required="true"
                >
                    <el-input
                        v-model.trim="form.rsa_private_key"
                        type="textarea"
                        placeholder="请填写borad生成的秘钥，否则无法使用"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    label="公钥："
                    required="true"
                >
                    <el-input
                        v-model.trim="form.rsa_public_key"
                        type="textarea"
                        placeholder="请填写borad生成的公钥，否则无法使用"
                        clearable
                    />
                </el-form-item>

                <el-divider />
                <el-button
                    round
                    type="primary"
                    size="middle"
                    class="btn-submit ml10"
                    @click="submit"
                >
                    提交
                </el-button>
            </el-form>
        </div>
    </div>
</template>

<script>
import { EMAILREG } from '@js/const/reg';

export default {
    data() {
        return {
            form: {
                member_id:       '',
                member_name:     '',
                rsa_private_key: '',
                rsa_public_key:  '',
            },
        };
    },
    methods: {
        emailFormat(rule, value, callback) {
            if (EMAILREG.test(value)) {
                callback();
            } else {
                callback(false);
            }
        },
        submit() {
            this.$refs['sign-form'].validate(async valid => {
                if (valid) {
                    const { code } = await this.$http.post({
                        url:  '/global_setting/initialize',
                        data: this.form,
                    });

                    if (code === 0) {
                        this.$router.replace({
                            name: 'index',
                        });
                        this.$message.success('欢迎来到 WeFe-serving! ');
                    }
                }
            });
        },
    },
};
</script>

<style lang="scss" scoped>
@import "../sign/sign.scss";
.register-wrapper {
    overflow: hidden;
    min-height: 100vh;
    background: linear-gradient(90deg, #434343 0, #000);
}

.sign-box {
    padding-top: 0;
    margin-top: 170px;
    background: #fff;
    border-radius: 3px;
    padding: 20px;
}
.btn-submit {
    display: block;
    margin: 0 auto;
}
</style>
