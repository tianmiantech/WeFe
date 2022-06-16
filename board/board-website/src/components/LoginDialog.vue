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
            <el-form-item label="手机号">
                <el-input v-model="form.phone_number" />
            </el-form-item>
            <el-form-item label="密码">
                <el-input
                    v-model="form.password"
                    type="password"
                    @paste.prevent
                    @copy.prevent
                    @contextmenu.prevent
                />
            </el-form-item>
            <el-form-item label="验证码">
                <el-input
                    v-model="form.code"
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
                if(!this.show) {
                    // hide the chat room
                    window.localStorage.setItem(`${window.api.baseUrl}_chat`, 'disconnect');
                    this.$store.commit('SYSTEM_INITED', false);
                    this.$store.commit('UPDATE_USERINFO', {});
                    this.form.code = '';
                    this.show = true;
                    this.getImgCode();
                    clearUserInfo();
                }
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
                    this.show = false;
                    this.$store.commit('UPDATE_USERINFO', {
                        ...this.userInfo,
                        ...data,
                    });
                    this.$store.commit('SYSTEM_INITED', true);
                    this.$store.commit('UI_CONFIG', data.ui_config);
                    this.$message.success('登录成功');

                    const res = await this.$http.get({
                        url: '/member/detail',
                    });

                    if(res.code === 0){
                        const info = Object.assign(data, res.data);

                        this.$store.commit('UPDATE_USERINFO', info);
                    }
                    // login and refresh whole page
                    if(this.$route.meta.loginAndRefresh) {
                        this.refresh();
                        this.$bus.$emit('loginAndRefresh'); // notice other components
                    }
                    this.checkEnv();
                    this.getUserList();
                } else {
                    this.getImgCode();
                }
                this.loading = false;
            },
            async checkEnv() {
                const { code, data } = await this.$http.get('/env');

                if (code === 0) {
                    const is_demo = data.env_properties.is_demo === 'true';

                    this.$store.commit('IS_DEMO', is_demo);
                } else {
                    this.$store.commit('IS_DEMO', false);
                }
            },
            async getUserList() {
                const { code, data } = await this.$http.post({
                    url:  '/account/query',
                    data: {
                        phone_number: '',
                        nickname:     '',
                        audit_status: '',
                        page_index:   '',
                        page_size:    '',
                    },
                });

                if (code === 0 && data) {
                    let admin_list = [];

                    if (data.list && data.list.length) {
                        admin_list = data.list.filter(item => item.admin_role);
                    }
                    this.$store.commit('ADMIN_USER_LIST', admin_list);
                }
            },

            register() {
                this.$router.push({ name: 'register' });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-dialog__wrapper{
        :deep(.el-dialog){
            min-width: 360px;
            max-width: 460px;
        }
    }
    .form-code{
        :deep(.el-input-group__append){
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
    .login-form :deep(.el-input) {width: 90%;}
    .login-form :deep(.login-btn) {
        width: 100px;
        display: block;
        margin: 20px auto 10px;
        font-size: 14px;
    }
    .login-form{
        .el-form-item{display: flex;}
        :deep(.el-form-item__label) {width: 70px;}
        :deep(.el-form-item__content) {flex: 1;}
    }
    .el-button + .el-button{margin-left:0;}
</style>
