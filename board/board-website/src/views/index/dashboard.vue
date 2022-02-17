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

<script>
    import MessageList from './components/message-list.vue';
    import ServiceStatus from './components/service-available-list.vue';

    export default {
        components: {
            MessageList,
            ServiceStatus,
        },
    };
</script>
