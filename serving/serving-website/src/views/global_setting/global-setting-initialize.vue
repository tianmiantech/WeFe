<template>
    <div class="page register-wrapper">
        <div class="sign-box">
            <div class="logo">
                <img src="../../assets/images/logo.png">
            </div>
            <h4 class="sign-title mt10">初始化系统 · 配置系统变量</h4>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.native.prevent
            >
                <el-form-item
                    label="联邦成员id："
                    required
                >
                    <el-input
                        v-model.trim="form.member_id"
                        maxlength="40"
                        clearable
                    />
                </el-form-item>

                <el-form-item
                    label="联邦成员名称："
                    required
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
                    required
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
                    required
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
                    v-loading="loading"
                    type="primary"
                    size="middle"
                    class="btn-submit ml10"
                    round
                    @click="submit"
                >
                    提交
                </el-button>
            </el-form>
        </div>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
    import { EMAILREG } from '@js/const/reg';
    import { baseLogout } from '@src/router/auth';

    export default {
        data() {
            return {
                loading: false,
                form:    {
                    member_id:       '',
                    member_name:     '',
                    rsa_private_key: '',
                    rsa_public_key:  '',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            // this.systemStatusCheck();
        },
        methods: {
            emailFormat(rule, value, callback) {
                if (EMAILREG.test(value)) {
                    callback();
                } else {
                    callback(false);
                }
            },

            async systemStatusCheck() {
                if(this.loading) return;
                this.loading = true;

                const { code, data } = await this.$http.get({
                    url: '/setting/initialize',
                });

                this.loading = false;
                if(code === 0) {
                    if(data.initialized) {
                        if(this.userInfo.member_id) {
                            this.$store.commit('SYSTEM_INITED', true); // system inited
                            this.$router.replace({
                                name: 'index',
                            });
                        } else {
                            this.$message.success('请重新登录');
                            baseLogout();
                        }
                    }
                } else if (code === 10006) {
                    baseLogout();
                }
            },

            submit() {
                if(this.loading) return;
                this.$refs['sign-form'].validate(async valid => {
                    if (valid) {
                        this.loading = true;
                        const { code } = await this.$http.post({
                            url:  '/global_setting/initialize',
                            data: this.form,
                        });

                        this.loading = false;
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
