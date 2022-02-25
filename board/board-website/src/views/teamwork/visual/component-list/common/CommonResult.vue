<template>
    <el-form class="flex-form">
        <el-button
            v-if="showHistory"
            class="history-btn"
            type="text"
            size="small"
            @click="checkHistory"
        >执行历史</el-button>

        <el-form-item label="运行状态：">
            {{ jobStatus[result.task.status] }}
        </el-form-item>
        <el-form-item
            v-if="result.task.start_time"
            label="启动时间："
        >
            {{ dateFormat(result.task.start_time) }}
        </el-form-item>
        <el-form-item
            v-if="result.task.finish_time"
            label="结束时间："
        >
            {{ dateFormat(result.task.finish_time) }}
        </el-form-item>
        <el-form-item
            v-if="result.task.finish_time > result.task.start_time"
            label="运行时长："
        >
            {{ timeFormat((result.task.finish_time - result.task.start_time) / 1000) }}
        </el-form-item>
        <el-form-item
            v-if="result.task.message"
            label="任务信息："
        >
            <p :style="result.task.status === 'success' ? '' : 'color:#F85564;font-weight: bold;'">{{ result.task.message }}</p>
        </el-form-item>
        <el-form-item
            v-if="result.task.error_cause"
            label="详细原因："
        >
            <p style="color:#F85564;word-break: break-all;">{{ result.task.error_cause }}</p>
        </el-form-item>
        <el-form-item v-if="showHistory" label="任务执行顺序：">
            {{ result.task.position }}
        </el-form-item>
    </el-form>
</template>

<script>
    import { getCurrentInstance } from 'vue';

    export default {
        props: {
            autoReadResult: Boolean,
            result:         Object,
            currentObj:     Object,
            jobDetail:      Object,
            showHistory:    {
                type:    Boolean,
                default: true,
            },
        },
        emits: ['show-node-history'],
        setup(props) {
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;
            const jobStatus = {
                created:          '已创建',
                wait_run:         '等待运行',
                running:          '运行中',
                stop:             '人为结束',
                wait_stop:        '等待结束',
                stop_on_running:  '人为关闭',
                error_on_running: '程序异常关闭',
                error:            '执行失败',
                success:          '成功(正常结束)',
            };

            const checkHistory = () => {
                $bus.$emit('show-node-history', props.currentObj.nodeId);
            };

            return {
                jobStatus,
                checkHistory,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .history-btn{
        float: right;
    }
    .flex-form{
        .el-form-item{margin-bottom:5px;}
        :deep(.el-form-item__label),
        :deep(.el-form-item__content){line-height: 20px;}
    }
</style>
