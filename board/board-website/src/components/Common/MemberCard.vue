<template>
    <div
        :class="['member-card', { readonly: !edit }]"
        :style="{ width: size[0], height: size[1] }"
    >
        <el-tooltip v-if="form && form.ext_json && form.ext_json.real_name_auth_status === 2"  content="已通过企业实名认证" effect="light">
            <span class="certification">
                <i class="iconfont icon-certification" title="已通过企业实名认证"></i>
                已通过企业实名认证
            </span>
        </el-tooltip>
        <MemberAvatar
            :uploader="uploader"
            :member-name="vData.member.member_name"
            :img="vData.member.member_logo"
        />
        <div class="member-content">
            <div class="member-name">{{ vData.member.member_name }}</div>
            <el-form>
                <el-form-item>
                    <el-input
                        v-model="vData.member.member_email"
                        placeholder="邮箱"
                        :readonly="!edit"
                    >
                        <template #prefix>
                            <i class="iconfont icon-email"></i>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item>
                    <el-input
                        v-model="vData.member.member_mobile"
                        placeholder="电话"
                        :readonly="!edit"
                    >
                        <template #prefix>
                            <i class="iconfont icon-mobile"></i>
                        </template>
                    </el-input>
                </el-form-item>
            </el-form>
        </div>
        <slot name="default" />
    </div>
</template>

<script>
    import {
        reactive,
        computed,
        onBeforeMount,
        watch,
    } from 'vue';
    import { useStore } from 'vuex';

    export default {
        name:  'MemberCard',
        props: {
            uploader: Boolean,
            size:     {
                type:    Array,
                default: () => ['400px', '200px'],
            },
            form: Object,
            edit: Boolean,
        },
        setup(props) {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const vData = reactive({
                member: {
                    member_name:   '',
                    member_logo:   '',
                    member_email:  '',
                    member_mobile: '',
                },
            });
            const init = () => {
                if(props.form) {
                    vData.member.member_name = props.form.name;
                    vData.member.member_logo = props.form.logo;
                    vData.member.member_email = props.form.email;
                    vData.member.member_mobile = props.form.mobile;
                } else {
                    vData.member.member_email = userInfo.value.member_email;
                    vData.member.member_name = userInfo.value.member_name;
                    vData.member.member_logo = userInfo.value.member_logo;
                    vData.member.member_mobile = userInfo.value.member_mobile;
                }
            };

            onBeforeMount(() => {
                init();
            });

            watch(
                () => userInfo,
                () => {
                    init();
                },
                { deep: true },
            );

            return {
                vData,
                init,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .member-card{
        display:flex;
        position: relative;
        background: url('../../assets/images/card-back.png') repeat-x 0 0;
        background-size:contain;
        padding:20px 30px;
        border-radius: 10px;
        border:1px solid #eee;
        box-shadow:-10px 10px 10px 4px #ddd;
        &.readonly{
            :deep(.el-input__inner):hover{
                border-color: transparent;
                background: transparent;
            }
        }
        :deep(.el-input__inner){color: #fff;}
    }
    .certification{
        position: absolute;
        top: 15px;
        right: 20px;
        font-size: 12px;
        color: $--color-warning;
        line-height: 20px;
    }
    .icon-certification{
        position: relative;
        top:2px;
    }
    .member-avatar{margin-top: 30px;}
    .member-content{
        flex: 1;
        position: relative;
        margin-left: 20px;
        padding-left: 0px;
        margin-top: 20px;
        :deep(.el-form-item){margin-bottom: 0;}
        :deep(.el-input__inner){
            border-color: transparent;
            background: transparent;
            padding-right: 0;
            &:hover{
                border-color: #e5e9f2;
                background:#fff;
            }
        }
    }
    .member-name{
        color: #fff;
        margin-bottom: 10px;
        font-size: 18px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-weight: bold;
        &:after {
            content: '';
            width: 60px;
            display: block;
            margin-top: 5px;
            border-bottom: 1px solid #eee;
        }
    }
</style>
