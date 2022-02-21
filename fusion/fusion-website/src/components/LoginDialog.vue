<template>
    <el-dialog
        v-loading="loading"
        :visible.sync="show"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        width="500px"
        title="登录"
        center
    >
        <el-form
            :model="form"
            class="login-form"
            label-width="100px"
            @submit.native.prevent
        >
            <el-form-item label="手机号">
                <el-input v-model="form.phone_number" />
            </el-form-item>
            <el-form-item label="密码">
                <el-input
                    v-model="form.password"
                    type="password"
                />
            </el-form-item>
            <el-form-item label="验证码">
                <el-input
                    v-model="form.code"
                    class="form-code"
                    maxlength="10"
                    clearable
                >
                    <template slot="append">
                        <div
                            class="code-img"
                            @click="getImgCode"
                        >
                            <img
                                v-show="imgCode"
                                class="code-img"
                                :src="imgCode"
                            >
                        </div>
                    </template>
                </el-input>
            </el-form-item>
            <div class="text-c">
                <el-button
                    type="primary"
                    class="login-btn"
                    @click="login"
                >
                    登 录
                </el-button>
                <el-button
                    type="text"
                    @click="register"
                >
                    注 册
                </el-button>
            </div>
        </el-form>
    </el-dialog>
</template>

<script>
import md5 from 'js-md5';

export default {
    inject: ['refresh'],
    data() {
        return {
            loading: false,
            show:    false,
            form:    {
                phone_number: '',
                password:     '',
                code:         '',
                key:          '',
            },
            imgCode: '',
        };
    },
    created () {
        this.$bus.$on('show-login-dialog', () => {
            this.show = true;
            this.getImgCode();
        });

    },
    methods: {
        async getImgCode() {
            const { code, data } = await this.$http.get('/account/captcha');

            if (code === 0) {
                this.imgCode = data.image;
                this.form.key = data.key;
            }
        },
        async login() {
            if(!this.form.code) return this.$message.error('请输入验证码!');
            if(this.loading) return;

            this.loading = true;

            const password = [
                this.form.phone_number,
                this.form.password,
                this.form.phone_number,
                this.form.phone_number.substr(0, 3),
                this.form.password.substr(this.form.password.length - 3),
            ].join('');

            const { code, data } = await this.$http.post({
                url:  '/account/login',
                data: {
                    phone_number: this.form.phone_number,
                    password:     md5(password),
                    key:          this.form.key,
                    code:         this.form.code,
                },
            });

           if (code === 0) {
                this.$store.commit('UPDATE_USERINFO', data);
                window.$app.$message.success('登录成功');
                this.show = false;
            } else {
                this.getImgCode();
            }
            this.loading = false;
        },
        register() {
            this.$router.push({ name: 'register' });
        },
    },
};
</script>

<style lang="scss" scoped>
.form-code{
    ::v-deep .el-input-group__append{
        padding:0;
        width: 85px;
        overflow: hidden;
    }
}
.code-img{
    width: 85px;
    height: 30px;
}
.login-form ::v-deep .el-input {width: 80%;}
.login-form ::v-deep .login-btn {
    width: 100px;
    display: block;
    margin: 20px auto 10px;
    font-size: 14px;
}
.el-button + .el-button{margin-left:0;}
</style>
