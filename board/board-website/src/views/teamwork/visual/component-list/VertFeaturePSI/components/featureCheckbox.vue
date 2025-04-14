<template>
    <el-form
        v-loading="data.columnListLoading"
        element-loading-text="当前特征列较多需要时间处理, 请耐心等待"
        class="flex-form"
        @submit.prevent
    >
        <el-form-item label="快速选择：">
            <el-input
                v-model="data.quickSelect"
                placeholder="输入特征名称, 多个特征名称用,分开"
                clearable
            >
                <template #append>
                    <el-button @click="check">
                        确定
                    </el-button>
                </template>
            </el-input>
        </el-form-item>
        <div class="mt20 mb10">
            <el-checkbox
                v-model="data.checkAll"
                :indeterminate="data.isIndeterminate"
                @change="handleCheckAllChange"
            >
                全选
            </el-checkbox>
            <el-button
                type="primary"
                size="small"
                class="ml10 revert-check-btn"
                @click="revertCheck()"
            >
                反选
            </el-button>
            <span class="ml15">({{ data.selected.length }} / {{ allFeature.length }})</span>
        </div>

        <el-form-item>
            <el-checkbox-group v-model="data.selected" @change="handleCheckedChange">
                <el-checkbox v-for="item in allFeature" :key="item" :label="item">
                    <FeatureTagVue :name="item" :data_set_id="data_set_id" />
                    <span>{{item}}</span>
                </el-checkbox>
            </el-checkbox-group>
        </el-form-item>
    </el-form>
</template>

<script setup>
    import { reactive, computed,watch,toRaw ,getCurrentInstance } from 'vue';
    import { replace } from '../../common/utils';
    import FeatureTagVue from '../../common/featureTag.vue'
    const instance = getCurrentInstance();
    const { $message } = instance.appContext.config.globalProperties;
    // eslint-disable-next-line no-undef
    const props = defineProps({
        /** 默认选中 */
        defaultSelected: {
            type:    Array,
            default: () => [],
        },
        allFeature: {
            type:    Array,
            default: () => [],
        },
        data_set_id: {
            type:   String,
            default: ''
        }
    });
    
    const data = reactive({
        quickSelect:       '',
        selected:          [...props.defaultSelected],
        checkAll:          false,
        isIndeterminate:   false,
        columnListLoading: false,
    });

    // 用于告诉父组件
    const selected = computed(() => data.selected);

    // eslint-disable-next-line no-undef
    watch(
        () => props.defaultSelected,
        (current, prev) => {
            /* ... */
            data.selected = current;
        },
    );

    const check = () => {
        const array = replace(toRaw(data.quickSelect)).replace(/，/, ',').split(',');
        const notFind = [];
        const find = [];

        for(const item of array){
            if(Array.isArray(props.allFeature) && props.allFeature.includes(item)){
                find.push(item);
            } else {
                notFind.push(item);
            }
        }
        data.selected = [... new Set([...toRaw(data.selected),...find] )];


        if(notFind.length){
            $message.warning(`找不到特征[${[...new Set(notFind)].join(',')}]`);
        }

        
    };

    const handleCheckAllChange = (val) => {
        data.selected = val ? props.allFeature : [];
        data.isIndeterminate = false;
    };
    const handleCheckedChange = (value) => {
        const checkedCount = value.length;

        data.checkAll = checkedCount === props.allFeature.length;
        data.isIndeterminate = checkedCount > 0 && checkedCount < props.allFeature.length;
    };
    const revertCheck = () => {
        data.selected = props.allFeature.filter(item => {
            return !data.selected.includes(item);
        });
    };

    // eslint-disable-next-line no-undef
    defineExpose({
        selected,
    });

</script>
