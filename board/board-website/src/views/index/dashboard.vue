<template>
    <div class="page">
        <!-- <el-button @click="clickHandle">111</el-button> -->
        <el-row :gutter="20">
            <el-col :span="12">
                <ServiceStatus />
            </el-col>
            <el-col :span="12">
                <MessageList />
            </el-col>
        </el-row>
    </div>
    <Tezhen ref="TezhenRef" />
</template>

<script setup>
import { ref } from 'vue';
import MessageList from './components/message-list.vue';
import ServiceStatus from './components/service-available-list.vue';
import Tezhen from '../../components/tezhenshaixuan.vue';

const TezhenRef = ref();
const clickHandle = () => {
    TezhenRef.value.open = true;
};
</script>

<style lang="scss" scoped>
.el-row {
    height: calc(100vh - 120px);
}
</style>
