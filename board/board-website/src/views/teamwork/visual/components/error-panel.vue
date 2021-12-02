<template>
    <transition name="dropIn">
        <div
            ref="dom"
            v-if="vData.show"
            :style="{top: `${vData.top}px`, left: `${vData.left}px`}"
            class="el-popover el-popper error-popover"
            data-popper-placement="top"
        >
            <i
                class="el-icon-close"
                @click="close"
            />
            <p class="el-popover__title">
                <i class="el-icon-warning" />
                发生错误:
            </p>
            <i class="el-popper__arrow" />
            {{ vData.message }}
        </div>
    </transition>
</template>

<script>
    import { ref, reactive } from 'vue';

    export default {
        setup() {
            const dom = ref();
            const vData = reactive({
                show:    false,
                nodeId:  '', // current node id
                top:     0,
                left:    0,
                message: '',
            });
            const close = () => {
                vData.show = false;
                vData.message = '';
                vData.nodeId = '';
            };

            return {
                vData,
                close,
                dom,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .error-popover{
        top: 100px;
        left: 10px;
        width: 400px;
        position: fixed;
        margin-left: -130px;
        .el-popover__title{color: #F85564;}
        .el-popper__arrow{
            background:none;
            left:50%;
            &:before {background: #fff;}
        }
        .el-icon-close{
            position: absolute;
            top: 10px;
            right: 10px;
            font-size: 20px;
            cursor: pointer;
        }
    }
</style>
