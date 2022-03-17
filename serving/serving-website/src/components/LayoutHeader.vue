<template>
    <div class="layout-header">
        <div class="heading-bar">
            <span
                ref="heading-title"
                class="heading-title float-left"
                v-html="headingTitle || $route.meta.title"
            />
            <span class="heading-tools">
                <!-- 全屏显示 -->
                <el-tooltip
                    effect="light"
                    content="切换全屏"
                    placement="bottom"
                >
                    <i
                        class="el-icon-full-screen"
                        @click="fullScreenSwitch"
                    />
                </el-tooltip>
            </span>
            <div class="heading-user">
                你好,
                <el-dropdown @command="handleCommand">
                    <span class="el-dropdown-link">
                        <strong>{{ userInfo.nickname }}</strong>
                        <i class="el-icon-arrow-down el-icon--right" />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="logout">
                            <i class="el-icon-switch-button" />
                            注销
                        </el-dropdown-item>
                        <el-dropdown-item command="changepwd">
                            <i class="el-icon-edit" />
                            修改密码
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </div>
        </div>
        <el-dialog
            width="340px"
            title="修改密码"
            :visible.sync="changepwdDialog.visible"
            destroy-on-close
            append-to-body
        >
            <el-form
                ref="form"
                :model="form"
                style="max-width: 300px;"
                @submit.prevent
            >
                <el-form-item
                    label="旧密码"
                    prop="old_password"
                    :rules="old_password"
                >
                    <el-input
                        v-model="form.old_password"
                        type="password"
                    />
                </el-form-item>
                <el-form-item
                    label="新密码"
                    prop="new_password"
                    :rules="new_password"
                >
                    <el-input
                        v-model="form.new_password"
                        type="password"
                    />
                </el-form-item>
                <el-form-item
                    label="再次确认新密码"
                    prop="repeat_password"
                    :rules="repeat_password"
                >
                    <el-input
                        v-model="form.repeat_password"
                        type="password"
                    />
                </el-form-item>
                <el-button
                    type="primary"
                    @click="submit"
                >
                    提交
                </el-button>
            </el-form>
        </el-dialog>
        <layout-tags v-show="tagsList.length" />
    </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { baseLogout } from '@src/router/auth';
import LayoutTags from './LayoutTags.vue';
import md5 from 'js-md5';
import { PASSWORDREG } from '@js/const/reg';

export default {
    components: {
        LayoutTags,
    },
    data() {
        return {
            headingTitle:    '',
            asideCollapsed:  false,
            changepwdDialog: {
                visible: false,
            },
            form: {
                old_password:    '',
                new_password:    '',
                repeat_password: '',
            },
            old_password: [
                { required: true, message: '必填!' },
            ],
            new_password: [
                { required: true, message: '必填!' },
                {
                    validator: this.passwordType,
                    message:   '密码至少8位, 需包含数字,字母,特殊字符任意组合',
                    trigger:   'blur',
                },
            ],
            repeat_password: [
                { required: true, message: '请再次输入密码' },
                {
                    min:     8,
                    message: '密码至少8位',
                    trigger: 'blur',
                },
                {
                    validator: this.passwordCheck,
                    message:   '两次密码不一致',
                    trigger:   'blur',
                },
            ],
        };
    },
    computed: {
        ...mapGetters(['userInfo', 'tagsList']),
    },
    created() {
        this.$bus.$on('change-layout-header-title', data => {
            this.headingTitle = data;
        });
    },
    methods: {
        collapseAside() {
            this.asideCollapsed = !this.asideCollapsed;
            this.$bus.$emit('collapseChanged', this.asideCollapsed);
        },
        // 处理命令
        handleCommand(command) {
            const _this = this;

            if (!command) return;

            const policy = {
                logout() {
                    baseLogout();
                },
                changepwd() {
                    _this.changepwdDialog.visible = true;
                },
            };

            policy[command]();
        },
        // 检测全屏
        checkFullScreen() {
            const doc = document;

            return Boolean(
                doc.fullscreenElement ||
                    doc.webkitFullscreenElement ||
                    doc.mozFullScreenElement ||
                    doc.msFullscreenElement,
            );
        },
        // 切换全屏
        fullScreenSwitch() {
            const doc = document;

            if (this.checkFullScreen()) {
                const cancelFullScreen = [
                    'cancelFullScreen',
                    'webkitCancelFullScreen',
                    'mozCancelFullScreen',
                    'msExitFullScreen',
                ];

                for (const item of cancelFullScreen) {
                    if (doc[item]) {
                        doc[item]();
                        break;
                    }
                }
            } else {
                const element = doc.documentElement;
                const requestFullscreen = [
                    'requestFullscreen',
                    'webkitRequestFullscreen',
                    'mozRequestFullscreen',
                    'msRequestFullscreen',
                ];

                for (const item of requestFullscreen) {
                    if (element[item]) {
                        element[item]();
                        break;
                    }
                }
            }
        },
        passwordType(rule, value, callback) {
                if (PASSWORDREG.test(value)) {
                    callback();
                } else {
                    callback(false);
                }
            },
            passwordCheck(rule, value, callback) {
                if (value === this.form.new_password) {
                    callback();
                } else {
                    callback(false);
                }
            },
            submit() {
                this.$refs['form'].validate(async valid => {
                    if(valid) {
                        const oldPassword = [
                            this.userInfo.phone_number,
                            this.form.old_password,
                            this.userInfo.phone_number,
                            this.userInfo.phone_number.substr(0, 3),
                            this.form.old_password.substr(this.form.old_password.length - 3),
                        ].join('');
                        const password = [
                            this.userInfo.phone_number,
                            this.form.new_password,
                            this.userInfo.phone_number,
                            this.userInfo.phone_number.substr(0, 3),
                            this.form.new_password.substr(this.form.new_password.length - 3),
                        ].join('');

                        const { code } = await this.$http.post({
                            url:  '/account/update_password',
                            data: {
                                oldPassword: md5(oldPassword),
                                newPassword: md5(password),
                            },
                        });

                        if(code === 0) {
                            baseLogout({ redirect: false });
                            this.$message.success('密码修改成功! 请重新登录');
                        }
                    }
                });
            },
    },
};
</script>

<style lang="scss">
.heading-bar {
    text-align: right;
    padding: 12px 0;
    height: 60px;
    line-height: 36px;
    .heading-tools {
        display: inline-block;
        padding: 0 10px;
        height: 30px;
        line-height: 30px;
        text-align: center;
        [class*="el-icon-"] {
            width: 30px;
            height: 30px;
            line-height: 36px;
            margin-left: 10px;
            cursor: pointer;
            &:hover {
                transform: scale(1.15);
            }
        }
    }
    .heading-user {
        display: inline-block;
        padding-right: 10px;
        font-size: 14px;
        height: 30px;
        line-height: 30px;
        cursor: pointer;
    }
}
</style>
