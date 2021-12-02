<template>
    <div
        ref="target"
        :class="['drag-resize', vData.size]"
        :style="{
            minWidth: `${vData.rect.minWidth}px`,
            maxWidth: `${vData.rect.maxWidth}px`,
            minHeight: `${vData.rect.minHeight}px`,
            maxHeight: `${vData.rect.maxHeight}px`,
            top: `${vData.rect.top}px`,
            left: `${vData.rect.left}px`,
            width: `${vData.rect.width}px`,
            height: `${vData.rect.height}px`,
            position,
            zIndex,
        }"
    >
        <div class="drag-content">
            <slot />
        </div>
        <!-- window btns -->
        <div
            v-if="windowBtns"
            class="window-btns"
            @mousedown.stop.prevent
        >
            <i
                v-if="vData.size === 'max'"
                class="icons el-icon-minus"
                @click="methods.min"
            />
            <i
                v-if="vData.size !== 'max'"
                class="icons el-icon-plus"
                @click="methods.max"
            />
            <i
                v-if="showHideBtn"
                class="icons el-icon-close"
                @click="methods.hide"
            />
        </div>
        <!-- control points -->
        <span :class="['control-points', vData.dragClass]">
            <span
                v-for="(p, index) in controlPoints"
                :key="index"
                :class="['icon-btn', Object.entries(p)[0][0]]"
                @mousedown.prevent="methods.dragStart"
            >
                <i
                    class="drag-target"
                    :action="Object.entries(p)[0][1].action"
                    :direction="Object.entries(p)[0][1].direction"
                    @mousemove.prevent="methods.dragMove"
                    @mouseup.prevent="methods.dragEnd"
                    @mouseleave.prevent="methods.dragEnd"
                />
                <i :class="['iconfont', Object.entries(p)[0][1].icon]" />
            </span>
        </span>
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
        nextTick,
        onMounted,
    } from 'vue';

    export default {
        name:  'DragResize',
        props: {
            position: {
                type:    String,
                default: 'fixed',
            },
            top:        String,
            left:       String,
            parent:     Object,
            limit:      Object,
            width:      String,
            height:     String,
            zIndex:     Number,
            minWidth:   String,
            maxWidth:   String,
            minHeight:  String,
            maxHeight:  String,
            windowBtns: {
                type:    Boolean,
                default: true,
            },
            showHideBtn: {
                type:    Boolean,
                default: false,
            },
            isDraggable: {
                type:    Boolean,
                default: true,
            },
            isResizeable: {
                type:    Boolean,
                default: false,
            },
            controlPoints: {
                type:    Array,
                default: () => [
                    {
                        'ctrl-top': {
                            action:    'drag',
                            direction: 'horzantical',
                            icon:      'icon-horzantical',
                        },
                    },
                    {
                        'ctrl-bottom': {
                            action:    'drag',
                            direction: 'horzantical',
                            icon:      'icon-horzantical',
                        },
                    },
                    {
                        'ctrl-right': {
                            action:    'resize',
                            direction: 'vertical',
                            icon:      'icon-vertical',
                        },
                    },
                    {
                        'ctrl-left': {
                            action:    'resize',
                            direction: 'vertical',
                            icon:      'icon-vertical',
                        },
                    },
                ],
            },
        },
        emits: ['window-hide', 'window-max', 'window-min', 'drag-start', 'dragging', 'drag-end'],
        setup(props, context) {
            const target = ref();
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;
            const vData = reactive({
                rect: {
                    top:       '',
                    left:      '',
                    width:     '',
                    height:    '',
                    minWidth:  '',
                    minHeight: '',
                },
                size:      'normal',
                dragClass: '',
                coord:     {
                    x: 0,
                    y: 0,
                },
                style:      {},
                isDragging: false,
            });
            const methods = {
                init() {
                    const { top, left } = target.value.getBoundingClientRect();

                    vData.rect.top = '';
                    vData.rect.left = '';

                    nextTick(_ => {
                        vData.rect.top = top;
                        vData.rect.left = left;
                    });
                },

                hide() {
                    vData.size = 'hide';
                    context.emit('window-hide');
                },

                max() {
                    vData.size = 'max';
                    vData.rect.top = 0;
                    vData.rect.left = 0;
                    context.emit('window-max', { rect: vData.rect });
                },

                min() {
                    vData.size = 'min';
                    context.emit('window-min', { rect: vData.rect });
                },

                dragStart(e) {
                    const { clientX, clientY } = e;

                    vData.coord.x = clientX;
                    vData.coord.y = clientY;
                    vData.isDragging = true;
                    vData.dragClass = 'covered';
                    context.emit('drag-start');
                },

                dragMove(e) {
                    if(vData.isDragging) {
                        const { clientX, clientY } = e;
                        const diff = {
                            x: vData.coord.x - clientX,
                            y: vData.coord.y - clientY,
                        };
                        const action = e.target.getAttribute('action') || 'drag';
                        const direction = e.target.getAttribute('direction') || 'horzantical';

                        vData.coord.x = clientX;
                        vData.coord.y = clientY;
                        if(action === 'drag') {
                            methods.updateRectPosition(diff);
                        } else {
                            methods.updateRectSize(direction, diff);
                        }
                        context.emit('dragging');
                    }
                },

                dragEnd(e) {
                    vData.dragClass = '';
                    vData.isDragging = false;
                    context.emit('drag-end');
                    $bus.$emit('drag-end');
                },

                updateRectSize(direction, diff) {
                    switch (direction) {
                    case 'horzantical':
                        {
                            const width = parseInt(vData.rect.width, 10); // to number

                            vData.rect.width = width + diff.x;

                            if(width + diff.x <= parseInt(vData.rect.minWidth, 10)) {
                                vData.rect.width = vData.rect.minWidth;
                            }
                        }
                        break;
                    case 'vertical':
                        {
                            const height = parseInt(vData.rect.width, 10); // to number

                            vData.rect.height = height + diff.y;

                            if(height + diff.y <= parseInt(vData.rect.minHeight, 10)) {
                                vData.rect.height = vData.rect.minHeight;
                            }
                        }
                        break;
                    }
                },

                updateRectPosition(diff) {
                    vData.rect.left -= diff.x;
                    vData.rect.top -= diff.y;
                },
            };

            onMounted(() => {
                const { width, height } = target.value.getBoundingClientRect();

                vData.rect.width = props.width || props.minWidth;
                vData.rect.height = props.height || props.minHeight;
                vData.rect.minWidth = props.minWidth;
                vData.rect.minHeight = props.minHeight;
                // calc width/height
                if(!vData.rect.width) {
                    vData.rect.width = width;
                }
                if(!vData.rect.height) {
                    vData.rect.height = height;
                }
                vData.rect.top = props.top;
                vData.rect.left = props.left;
            });

            return {
                vData,
                target,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
.drag-resize{
    position: relative;
    &.max{
        top:0;
        left:0;
        width: 100% !important;
        height: 100% !important;
        transform: translateX(0) translateY(0) translateZ(0);
    }
}
.drag-content{
    overflow-y: auto;
    position: absolute;
    top: 0;
    left:0;
    height: 100%;
    width: 100%;
}
.window-btns{
    position: absolute;
    right:25px;
    top: 11px;
    z-index:10;
    height: 24px;
    font-size: 12px;
    color: #fff;
    .icons{
        width: 16px;
        height: 16px;
        line-height: 16px;
        font-style: normal;
        display: inline-block;
        vertical-align: top;
        text-align: center;
        border-radius: 50%;
        margin-left: 5px;
        font-size: 0;
        &:before{display: inline-block;}
        &:hover{
            font-size:12px;
            cursor: pointer;
        }
    }
    .el-icon-minus{background: #f1b92a;}
    .el-icon-plus{background: #35c895;}
    .el-icon-close{background: #f85564;}
}
.control-points{
    .icon-btn{
        position: absolute;
        cursor: move;
    }
    .drag-target{
        display:none;
        position: absolute;
        width: 400px;
        height: 400px;
        margin-left: -200px;
        margin-top: -200px;
        cursor: move;
    }
    &.covered{
        .drag-target{display: block;}
    }
    .iconfont{display:block;}
}
.icon-btn{
    &.ctrl-top{
        top: 4px;
        left:50%;
    }
    &.ctrl-right{
        right: 4px;
        top:50%;
    }
    &.ctrl-bottom{
        bottom: 4px;
        left:50%;
    }
    &.ctrl-left{
        left: 4px;
        top:50%;
    }
}
</style>
