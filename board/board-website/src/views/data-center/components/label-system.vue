<template>
    <div class="label_system">
        <div id="container" ref="container" class="container" :style="{width: vData.width+'px'}" />
        <label-modal ref="labelModalRef" :labelList="vData.labelList" :labelPosition="vData.labelPosition" @destroy-node="methods.destroyNode" @label-node="methods.labelNode" @key-code-search=methods.keyCodeSearch />
        <el-button type="primary" class="save_label" @click="methods.saveLabel">
            保存当前标注
        </el-button>
    </div>
</template>

<script>
    import Konva from 'konva';
    import { ref, reactive } from 'vue';
    import cup1 from '@assets/images/card-back.png';
    import LabelModal from './label-modal.vue';
    export default {
        components: {
            LabelModal,
        },
        props: {
            currentImage: String,
        },
        setup(props, context) {
            const vData = reactive({
                stage:      null, // 导致无法在图片以外的区域画框 Proxy
                layer:      null,
                graphNow:   null,
                trLayer:    null,
                labelLayer: null,
                groupLayer: null,
                rectLayer:  null,
                cup1,
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
                    {
                        text:    '苹果',
                        keyword: 10,
                    },
                    {
                        text:    '电脑',
                        keyword: 11,
                    },
                ],
                oldLabelList: [
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
                    {
                        text:    '苹果',
                        keyword: 10,
                    },
                    {
                        text:    '电脑',
                        keyword: 11,
                    },
                ],
                labelPosition: '',
                currentLabel:  {},
                labelNowPos:   null, // 标注文字位置
                isLabeled:     false, // 当前标注框是否已标注
            });

            console.log(vData.width);
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

                    let imgOptions = {}, imageLayer = {};

                    imgObj.onload = () => {
                        let imgW= imgObj.width, imgH = imgObj.height;

                        if (imgW >= 999) {
                            imgW = imgW / 2;
                            imgH = imgH /2;
                        } else if(999>imgW > 1500) {
                            imgW = imgW/3;
                            imgH = imgH /3;
                        } else if (imgW > 3000) {
                            imgW = imgW/6;
                            imgH = imgH /6;
                        }
                        // console.log(imgW, imgH);
                        imgOptions = {
                            // x:      vData.width/2 - imgW/2,
                            x:      0,
                            y:      0,
                            image:  imgObj,
                            width:  imgW,
                            height: imgH,
                        };
                    };
                    setTimeout(() => {
                        imageLayer = new Konva.Image(imgOptions);
                        vData.layer.add(imageLayer);
                        vData.layer.batchDraw();
                    }, 100);
                    imgObj.src = props.currentImage;
                
                    vData.stage.on('mousedown', function(e) {
                        labelModalRef.value.methods.hideModal();
                        vData.labelList = vData.oldLabelList;
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
                            // 鼠标右击 对于已标注的区域可进行编辑操作
                            if (this.attrs.isLabeled) {
                                console.log(this);
                                methods.showLabelModal();
                            }
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
                        vData.labelList = vData.oldLabelList;
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
                        vData.stage.find('Transformer').destroy();
                        this.destroy();
                        if (e.target.attrs.isLabeled) {
                            vData.labelLayer.destroy();
                            vData.currentLabel = {};
                        }
                        vData.layer.draw();
                        labelModalRef.value.methods.hideModal();
                        vData.labelList = vData.oldLabelList;
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
                    if (e.target.attrs.isLabeled && e.evt.button !== 2) {
                        labelModalRef.value.methods.hideModal();
                        vData.labelList = vData.oldLabelList;
                        vData.labelList = vData.oldLabelList;
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
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.width * vData.graphNow.attrs.scaleX + 10;
                        labelY = vData.graphNow.attrs.y;
                    }
                
                    // 2、width>0, height<0
                    if (vData.graphNow.width() > 0 && vData.graphNow.height() < 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.width * vData.graphNow.attrs.scaleX + 10;
                        labelY = vData.graphNow.attrs.y + vData.graphNow.height();
                    }

                    // 3、width<0, height>0
                    if (vData.graphNow.width() < 0 && vData.graphNow.height() > 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.scaleX + 10;
                        labelY = vData.graphNow.attrs.y;
                    }

                    // 4、width<0, height<0
                    if (vData.graphNow.width() < 0 && vData.graphNow.height() < 0) {
                        labelX = vData.graphNow.attrs.x + vData.graphNow.attrs.scaleX + 10;
                        labelY = vData.graphNow.attrs.y + vData.graphNow.height();
                    }

                    vData.labelPosition =`${ labelX }px, ${ labelY }px`;
                    console.log(vData.labelPosition);
                    labelModalRef.value.methods.showModal();
                },
                // emit 删除标注框
                destroyNode() {
                    vData.layer.find('Transformer').destroy();
                    vData.layer.find('Rect').destroy();
                    if (vData.rectLayer.attrs.isLabeled) vData.labelLayer.destroy();
                    vData.layer.draw();
                    labelModalRef.value.methods.hideModal();
                    vData.labelList = vData.oldLabelList;
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
                    vData.labelList = vData.oldLabelList;
                    vData.rectLayer.setAttrs({
                        isLabeled: true,
                        labelName: data.text,
                    });
                },
                setLabelTextPosition() {
                    // 需考虑用户画标注框的初始方向
                    vData.labelLayer.setAttrs({
                        x: vData.labelNowPos.x() + vData.labelNowPos.width() * vData.labelNowPos.scaleX() / 2,
                        y: vData.labelNowPos.y() + vData.labelNowPos.height() * vData.labelNowPos.scaleY() / 2 - 18/2,
                    });
                },
                keyCodeSearch(val) {
                    vData.labelList = vData.oldLabelList.filter(function(item) {
                        return Object.keys(item).some(function(key) {
                            return (
                                String(item[key]).toLowerCase().indexOf(val) > -1
                            );
                        });
                    });
                },
                // 保存当前标注
                saveLabel() {
                    const labe_list = [];

                    const rect_list = vData.stage.find('Rect');

                    console.log(rect_list);
                    rect_list.forEach(item => {
                        if (item.attrs.isLabeled) {
                            labe_list.push({
                                label:  item.attrs.labelName,
                                points: [
                                    { x: item.attrs.x, y: item.attrs.y },
                                    { x: item.attrs.x + item.attrs.width, y: item.attrs.y + item.attrs.height },
                                ],
                            });
                        }
                    });
                    console.log(labe_list);
                    context.emit('save-label', labe_list);
                },
            };

            return {
                vData,
                methods,
                labelModalRef,
            };
        },
    };
</script>
<style lang="scss">
.label_system {
    position: relative;
    border: 1px solid #eee;
    background: #acd;
    .container {
        canvas {
            background: #f0f0f0!important;
        }
    }
    .save_label {
        position: absolute;
        right: 300px;
        top: 12px;
    }
}
</style>
