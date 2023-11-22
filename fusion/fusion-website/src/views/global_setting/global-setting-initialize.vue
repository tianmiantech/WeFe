<template>
    <div class="page register-wrapper">
        <div class="sign-box">
            <div class="logo">
                <img src="../../assets/images/favicon.png">
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
                    label="联邦成员名称："
                    required
                >
                    <el-input
                        v-model.trim="form.member_name"
                        maxlength="12"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="邮箱：">
                    <el-input
                        v-model.trim="form.member_email"
                        placeholder="hello@world.com"
                        maxlength="40"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="联系电话：">
                    <el-input
                        v-model.trim="form.member_mobile"
                        clearable
                    />
                </el-form-item>
                <el-divider />
                <el-button
                    round
                    size="middle"
                    type="primary"
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
    import { mapGetters } from 'vuex';
    import { EMAILREG } from '@js/const/reg';
    import { baseLogout } from '@src/router/auth';

    export default {
        data() {
            return {
                loading: false,
                form:    {
                    member_name:   '',
                    member_email:  '',
                    member_mobile: '',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.systemStatusCheck();
        },
        methods: {
            async systemStatusCheck() {
                if(this.loading) return;
                this.loading = true;

                const { code, data } = await this.$http.get({
                    url: '/global_setting/is_initialize',
                });

                this.loading = false;
                if(code === 0) {
                    if(data.initialized) {
                        if(this.userInfo.id) {
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
                            url:  '/system/initialize',
                            data: this.form,
                        });

                        if (code === 0) {
                            this.$store.commit('SYSTEM_INITED', true);
                            this.$router.replace({
                                name: 'index',
                            });
                            this.$message.success('欢迎来到 WeFe-fusion! ');
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
