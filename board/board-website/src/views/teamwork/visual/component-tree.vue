<template>
    <div
        ref="treeContainer"
        :class="['tree-container', { 'is-folder': data.isFolder }]"
        :style="`height:${ vData.unfold && data.isFolder ? `${ vData.height }px` : '' }`"
    >
        <div
            :class="[{
                'tree-item': !data.isFolder,
                'tree-drag': vData.draggable && !data.isFolder,
                'is-folder': data.isFolder,
            }]"
            :title="data.desc"
            draggable="true"
            @mouseup="methods.mouseup"
            @mousedown="methods.mousedown($event, data)"
            @dragstart="methods.dragstart($event, data)"
            @dragend="methods.dragend"
        >
            <i
                v-if="vData.unfold && data.isFolder"
                class="el-icon-caret-bottom"
            />
            <i
                v-else-if="!vData.unfold && data.isFolder"
                class="el-icon-caret-right"
            />
            {{ data.name.split('·')[0] }}
            <p v-if="data.name.split('·')[1]">{{ data.name.split('·')[1] }}</p>
        </div>
        <template v-if="data.children && data.children.length">
            <ComponentTree
                v-for="(item, $index) in data.children"
                :ref="el => { if (el) treeContainers[i] = el }"
                :key="`${index}-${$index}`"
                :data="item"
                :index="`${$index}`"
                @ready-to-drag="methods.readyToDrag"
                @drag-to-end="methods.dragend"
            />
        </template>
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        nextTick,
        onBeforeMount,
    } from 'vue';
    import componentCfg from './component-list/component-cfg';

    export default {
        name:  'ComponentTree',
        props: {
            data:  Object,
            index: String,
        },
        emits: ['ready-to-drag', 'drag-to-end', 'ready-to-drag'],
        setup(props, context) {
            const treeContainer = ref();
            const treeContainers = ref([]);
            const vData = reactive({
                draggable: false,
                unfold:    true,
                height:    'auto',
            });
            const img = new Image();
            const methods = {
                updateHeight() {
                    nextTick(() => {
                        vData.height = treeContainer.value.scrollHeight;
                    });
                },
                mousedown(e, item) {
                    if(item.isFolder) {
                        vData.unfold = !vData.unfold;
                    } else {
                        vData.draggable = true;
                    }
                },
                mouseup() {
                    vData.draggable = false;
                },
                dragstart(event, item) {
                    context.emit('ready-to-drag');

                    event.dataTransfer.setDragImage(img, 35, 20);
                    /* set dataTransfer */
                    event.dataTransfer.setData(
                        'dragComponent',
                        JSON.stringify({
                            label: item.name,
                            id:    item.id,
                            data:  {
                                componentType: item.id,
                                ...componentCfg[item.id],
                            },
                        }),
                    );
                },
                dragend() {
                    vData.draggable = false;
                    context.emit('drag-to-end');
                },
                readyToDrag() {
                    context.emit('ready-to-drag');
                },
            };

            onBeforeMount(() => {
                img.src = require('@assets/images/node.png');
            });

            return {
                vData,
                treeContainer,
                treeContainers,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .tree-container{
        overflow: hidden;
        margin-left: -10px;
        padding-left: 10px;
        transition-duration: 0.3s;
        user-select:none;
    }
    .tree-item{
        cursor: grab;
        padding:6px 10px;
        font-size: 12px;
        line-height:20px;
        border-radius:4px;
        text-align: center;
        margin-bottom: 10px;
        background: #ecf3ff;
        border: 1px solid #d9e8ff;
        word-break: break-all;
        color:#4483FF;
        &.tree-drag{cursor: grabbing;}
        &:hover{
            opacity: 0.8;
            background:#d9e8ff;
        }
    }
    .is-folder{
        height: 40px;
        line-height: 30px;
        margin-left: -10px;
        cursor: pointer;
        color: #909399;
    }
</style>
