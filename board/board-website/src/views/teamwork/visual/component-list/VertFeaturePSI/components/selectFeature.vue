<template>
    <el-form
        ref="form"
        v-loading="data.loading"
        @submit.prevent
        style="margin-top: 10px;"
    >
        <div>{{title}}：</div>
        <div>
            <el-space>
                <span style="color: #909399;font-size: 14px;">{{featureData.member_name}}</span>
                <el-button
                    style="margin-top:2px;"
                    size="small"
                    :disabled="NumDisabled || disabled"
                    @click="showColumnListDialog(item, index)"
                >
                    选择特征（{{ featureData.selectedFeature.length }}/{{ featureData.featureNames.length }}）
                </el-button>
            </el-space>
            <div
                v-if="featureData.selectedFeature"
                class="board-tag-list mb10"
            >
                <template
                    v-for="(item, index) in featureData.selectedFeature"
                    :key="index"
                >
                    <el-tag
                        v-if="index < 20"
                        :label="item"
                        :value="item"
                    >
                        {{ item }}
                    </el-tag>
                </template>
                <el-button
                    v-if="featureData.selectedFeature.length > 20"
                    size="small"
                    type="primary"
                    class="check-features"
                    @click="checkFeatures(featureData.selectedFeature)"
                >
                    查看更多
                </el-button>
            </div>
        </div>

        <el-dialog 
            v-model="data.show"
            width="70%"
            title="选择特征列"
            :close-on-click-modal="false"
            custom-class="large-width"
            destroy-on-close
            append-to-body

        >
            <featureCheckbox :defaultSelected="featureData.selectedFeature" :data_set_id="featureData.data_set_id" :allFeature="featureData.featureNames" ref="featureCheckboxRef"></featureCheckbox>
            <div class="text-r mt10">
                <el-button @click="hide">
                    取消
                </el-button>
                <el-button
                    type="primary"
                    @click="confirmCheck"
                >
                    确定
                </el-button>
            </div>
        </el-dialog>
    </el-form>
</template>

<script setup>
    import featureCheckbox from './featureCheckbox.vue';
    import { reactive,ref,getCurrentInstance, computed } from 'vue';
    const { appContext } = getCurrentInstance();
    const { $alert } = appContext.config.globalProperties;

    // eslint-disable-next-line no-undef
    const props = defineProps({
        name:        String,
        title:       String,
        flowId:      String,
        flowNodeId:  String,
        jobId:       String,
        disabled:    Boolean,
        featureData: {
            type:    Object,
            default: () =>({
                selectedFeature: [],
                member_name:     '',
                featureNames:    [],
                member_id:       '',
                member_role:     '',
                data_set_id:     '',
            }),
        },
    });

    // eslint-disable-next-line no-undef
    const emit = defineEmits(['selectFeature']);

    const featureCheckboxRef = ref();

    const NumDisabled = computed(() => !props.featureData.featureNames || !props.featureData.featureNames.length );
    const data = reactive({
        show:    false,
        loading: false,
    });

    const hide = () => {
        data.show = false;
    };

    const confirmCheck = () => {
        emit('selectFeature', featureCheckboxRef.value.selected);
        data.show = false;
    };

    const checkFeatures = (arr) => {
        $alert('已选特征:', {
            title:                    '已选特征:',
            message:                  `<div style="max-height: 80vh;overflow:auto;">${arr.join(',')}</div>`,
            dangerouslyUseHTMLString: true,
        });
    };

    const showColumnListDialog = () => {
        data.show = true;
    };

</script>
