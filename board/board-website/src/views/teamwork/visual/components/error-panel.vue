<template>
    <transition name="dropIn">
        <div
            ref="dom"
            v-if="vData.show"
            :style="{top: `${vData.top}px`, left: `${vData.left}px`}"
            class="board-popover board-popper error-popover"
            data-popper-placement="top"
        >
            <el-icon
                class="board-icon-close"
                @click="close"
            >
                <elicon-close />
            </el-icon>
            <p class="board-popover__title color-danger">
                <el-icon>
                    <elicon-warning-filled />
                </el-icon>
                发生错误:
            </p>
            <i class="board-popper__arrow" />
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
        .board-popper__arrow{
            background:none;
            left:50%;
            &:before {background: #fff;}
        }
        .board-icon-close{
            position: absolute;
            top: 10px;
            right: 10px;
            font-size: 20px;
            cursor: pointer;
        }
    }
</style>
