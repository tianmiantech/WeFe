<template>
    <div class="label_system" :style="{width: vData.width+'px', height: 400+'px'}">
        <div id="container" ref="container" class="container" :style="{width: vData.width+'px', height: 400+'px'}" />
    </div>
</template>

<script>
    import Konva from 'konva';
    import { ref, reactive } from 'vue';
    export default {
        props: {
            currentImage: Object,
            labelList:    Array,
            forJobType:   String,
        },
        setup(props, context) {
            const vData = reactive({
                stage:          null, // 导致无法在图片以外的区域画框 Proxy
                layer:          null,
                graphNow:       null,
                trLayer:        null,
                labelLayer:     null,
                groupLayer:     null,
                rectLayer:      null,
                width:          700,
                height:         400,
                pointStart:     [],
                graphColor:     '#1A73E8',
                drawing:        false,
                labelList:      props.labelList,
                labelPosition:  '',
                labelNowPos:    null, // 标注文字位置
                isLabeled:      false, // 当前标注框是否已标注
                currentRect:    null, // 当前被选中的标注框
                currentText:    null, // 当前被选中的标注框文字
                imgScaleConfig: {
                    x:      0,
                    y:      0,
                    scaleX: 1,
                    scaleY: 1,
                },
                imgLoading: false,
                labelScale: {
                    x: 1,
                    y: 1,
                },
                labelScaleX: 1,
                labelScaleY: 1,
            });

            let imageLayer = null;
            const labelModalRef = ref();
            const methods = {
                editLabelStage(scaleX, scaleY) {
                    // 模型校验标注框与标注系统的不一致
                    if (props.currentImage.item) {
                        console.log(props.currentImage.item);
                        const list = props.currentImage.item.bbox_results;

                        list.forEach(item => {
                            const x = item.bbox[0],
                                  y = item.bbox[1],
                                  w = Math.abs(item.bbox[2] - item.bbox[0]),
                                  h = Math.abs(item.bbox[3] - item.bbox[1]);

                            methods.drawRect(x*scaleX, y*scaleY, w, h, vData.graphColor, 0, scaleX, scaleY);
                            methods.labelNode(item);
                        });
                        
                    }
                },
                createStage() {
                    vData.currentRect = null;
                    vData.currentText = null;
                    vData.stage = new Konva.Stage({
                        container: 'container',
                        width:     vData.width,
                        height:    vData.height,
                    });
                    vData.stage.container().style.cursor = props.forJobType === 'detection' ? 'crosshair' : 'default';
                    vData.layer = new Konva.Layer();
                    vData.stage.add(vData.layer);
                    const imgObj = new Image();

                    let imgOptions = {}, labelScaleX = 1, labelScaleY = 1;

                    imgObj.onload = () => {
                        const imgW= imgObj.width, imgH = imgObj.height;

                        imgOptions = {
                            x:      0,
                            y:      0,
                            image:  imgObj,
                            width:  vData.width > imgW ? imgW : vData.width,
                            height: vData.height > imgH ? imgH : vData.height,
                        };
                        
                        if (imgW > vData.width && imgH > vData.height) {
                            labelScaleX = vData.width / imgW;
                            labelScaleY = vData.height / imgH;
                        } else if (imgW > vData.width && imgH < vData.height) {
                            labelScaleX = vData.width / imgW;
                            labelScaleY = 1;
                        } else if (imgW < vData.width && imgH > vData.height) {
                            labelScaleY = vData.height / imgH;
                            vData.labelScaleY = vData.height / imgH;
                            labelScaleX = 1;
                        } else {
                            labelScaleX = 1;
                            labelScaleY = 1;
                        }
                        vData.labelScaleX = labelScaleX;
                        vData.labelScaleY = labelScaleY;
                    };
                    setTimeout(() => {
                        imageLayer = new Konva.Image(imgOptions);
                        vData.layer.add(imageLayer);
                        vData.layer.batchDraw();
                        if (props.forJobType === 'detection') methods.editLabelStage(labelScaleX, labelScaleY);
                    }, 100);
                    if(props.currentImage.item) imgObj.src = props.currentImage.item.img_src;
                    vData.imgLoading = !(props.currentImage.item && props.currentImage.item.img_src);
                },
                stageMousedown(e) {
                    const x = e.evt.offsetX, y = e.evt.offsetY;

                    vData.pointStart = [x, y];
                    methods.drawRect(x, y, 0, 0, vData.graphColor, 0);
                    vData.drawing = true;
                },
                drawRect(x, y, w, h, c, sw, scaleX=1, scaleY=1) {
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
                        draggable:   false,
                        scaleX,
                        scaleY,
                    });
                    vData.graphNow = vData.rectLayer;
                    vData.layer.add(vData.rectLayer);
                    vData.layer.draw();
                    vData.labelNowPos = vData.rectLayer;
                },
                createTransformer(e) {
                    vData.trLayer = new Konva.Transformer({
                        borderStroke:       '#fff',
                        keepRatio:          false, // 不等比缩放
                        anchorCornerRadius: 10,
                        anchorSize:         10,
                        rotateEnabled:      false, // 是否可调节框选区域角度
                        resizeEnabled:      true, // 是否可调节框选区域大小
                        anchorFill:         '#fff',
                    });
                    vData.layer.add(vData.trLayer);
                    vData.trLayer.nodes([e.target]);
                    vData.layer.draw();
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
                    labelModalRef.value.methods.showModal();
                },
                // emit 删除标注框
                destroyNode() {
                    if (vData.currentRect && vData.currentRect.attrs.isLabeled) {
                        vData.stage.find('Text').forEach(item => {
                            if (item.attrs.traceId === vData.currentRect.attrs.traceId) {
                                vData.stage.find('Transformer').destroy();
                                item.destroy();
                                vData.currentRect.destroy();
                            }
                        });
                    } else {
                        vData.stage.find('Transformer').destroy();
                        if (vData.currentRect) {
                            vData.currentRect.destroy();
                        } else {
                            vData.rectLayer.destroy();
                        }
                    }
                    vData.layer.draw();
                    labelModalRef.value.methods.hideModal();
                    vData.labelList = props.labelList;
                },
                // 标注
                labelNode(data) {
                    vData.labelLayer = new Konva.Text({
                        x:         vData.labelNowPos.x() + vData.labelNowPos.width()*vData.labelNowPos.scaleX()/2,
                        y:         vData.labelNowPos.y() + vData.labelNowPos.height()*vData.labelNowPos.scaleY()/2 - 18/2,
                        text:      data.category_name,
                        fontSize:  18,
                        fill:      'rgba(255, 255, 255, .7)',
                        draggable: false,
                    });
                    vData.labelLayer.offsetX(vData.labelLayer.width() / 2);
                    vData.layer.add(vData.labelLayer);
                    vData.layer.draw();
                },
                setLabelTextPosition() {
                    // 需考虑用户画标注框的初始方向
                    vData.currentText.setAttrs({
                        x: vData.currentRect.x() + vData.currentRect.width() * vData.currentRect.scaleX() / 2,
                        y: vData.currentRect.y() + vData.currentRect.height() * vData.currentRect.scaleY() / 2 - 18/2,
                    });
                },
                keyCodeSearch(val) {
                    vData.labelList = props.labelList.filter(function(item) {
                        return Object.keys(item).some(function(key) {
                            return (
                                String(item[key]).toLowerCase().indexOf(val) > -1
                            );
                        });
                    });
                },
                // save current label
                saveLabel() {
                    const labe_list = [], rect_list = vData.stage.find('Rect');

                    rect_list.forEach(item => {
                        if (item.attrs.isLabeled) {
                            const sx = item.attrs.scaleX === 1 ? (item.attrs.x + item.attrs.width)/vData.labelScaleX : item.attrs.x/vData.labelScaleX + item.attrs.width,
                                  sy = item.attrs.scaleY === 1 ? (item.attrs.y + item.attrs.height)/vData.labelScaleY : item.attrs.y/vData.labelScaleY + item.attrs.height;

                            labe_list.push({
                                label:  item.attrs.labelName,
                                points: [
                                    { x: item.attrs.x/vData.labelScaleX, y: item.attrs.y/vData.labelScaleY },
                                    { x: sx, y: sy },
                                ],
                            });
                        }
                    });
                    context.emit('save-label', labe_list, props.currentImage.item.id);
                    vData.stage.find('Transformer').destroy();
                    vData.stage.find('Rect').destroy();
                    vData.stage.find('Text').destroy();
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
    background: #f0f0f0;
    position: relative;
    .container {
        canvas {
            background: #f0f0f0!important;
        }
    }
}
</style>
