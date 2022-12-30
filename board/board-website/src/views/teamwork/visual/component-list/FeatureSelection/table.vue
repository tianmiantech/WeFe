<template>
    <el-table :data="tableData" :height="props.height" :max-height="props.maxHeight">
        <slot></slot>
    </el-table>

    <el-pagination
        :small="props.small"
        :total="vData.total"
        :page-sizes="[10, 20, 30, 40, 50]"
        :page-size="vData.pageSize"
        :current-page="vData.page"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="currentPageChange"
        @size-change="pageSizeChange"
    />
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue';
const props = defineProps({
    data: Array,
    small: Boolean,
    height: String,
    maxHeight: String || Number,
});

const vData = reactive({
    page: 1,
    pageSize: 10,
    total: 10,
})

const tableData = computed(() => {
    return props.data.slice((vData.page-1) *vData.pageSize, vData.page * vData.pageSize);
})

const currentPageChange = (value) => {
    vData.page = value;
}

const pageSizeChange = (value) => {
    vData.pageSize = value;
}

watch(
    () => props.data,
    ()=>{
        vData.page = 1;
        vData.pageSize = 10;
        vData.total = props.data.length;
        console.log('props.data', props.data)
    },    {
        immediate: true,
    }
)

</script>