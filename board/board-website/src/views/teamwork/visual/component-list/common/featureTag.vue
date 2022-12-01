<template>
    <el-tag :color="showColor" effect="dark" size="small" v-if="type">
        {{type}}
    </el-tag>
</template>

<script setup>
    import { defineProps,computed } from 'vue';
    import { useStore } from 'vuex';

    const props = defineProps({
        data_set_id: {
            type:    String,
            default: '',
        },
        name:            String,
        /**
         * 如果上层传递下来，直接用
         */
        featureTypeList: {
            type:    Object,
            default: () => ({}),
        },
    });

    const store = useStore();

    const featureType = computed(() => store.state.base.featureType);

    const color = (type)=> {
        return {
            'Integer': '#79bbff',
            'Long':    '#67c23a',
            'Double':  '#909399', 
            'Enum':    '#e6a23c', 
            'String':  '#f56c6c',
        }[type] || ''
    };

    const type = computed(() => {
        // const data = (featureType.value.filter((item) => {
        //     return item.data_set_id === props.data_set_id;
        // }) || [])[0] || {};
        const data = props.featureTypeList[props.data_set_id] || featureType.value[props.data_set_id] || {};

        console.error('vData.featureTypeList',props.featureTypeList)
        return  data[props.name];
    });

    const showColor = computed(() => {
        return color(type.value);
    });

    
</script>
