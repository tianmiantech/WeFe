<template>
    <div class="label-modal" :style="{ transform: `translate(${labelPosition})`, display: vData.isShowLabelModal}">
        <div class="label-modal-title">
            <span>请选择标签</span>
            <span class="close-span" @click="methods.hideModal">x</span>
        </div>
        <div class="label-modal-input">
            <el-input v-model="vData.labelSearchText" placeholder="搜索关键词" @input="methods.inputKeycode" />
        </div>
        <div class="label-modal-list">
            <div v-for="item in labelList" :key="item.text" class="label-modal-item" @click="methods.labelEvent(item)">
                <span class="label-modal-item_text">{{item.label}}</span>
                <span v-if="item.keycode !== ''" class="label-modal-item_keycode">{{item.keycode}}</span>
            </div>
        </div>
        <div class="label-modal-delete">
            <el-button plain size="mini" @click="methods.deleteCalloutBox">删除标注框</el-button>
        </div>
    </div>
</template>

<script>
    import { reactive } from 'vue';
    export default {
        props: {
            labelList:     Array,
            labelPosition: String,
        },
        setup(props, context) {
            const vData = reactive({
                labelSearchText:  '',
                isShowLabelModal: 'none',
            });

            const methods = {
                showModal() {
                    vData.isShowLabelModal = 'block';
                },
                hideModal() {
                    vData.isShowLabelModal = 'none';
                    vData.labelSearchText = '';
                },
                deleteCalloutBox() {
                    context.emit('destroy-node');
                },
                labelEvent(item) {
                    context.emit('label-node', item);
                },
                inputKeycode(val) {
                    context.emit('key-code-search', val);
                },
            };

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss">
@mixin flexBox {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
$font14: 14px;
$paddinglr: 0 12px;
$height32: 32px;

.label-modal {
    width: 186px;
    max-height: 325px;
    border: 1px solid #f6f6f6;
    position: absolute;
    left: 0;
    top: 0;
    background: #fff;
    border: 1 px solid #eee;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    z-index: 200;
    .label-modal-title {
        height: 40px;
        font-size: $font14;
        padding: $paddinglr;
        background: #fafafa;
        border: 1px solid #f6f6f6;
        @include flexBox;
        .close-span {
            cursor: pointer;
        }
    }
    .label-modal-input {
        .el-input__inner {
            font-size: $font14;
            height: $height32 !important;
            box-shadow: inset 0 -1px 0 0 #eee;
            border: none;
            padding: $paddinglr;
            border-radius: unset;
        }
    }
    .label-modal-list {
        margin: 4px 0;
        max-height: 192px;
        overflow-y: auto;
        .label-modal-item {
            cursor: pointer;
            height: 32px;
            line-height: 32px;
            padding: 0 12px;
            display: flex;
            .label-modal-item_text {
                flex-grow: 1;
                text-overflow: ellipsis;
                overflow: hidden;
                white-space: nowrap;
                text-align: left;
            }
            .label-modal-item_keycode {
                flex-shrink: 0;
                width: 18px;
                height: 18px;
                font-size: 12px;
                color: #999;
                letter-spacing: 0;
                text-align: center;
                line-height: 16px;
                font-weight: 500;
                border: 1px solid #ddd;
                align-self: center;
                border-radius: 2px;
                margin-left: 12px;
            }
        }
    }
    .label-modal-delete {
        height: 40px;
        line-height: 40px;
        box-shadow: 0 -2px 10px 0 #f5f5f5;
        text-align: center;
    }
}
</style>
