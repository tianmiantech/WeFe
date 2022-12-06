<template>
    <div class="page">
        <el-row :gutter="20">
            <el-col :span="12">
                <ServiceStatus />
            </el-col>
            <el-col :span="12">
                <MessageList />
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import MessageList from './components/message-list.vue';
import ServiceStatus from './components/service-available-list.vue';
</script>

<style lang="scss" scoped>
    .board-row{height: calc(100vh - 120px);}
</style>
