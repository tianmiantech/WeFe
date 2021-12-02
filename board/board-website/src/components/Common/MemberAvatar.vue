<template>
    <div
        class="member-avatar"
        :style="{ width: `${width}px`, height: `${width}px`, 'line-height': `${width - 2}px` }"
    >
        <el-upload
            v-if="uploader"
            :class="['avatar-uploader', { disabled: !userInfo.super_admin_role }]"
            :before-upload="beforeUpload"
            :show-file-list="false"
            :http-request="http"
            accept="image/*"
            action="#"
        >
            <div
                v-if="img"
                class="avatar-img"
                :style="{ width: `${width}px`, height: `${width}px`, 'line-height': `${width - 2}px` }"
            >
                <img :src="img">
            </div>
            <i
                v-else
                class="el-icon-plus avatar-uploader-icon"
                :style="{ width: `${width}px`, height: `${width}px`, 'line-height': `${width}px` }"
            />
        </el-upload>
        <div
            v-else
            class="text-c"
        >
            <template v-if="img != null">
                <img
                    v-if="img"
                    :src="img"
                >
                <strong
                    v-else
                    class="nickname"
                >{{ (memberName || userInfo.member_name || '').substring(0,1) }}</strong>
            </template>
            <template v-else>
                <img
                    v-if="userInfo.member_logo"
                    :src="userInfo.member_logo"
                >
                <strong
                    v-else
                    class="nickname"
                >{{ (memberName || userInfo.member_name || '').substring(0,1) }}</strong>
            </template>
        </div>
    </div>
</template>

<script>
    import {
        getCurrentInstance,
        computed,
        watch,
    } from 'vue';
    import { useStore } from 'vuex';

    export default {
        name:  'MemberAvatar',
        props: {
            img:        String,
            uploader:   Boolean,
            memberName: String,
            width:      {
                type:    Number,
                default: 100,
            },
            fileSizeLimit: {
                type:    Number,
                default: 2, // MB
            },
        },
        emits: ['beforeUpload'],
        setup(props, context) {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const { appContext } = getCurrentInstance();
            const { $message } = appContext.config.globalProperties;
            // custom upload way
            const http = () => {};
            const beforeUpload = (file) => {
                const isLimit = file.size / 1024 / 1024 < props.fileSizeLimit;

                if (!isLimit) {
                    return $message.error(`上传头像图片大小不能超过 ${props.fileSizeLimit}MB!`);
                }

                const imgFile = new FileReader();

                imgFile.readAsDataURL(file);
                imgFile.onload = function (event) {
                    const { result } = event.target; //base64 data

                    context.emit('beforeUpload', result);
                };
            };

            watch(
                () => userInfo,
                () => {
                    // update all avatars
                    // console.log(JSON.stringify(val));
                },
                { deep: true },
            );

            return {
                userInfo,
                beforeUpload,
                http,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .member-avatar{
        overflow: hidden;
        display: inline-block;
        vertical-align:middle;
        color: $--color-primary;
        background:#f5f5f5;
        border-radius: 4px;
        font-size: 20px;
        img{width: 100%;
            height:auto;
        }
    }
    .avatar-uploader{
        :deep(.el-upload) {
            cursor: pointer;
            border-radius: 8px;
            position: relative;
            overflow: hidden;
        }
        &.disabled{
            :deep(.el-upload) {
                background:#f5f5f5;
                cursor: not-allowed;
            }
        }
    }
    .avatar-uploader-icon {
        font-size: 28px;
        color: #8c939d;
        text-align: center;
        border: 1px dashed #ddd;
        border-radius: 6px;
    }
    .avatar-img {
        background:#f5f5f5;
        display: block;
    }
</style>
