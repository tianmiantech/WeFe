<template>
    <div id="container" ref="container" class="container" :style="{width: 800+'px'}" />
    <label-modal ref="labelModalRef" :labelList="vData.labelList" :labelPosition="vData.labelPosition" @destroy-node="methods.destroyNode" @label-node="methods.labelNode" />
</template>

<script>
    import Konva from 'konva';
    import { ref, onBeforeMount, reactive } from 'vue';
    // import cup1 from '@assets/images/image-data.png';
    import LabelModal from './components/label-modal.vue';
    export default {
        components: {
            LabelModal,
        },
        setup() {
            const vData = reactive({
                stage:      null, // 导致无法在图片以外的区域画框 Proxy
                layer:      null,
                graphNow:   null,
                trLayer:    null,
                labelLayer: null,
                groupLayer: null,
                rectLayer:  null,
                cup1:       'blob:http://172.29.20.91:8081/d4339796-7849-4687-b17b-92feb999cac4',
                width:      800,
                height:     528,
                pointStart: [],
                graphColor: '#1A73E8',
                drawing:    false,
                labelList:  [
                    {
                        text:    'mouse',
                        keyword: 1,
                    },
                    {
                        text:    'flower',
                        keyword: 2,
                    },
                    {
                        text:    'fruit',
                        keyword: 3,
                    },
                    {
                        text:    'person',
                        keyword: 4,
                    },
                    {
                        text:    'monkey',
                        keyword: 5,
                    },
                    {
                        text:    'book',
                        keyword: 6,
                    },
                    {
                        text:    'desktop',
                        keyword: 7,
                    },
                    {
                        text:    'tv',
                        keyword: 8,
                    },
                    {
                        text:    'paper',
                        keyword: 9,
                    },
                ],
                labelPosition: '',
                currentLabel:  {},
                labelNowPos:   null, // 标注文字位置
                isLabeled:     false, // 当前标注框是否已标注
            });
            const labelModalRef = ref();

            const methods = {
                createStage() {
                    vData.stage = new Konva.Stage({
                        container: 'container',
                        width:     vData.width,
                        height:    vData.height,
                    });
                    vData.stage.container().style.cursor = 'crosshair';
                    vData.layer = new Konva.Layer();
                    vData.stage.add(vData.layer);

                    const imgObj = new Image();

                    let imgOptions = {};

                    let imageLayer = {};

                    imgObj.onload = () => {
                        imgOptions = {
                            x:      vData.width/2 - 300/2 - vData.height/4,
                            y:      0,
                            image:  imgObj,
                            width:  vData.height,
                            height: vData.height,
                        };
                    };
                    setTimeout(() => {
                        imageLayer = new Konva.Image(imgOptions);
                        vData.layer.add(imageLayer);
                        vData.layer.batchDraw();
                    }, 100);
                    imgObj.src = vData.cup1;
                
                    vData.stage.on('mousedown', function(e) {
                        labelModalRef.value.methods.hideModal();
                        let lastEvent = null;
                        // 如果点击空白处 移除图形选择框

                        if ((e.target === vData.stage || e.target === imageLayer) && lastEvent !== e.evt) {
                            methods.stageMousedown(e);
                            vData.stage.find('Transformer').destroy();
                            vData.layer.draw();
                            return;
                        }
                        lastEvent = e.evt;
                    
                        // 如果没有匹配到就终止往下执行
                        if (!e.target.hasName('rect')) {
                            return;
                        }
                    
                        // 移除图形选择框
                        vData.stage.find('Transformer').destroy();

                        // 当前点击的对象赋值给graphNow
                        vData.graphNow = e.target;
                        if (e.target.attrs.isLabeled) {
                        // 可删除已标注的选框
                        } else {
                            // 创建图形选框事件
                            methods.createTransformer(e);
                        }
                    });
                    vData.stage.on('mousemove', function(e) {
                        if (vData.graphNow && vData.drawing) {
                            methods.stageMousemove(e);
                        }
                    });
                    vData.stage.on('mouseup', function () {
                        vData.drawing = false;
                        // 画框宽高大于10时显示标签选框
                        if (vData.graphNow && (Math.abs(vData.graphNow.width()) >= 10 && Math.abs(vData.graphNow.height()) >= 10) && !vData.rectLayer.attrs.isLabeled) {
                            methods.showLabelModal();
                        }
                    });
                },
                stageMousedown(e) {
                    const x = e.evt.offsetX, y = e.evt.offsetY;

                    vData.pointStart = [x, y];
                    methods.drawRect(x, y, 0, 0, vData.graphColor, 0);
                    vData.drawing = true;
                },
                drawRect(x, y, w, h, c, sw) {
                    vData.rectLayer = new Konva.Rect({
                        name:        'rect',
                        x,
                        y,
                        width:       w,
                        height:      h,
                        fill:        sw === 0 ? c : null,
                        // stroke: sw > 0 ? c : null,
                        stroke:      'black',
                        strokeWidth: sw,
                        opacity:     sw === 0 ? 0.5 : 1,
                        draggable:   true,
                    });
                    vData.graphNow = vData.rectLayer;
                    vData.layer.add(vData.rectLayer);
                    vData.layer.draw();
                    vData.labelNowPos = vData.rectLayer;
                
                    vData.rectLayer.on('dragmove', function(e) {
                        vData.labelNowPos.setAttrs({
                            x: vData.rectLayer.x(),
                            y: vData.rectLayer.y(),
                        });
                        if (e.target.attrs.isLabeled) {
                            methods.setLabelTextPosition();
                        }
                    });
                    vData.rectLayer.on('mousedown', function(e) {
                        if (e.evt.button === 2) {
                        // 鼠标右击
                        }
                    });
                    vData.rectLayer.on('click', function(e) {
                        if (e.target.attrs.isLabeled) {
                            vData.labelNowPos = vData.rectLayer;
                            vData.stage.find('Transformer').destroy();
                            methods.createTransformer(e);
                        }
                    });
                    vData.rectLayer.on('transformstart', function() {
                        labelModalRef.value.methods.hideModal();
                    });
                    vData.rectLayer.on('transform', function(e) {
                        if (e.target.attrs.isLabeled) {
                            vData.labelNowPos.setAttrs({
                                x:      vData.rectLayer.x(),
                                y:      vData.rectLayer.y(),
                                width:  vData.rectLayer.width(),
                                height: vData.rectLayer.height(),
                            });
                            methods.setLabelTextPosition();
                        }
                    });
                    vData.rectLayer.on('mouseenter', function() {
                        vData.stage.container().style.cursor = 'move';
                    });
                    vData.rectLayer.on('mouseleave', function() {
                        vData.stage.container().style.cursor = 'crosshair';
                    });
                    vData.rectLayer.on('mouseup', function(e) {
                        if (!e.target.attrs.isLabeled) {
                            methods.showLabelModal();
                        } else {
                        // 可删除已标注
                        }
                    
                    });
                    vData.rectLayer.on('dblclick', function(e) {
                        this.remove();
                        vData.stage.find('Transformer').destroy();
                        this.destroy();
                        if (e.target.attrs.isLabeled) {
                            vData.labelLayer.destroy();
                            vData.currentLabel = {};
                        }
                        vData.layer.draw();
                        labelModalRef.value.methods.hideModal();
                        vData.stage.container().style.cursor = 'crosshair';
                    });
                },
                createTransformer(e) {
                    vData.trLayer = new Konva.Transformer({
                        borderStroke:       '#fff',
                        keepRatio:          false, // 不等比缩放
                        anchorCornerRadius: 10,
                        anchorSize:         10, // 
                        rotateEnabled:      false, // 是否可调节框选区域角度
                        resizeEnabled:      true, // 是否可调节框选区域大小
                        anchorFill:         '#fff',
                    });
                    vData.layer.add(vData.trLayer);
                    vData.trLayer.nodes([e.target]);
                    vData.layer.draw();
                    if (e.target.attrs.isLabeled) {
                        labelModalRef.value.methods.hideModal();
                    }
                },
                stageMousemove(e) {
                    vData.graphNow.setAttrs({
                        width:  e.evt.offsetX - vData.pointStart[0],
                        height: e.evt.offsetY - vData.pointStart[1],
                    });
                    vData.layer.draw();
                },
                showLabelModal() {
                    vData.graphNow.attrs.scaleX = vData.graphNow.attrs.scaleX || 1;
                    let labelX = 0, labelY = 0;
                    // 不同方向画标注框，标签选框显示在标注框的同一位置
                    // 1、width、height>0

                    if (vData.graphNow.width() > 0 && vData.graphNow.height() > 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.width * vData.graphNow.attrs.scaleX + 27;
                        labelY = vData.graphNow.attrs.y + 20;
                    }
                
                    // 2、width>0, height<0
                    if (vData.graphNow.width() > 0 && vData.graphNow.height() < 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.width * vData.graphNow.attrs.scaleX + 27;
                        labelY = vData.graphNow.attrs.y + 20 + vData.graphNow.height();
                    }

                    // 3、width<0, height>0
                    if (vData.graphNow.width() < 0 && vData.graphNow.height() > 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.scaleX + 27;
                        labelY = vData.graphNow.attrs.y + 20;
                    }

                    // 4、width<0, height<0
                    if (vData.graphNow.width() < 0 && vData.graphNow.height() < 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.scaleX + 27;
                        labelY = vData.graphNow.attrs.y + 20 + vData.graphNow.height();
                    }

                    vData.labelPosition =`${ labelX }px, ${ labelY }px`;
                    labelModalRef.value.methods.showModal();
                },
                // emit 删除标注框
                destroyNode() {
                    vData.layer.find('Transformer').destroy();
                    vData.layer.find('Rect').destroy();
                    vData.labelLayer.destroy();
                    vData.layer.draw();
                    labelModalRef.value.methods.hideModal();
                },
                // 标注
                labelNode(data) {
                    // console.log(vData.labelNowPos.x(), vData.labelNowPos.y(),vData.labelNowPos.width(), vData.labelNowPos.height());
                    vData.currentLabel = data;
                    vData.labelLayer = new Konva.Text({
                        x:        vData.labelNowPos.x() + vData.labelNowPos.width()/2,
                        y:        vData.labelNowPos.y() + vData.labelNowPos.height()/2 - 18/2,
                        text:     data.text,
                        fontSize: 18,
                        fill:     'rgba(255, 255, 255, .7)',
                    });
                    vData.labelLayer.offsetX(vData.labelLayer.width() / 2);
                    vData.layer.add(vData.labelLayer);
                    vData.layer.draw();
                    vData.currentLabel = {}; // 同一张图中有多个标注时，清除上个标注信息
                    vData.labelLayer.on('mouseenter', function() {
                        vData.stage.container().style.cursor = 'move';
                    });
                    labelModalRef.value.methods.hideModal();
                    vData.stage.find('Rect').setAttrs({
                        isLabeled: true,
                    });
                },
                setLabelTextPosition() {
                    // 需考虑用户画标注框的初始方向
                    vData.labelLayer.setAttrs({
                        x: vData.labelNowPos.x() + vData.labelNowPos.width() * vData.labelNowPos.scaleX() / 2,
                        y: vData.labelNowPos.y() + vData.labelNowPos.height() * vData.labelNowPos.scaleY() / 2 - 18/2,
                    });
                },
            };

            onBeforeMount(() => {
                setTimeout(() => {
                    methods.createStage();
                }, 10);
            });

            return {
                vData,
                methods,
                labelModalRef,
            };
        },
    };
</script>
<style lang="scss">
.container {
    canvas {
        background: #f0f0f0!important;
    }
}
</style>
