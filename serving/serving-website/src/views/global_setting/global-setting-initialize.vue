<template>
    <div
        v-loading="pageLoading"
        class="page register-wrapper"
    >
        <div class="sign-box">
            <div class="logo">
                <img src="../../assets/images/x-logo.png">
            </div>
            <h4
                class="sign-title mt10"
                align="center"
            >
                初始化系统
            </h4>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.native.prevent
            >
                <el-form-item
                    label="名称："
                    label-width="65px"
                    required
                >
                    <el-input
                        v-model="form.member_name"
                        maxlength="20"
                        clearable
                    />
                </el-form-item>
                <!--                <el-form-item-->
                <!--                    label="名称："-->
                <!--                    required-->
                <!--                >-->
                <!--                    <el-input-->
                <!--                        v-model.trim="form.member_name"-->
                <!--                        maxlength="30"-->
                <!--                        clearable-->
                <!--                    />-->
                <!--                </el-form-item>-->

                <!--                <el-form-item-->
                <!--                    label="联邦成员名称："-->
                <!--                    required-->
                <!--                >-->
                <!--                    <el-input-->
                <!--                        v-model.trim="form.member_name"-->
                <!--                        maxlength="12"-->
                <!--                        clearable-->
                <!--                    />-->
                <!--                </el-form-item>-->
                <!--                <el-form-item-->
                <!--                    label="私钥："-->
                <!--                    required-->
                <!--                >-->
                <!--                    <el-input-->
                <!--                        v-model.trim="form.rsa_private_key"-->
                <!--                        type="textarea"-->
                <!--                        placeholder="请填写borad生成的秘钥，否则无法使用"-->
                <!--                        clearable-->
                <!--                    />-->
                <!--                </el-form-item>-->
                <!--                <el-form-item-->
                <!--                    label="公钥："-->
                <!--                    required-->
                <!--                >-->
                <!--                    <el-input-->
                <!--                        v-model.trim="form.rsa_public_key"-->
                <!--                        type="textarea"-->
                <!--                        placeholder="请填写borad生成的公钥，否则无法使用"-->
                <!--                        clearable-->
                <!--                    />-->
                <!--                </el-form-item>-->

                <!--                <el-divider />-->
                <el-button

                    type="primary"
                    size="middle"
                    class="btn-submit ml10"

                    @click="confirmInit"
                >
                    确认
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
            pageLoading: false,
        };
    },
    computed: {
        ...mapGetters(['userInfo']),
    },
    created() {
        this.systemStatusCheck();
    },
    methods: {
        confirmInit() {
            this.$alert('是否将serving系统初始化为独立模式(独立模式无法创建联邦学习模型服务！)，如需联邦模式请由board系统对本serving系统初始化', '确认', {
                confirmButtonText: '确定',
                callback:          action => {

                    if (action === 'confirm') {
                        if (this.form.member_name !== '') {
                            this.submit();
                            // this.$message({
                            //     type: 'info',
                            //     message: '初始化成功'
                            // });
                        }else{
                            this.$message({
                                type:    'error',
                                message: '请填写名称',
                            });
                        }
                    }
                },
            });
        },

        emailFormat(rule, value, callback) {
            if (EMAILREG.test(value)) {
                callback();
            } else {
                callback(false);
            }
        },

        async systemStatusCheck() {
            if (this.loading) return;
            this.loading = true;

            const { code, data } = await this.$http.get({
                url: '/global_config/is_initialize',
            });

            this.loading = false;
            if (code === 0) {
                if (data.initialized) {
                    if (this.userInfo.id) {
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
            // if (this.loading) return;
            this.$refs['sign-form'].validate(async valid => {
                if (valid) {
                    this.loading = true;
                    this.pageLoading = true;
                    const { code } = await this.$http.post({
                        url:  '/global_config/initialize',
                        data: this.form,
                    });

                    this.loading = false;
                    if (code === 0) {
                        this.$store.commit('SYSTEM_INITED', true);
                        this.$router.replace({
                            name: 'index',
                        });
                        this.$message.success('欢迎来到 WeFe-serving! ');
                    }
                    this.pageLoading = false;
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
