<template>
    <div class="label_system">
        <div id="container" ref="container" class="container" :style="{width: vData.width+'px'}" />
        <div class="loading_layer" :style="{display: vData.imgLoading ? 'block' : 'none', width: vData.width + 'px'}"><el-icon class="el-icon-loading"><elicon-loading /></el-icon></div>
        <label-modal ref="labelModalRef" :labelList="vData.labelList" :labelPosition="vData.labelPosition" @destroy-node="methods.destroyNode" @label-node="methods.labelNode" @key-code-search=methods.keyCodeSearch />
        <div class="show_label_info">
            <h3 v-if="forJobType === 'classify'">标注结果</h3>
            <div v-if="currentImage.item && currentImage.item.labeled && forJobType === 'classify'" class="show_label_txt">{{currentImage.item.label_list}}</div>
            <div v-else-if="currentImage.item && !currentImage.item.labeled && forJobType === 'classify'" class="show_label_txt">请在右侧选择标签</div>
        </div>
        <el-button type="primary" class="save_label" @click="methods.saveLabel">
            保存当前标注
        </el-button>
    </div>
</template>

<script>
    import Konva from 'konva';
    import { ref, reactive } from 'vue';
    import { useRouter } from 'vue-router';
    import LabelModal from './label-modal.vue';
    export default {
        components: {
            LabelModal,
        },
        props: {
            currentImage: Object,
            labelList:    Array,
            forJobType:   String,
        },
        setup(props, context) {
            const router = useRouter();
            const vData = reactive({
                stage:          null, // 导致无法在图片以外的区域画框 Proxy
                layer:          null,
                graphNow:       null,
                trLayer:        null,
                labelLayer:     null,
                groupLayer:     null,
                rectLayer:      null,
                width:          800,
                height:         528,
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
            });

            let imageLayer = null;
            const labelModalRef = ref();
            const methods = {
                editLabelStage(scaleX, scaleY) {
                    if (props.currentImage.item) {
                        if (props.currentImage.item.label_info.labeled) {
                            const list = props.currentImage.item.label_info.objects;

                            list.forEach(item => {
                                const x = item.points[0].x,
                                      y = item.points[0].y,
                                      w = item.points[1].x - item.points[0].x,
                                      h = item.points[1].y - item.points[0].y;

                                methods.drawRect(x*scaleX, y*scaleY, w, h, vData.graphColor, 0, scaleX, scaleY);
                                methods.labelNode(item);
                            });
                        }
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
                            vData.labelScale.x = vData.width / imgW;
                            vData.labelScale.y = vData.height / imgH;
                        } else if (imgW > vData.width && imgH < vData.height) {
                            labelScaleX = vData.width / imgW;
                            vData.labelScale.x = vData.width / imgW;
                        } else if (imgW < vData.width && imgH > vData.height) {
                            labelScaleY = vData.height / imgH;
                            vData.labelScale.y = vData.height / imgH;
                        }
                    };
                    setTimeout(() => {
                        imageLayer = new Konva.Image(imgOptions);
                        vData.layer.add(imageLayer);
                        vData.layer.batchDraw();
                        if (props.forJobType === 'detection') methods.editLabelStage(labelScaleX, labelScaleY);
                    }, 100);
                    if(props.currentImage.item) imgObj.src = props.currentImage.item.img_src;
                    vData.imgLoading = !(props.currentImage.item && props.currentImage.item.img_src);

                    if (props.forJobType === 'detection') {
                        vData.stage.on('mousedown', function(e) {
                            // labelModalRef.value.methods.hideModal();
                            vData.labelList = props.labelList;
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
                    /* 后续再加
	                    vData.stage.on('wheel', function(e) {
	                        const bgx = imageLayer.x(), bgy = imageLayer.y();
	                        const scaleBy = 1.2;
	                        // 这里用鼠标位置减去图片的位置，然后除以缩放的比例
	                        const mousePointTo = {
	                            x: (e.evt.x - bgx) / vData.imgScaleConfig.scaleX,
	                            y: (e.evt.y - bgy) / vData.imgScaleConfig.scaleY,
	                        };

	                        if (e.evt.deltaY > 0) {
	                            // 缩小
	                            vData.imgScaleConfig.scaleX = vData.imgScaleConfig.scaleX / scaleBy;
	                            vData.imgScaleConfig.scaleY = vData.imgScaleConfig.scaleY / scaleBy;
	                            if (vData.imgScaleConfig.scaleX < 1) vData.imgScaleConfig.scaleX = 1;
	                            if (vData.imgScaleConfig.scaleY < 1) vData.imgScaleConfig.scaleY = 1;
	                            vData.stage.container().style.cursor = 'move';
	                        } else {
	                            // 放大
	                            vData.imgScaleConfig.scaleX = vData.imgScaleConfig.scaleX * scaleBy;
	                            vData.imgScaleConfig.scaleY = vData.imgScaleConfig.scaleY * scaleBy;
	                            if (vData.imgScaleConfig.scaleX > 1) {
	                                imageLayer.setAttrs({
	                                    draggable: true,
	                                });
	                                vData.stage.container().style.cursor = 'move';
	                            }
	                            if (vData.imgScaleConfig.scaleX > 1.6) vData.imgScaleConfig.scaleX = 1.6;
	                            if (vData.imgScaleConfig.scaleY > 1.6) vData.imgScaleConfig.scaleY = 1.6;
	                        }

	                        // vData.imgScaleConfig.x = e.evt.x - mousePointTo.x * vData.imgScaleConfig.scaleX;
	                        // vData.imgScaleConfig.y = e.evt.y - mousePointTo.y * vData.imgScaleConfig.scaleY;
	                        imageLayer.setAttrs({
	                            x:      vData.imgScaleConfig.x,
	                            y:      vData.imgScaleConfig.y,
	                            scaleX: vData.imgScaleConfig.scaleX,
	                            scaleY: vData.imgScaleConfig.scaleY,
	                        });
	                        vData.layer.draw();
	                    });
	                    vData.stage.on('click', function() {
	                        vData.stage.container().style.cursor = 'crosshair';
	                        imageLayer.setAttrs({
	                            draggable: false,
	                        });
	                    });
                    */
                    }
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
                        draggable:   true,
                        scaleX,
                        scaleY,
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
                        if (e.target.attrs.isLabeled) {
                            vData.stage.find('Text').forEach(item => {
                                if (item.attrs.traceId === this.attrs.traceId) {
                                    vData.currentRect = this;
                                    vData.currentText = item;
                                }
                            });
                        }
                        if (e.evt.button === 2) {
                            e.evt.preventDefault();
                            e.evt.returnValue = false;
                            e.evt.cancelBubble = false;
                            // right click to edit labeled element
                            if (this.attrs.isLabeled) {
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
                        vData.labelList = props.labelList;
                    });
                    vData.rectLayer.on('transform', function(e) {
                        if (e.target.attrs.isLabeled) {
                            vData.currentRect.setAttrs({
                                width:  vData.rectLayer.width() * vData.rectLayer.scaleX(),
                                height: vData.rectLayer.height() * vData.rectLayer.scaleY(),
                            });
                            methods.setLabelTextPosition();
                        } else {
                            vData.labelNowPos.setAttrs({
                                x:      vData.rectLayer.x(),
                                y:      vData.rectLayer.y(),
                                width:  vData.rectLayer.width() * vData.rectLayer.scaleX(),
                                height: vData.rectLayer.height() * vData.rectLayer.scaleY(),
                            });
                        }
                        // reset zoom ratio after each zoom.
                        vData.rectLayer.setAttrs({
                            scaleX: 1,
                            scaleY: 1,
                        });
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
                        if (e.target.attrs.isLabeled) {
                            vData.stage.find('Text').forEach(item => {
                                if (item.attrs.traceId === this.attrs.traceId) {
                                    vData.stage.find('Transformer').destroy();
                                    item.destroy();
                                    this.destroy();
                                }
                            });
                        } else {
                            vData.stage.find('Transformer').destroy();
                            this.destroy();
                        }
                        vData.currentText = null;
                        vData.layer.draw();
                        labelModalRef.value.methods.hideModal();
                        vData.labelList = props.labelList;
                        vData.stage.container().style.cursor = 'crosshair';
                    });
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
                    if (e.target.attrs.isLabeled && e.evt.button !== 2) {
                        labelModalRef.value.methods.hideModal();
                        vData.labelList = props.labelList;
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
                    if (vData.currentText) {
                        vData.currentText.setAttrs({
                            text: data.label,
                        });
                        vData.currentRect.setAttrs({
                            labelName: data.label,
                        });
                        vData.layer.draw();
                        labelModalRef.value.methods.hideModal();
                        vData.currentText = null;
                    } else {
                        vData.labelLayer = new Konva.Text({
                            x:        vData.labelNowPos.x() + vData.labelNowPos.width()*vData.labelNowPos.scaleX()/2,
                            y:        vData.labelNowPos.y() + vData.labelNowPos.height()*vData.labelNowPos.scaleY()/2 - 18/2,
                            text:     data.label,
                            fontSize: 18,
                            fill:     'rgba(255, 255, 255, .7)',
                        });
                        vData.labelLayer.offsetX(vData.labelLayer.width() / 2);
                        vData.layer.add(vData.labelLayer);
                        vData.layer.draw();
                        // vData.currentLabel = {}; // 同一张图中有多个标注时，清除上个标注信息
                        vData.labelLayer.on('mouseenter', function() {
                            vData.stage.container().style.cursor = 'move';
                        });
                        labelModalRef.value.methods.hideModal();
                        vData.labelList = props.labelList;
                        const traceId = Math.random();

                        vData.rectLayer.setAttrs({
                            isLabeled: true,
                            labelName: data.label,
                            traceId,
                        });
                        vData.labelLayer.setAttrs({
                            traceId,
                        });
                    }
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
                            labe_list.push({
                                label:  item.attrs.labelName,
                                points: [
                                    { x: item.attrs.x, y: item.attrs.y },
                                    { x: item.attrs.x + item.attrs.width, y: item.attrs.y + item.attrs.height },
                                ],
                            });
                        }
                    });
                    context.emit('save-label', labe_list, props.currentImage.item.id);
                    vData.stage.find('Transformer').destroy();
                    vData.stage.find('Rect').destroy();
                    vData.stage.find('Text').destroy();
                },
                // set keycode
                async handleEvent(e) {
                    console.log(e.keyCode);
                    switch (e.keyCode) {
                    case 48:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 49:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 50:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 51:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 52:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 53:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 54:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 55:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 56:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 57:
                        if(vData.stage.find('Rect').length) methods.labelNode(vData.labelList[Number(e.key)]);
                        break;
                    case 83:
                        e.preventDefault();
                        e.returnValue = false; // 阻止直接保存网页
                        methods.saveLabel();
                        if (e.ctrlKey && e.code === 'KeyS') return false;
                        break;
                    case 90:
                        router.push({ name: 'project-detail' });
                        if (router.name === 'index') return;
                        if (e.ctrlKey && e.code === 'KeyZ') {
                            router.go(-1);
                        }
                        break;
                    }
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
    .loading_layer {
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, .85);
        position: absolute;
        top: 0;
        z-index: 3;
        i {
            display: block;
            font-size: 28px;
            color: #438bff;
            position: absolute;
            left: 50%;
            top: 50%;
            transform: translate(-50%);
            z-index: 5;
        }
    }
    .save_label {
        position: fixed;
        right: 330px;
        top: 185px;
    }
    .show_label_info {
        position: fixed;
        right: 330px;
        top: 245px;
        h3 {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 22px;
        }
        .show_label_txt {
            font-size: 14px;
            color: #666;
        }
    }
}
</style>
