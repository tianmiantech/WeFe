<template>
    <div class="page">
        <div class="sign-box">
            <div class="logo">
                <img src="@assets/images/x-logo.png">
            </div>
            <h2 class="sign-title mt20">找回密码</h2>
            <el-divider />
            <el-form
                ref="sign-form"
                :model="form"
                inline-message
                @submit.prevent
            >
                <el-form-item
                    label="注册时的手机号"
                    prop="phone"
                    :rules="phoneRules"
                >
                    <el-input
                        v-model="form.phone"
                        placeholder="注册时的手机号码"
                        maxlength="11"
                        type="tel"
                        clearable
                    />
                </el-form-item>
                <el-form-item
                    label="注册时的邮箱"
                    prop="email"
                    :rules="emailRules"
                >
                    <el-input
                        v-model="form.email"
                        placeholder="注册时的邮箱"
                        maxlength="60"
                        type="text"
                        clearable
                    />
                </el-form-item>
                <el-divider />
                <div class="sign-action">
                    <router-link class="float-left mt5" :to="{name: 'login'}">立即登录</router-link>
                    <el-button
                        type="primary"
                        class="ml10"
                        @click="submit"
                    >
                        提交
                    </el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                form: {
                    old_password: '',
                    new_password: '',
                },
                phoneRules: [
                    { required: true, message: '请输入你的手机号' },
                    {
                        validator: (rule, value, callback) => {
                            if (/^1[3-9]\d{9}/.test(value)) {
                                callback();
                            } else {
                                callback(new Error('请输入正确的手机号'));
                            }
                        },
                        trigger: 'blur',
                    },
                ],
                passwordRules: [
                    { required: true, message: '请输入你的用户名' },
                    { min: 4, message: '用户名最少4位' },
                ],
                emailRules: [
                    { required: true, message: '请输入注册时的邮箱' },
                ],
            };
        },
        methods: {
            submit() {
                this.$refs['sign-form'].validate(async valid => {
                    if(valid) {
                        const { code, message } = await this.$http.post({
                            url:  '/account/update_password',
                            data: this.form,
                        });

                        if(code === 0) {
                            this.$message.success('密码更新成功! 请重新登录!');
                            this.$store.commit('UPDATE_USERINFO', {});

                            this.$router.replace({
                                name: 'login',
                            });
                        } else {
                            this.$message.error(message);
                        }
                    } else {
                        this.$message.error(valid.message);
                    }
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    @import './sign.scss';
</style>
