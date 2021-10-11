<template>
    <div
        class="el-checkbox-group f12"
        :style="{ height: `${vData.height}px`}"
    >
        <VirtualList
            ref="virtualListRef"
            :data="vData.virtualList"
            :poolBuffer="20"
            :itemSize="32"
        >
            <template v-slot="{ index }">
                <slot name="checkbox" :index="index" :list="list"></slot>
            </template>
        </VirtualList>
    </div>
</template>

<script>
    import { ref, reactive, onMounted, nextTick } from 'vue';

    export default {
        name:  'BetterCheckbox',
        props: {
            list: Array,
        },
        emits: ['change'],
        setup(props, context) {
            const virtualListRef = ref();
            const vData = reactive({
                virtualList: [],
                height:      100,
            });

            for(let i = 0; i < Math.ceil(props.list.length / 5); i++) {
                vData.virtualList.push({ i });
            }

            onMounted(_ => {
                nextTick(_ => {
                    vData.height = virtualListRef.value.scrollHeight;
                });
            });

            return {
                vData,
                virtualListRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-checkbox-group{
        max-height: 500px;
        transform: translateX(0) translateY(0) translateZ(0);
        overflow: auto;
        :deep(.el-checkbox){
            user-select:auto;
            padding-right: 10px;
            overflow: hidden;
            width: 20%;
            margin:0;
        }
        :deep(.el-checkbox__label){
            width:100%;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    }
</style>
