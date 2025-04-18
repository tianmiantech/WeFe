<template>
    <el-dialog
        v-model="show"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        destroy-on-close
        append-to-body
        width="400px"
        title="登录"
        center
    >
        <el-form
            v-loading="loading"
            class="login-form"
            :model="form"
            @submit.prevent
        >
            <el-form-item label="手机号" prop="phone_number">
                <el-input v-model="form.phone_number" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input
                    v-model="form.password"
                    type="password"
                    @paste.prevent
                    @copy.prevent
                    @contextmenu.prevent
                />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
                <el-input
                    v-model="form.code"
                    placeholder="验证码"
                    class="form-code"
                    maxlength="10"
                    clearable
                >
                    <template v-slot:append>
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
                    native-type="submit"
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
    import { mapGetters } from 'vuex';
    import { clearUserInfo } from '../router/auth';

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
        computed: {
            ...mapGetters(['userInfo']),
        },
        created () {
            this.$bus.$on('show-login-dialog', () => {
                this.form.code = '';
                this.show = true;
                this.getImgCode();
                clearUserInfo();
            });
        },
        methods: {
            async getImgCode() {
                const { code, data } = await this.$http.get('/account/captcha');

                if (code === 0) {
                    this.imgCode = data.image;
                    this.form.key = data.key;
                    this.form.code = '';
                }
            },

            async login($event) {
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
                    btnState: {
                        target: $event,
                    },
                });

                if (code === 0) {
                    this.show = false;
                    this.$store.commit('UPDATE_USERINFO', {
                        ...this.userInfo,
                        ...data,
                    });

                    if(data.need_update_password) {
                        this.$message.success('密码等级太弱需修改密码!');
                        this.$router.replace({
                            name: 'change-password',
                        });
                    } else if(this.$route.meta.loginAndRefresh) {
                        this.$message.success('登录成功');
                        // login and refresh whole page
                        this.refresh();
                        this.$bus.$emit('loginAndRefresh'); // notice other components
                    }
                } else {
                    this.getImgCode();
                }
            },

            register() {
                this.$router.push({ name: 'register' });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .manager-dialog__wrapper{
        :deep(.manager-dialog){
            min-width: 360px;
            max-width: 460px;
        }
    }
    .form-code{
        :deep(.manager-input-group__append){
            padding:0;
            width: 90px;
            overflow: hidden;
        }
    }
    .code-img{
        width: 90px;
        height: 30px;
        cursor: pointer;
    }
    .login-form :deep(.manager-input) {width: 90%;}
    .login-form :deep(.login-btn) {
        width: 100px;
        display: block;
        margin: 20px auto 10px;
        font-size: 14px;
    }
    .login-form{
        .manager-form-item{display: flex;}
        :deep(.manager-form-item__label) {width: 70px;}
        :deep(.manager-form-item__content) {flex: 1;}
    }
    .manager-button + .manager-button{margin-left:0;}
</style>
