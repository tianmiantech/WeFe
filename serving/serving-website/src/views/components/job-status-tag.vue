<template>
    <el-tag
        size="mini"
        :type="style.type"
        :title="style.title"
        :effect="style.effect"
    >
        {{ status }}
    </el-tag>
</template>

<script>
export default {
    props: {
        status: String,
    },
    data() {
        return {
            style: {
                type:   '',
                title:  '',
                effect: 'light',
            },
        };
    },
    created() {
        const map = {
            created: {
                type:   '',
                title:  '已创建',
                effect: 'plain',
            },
            auditing: {
                type:   '',
                title:  '审核中',
                effect: 'light',
            },
            audited: {
                type:   '',
                title:  '审核完成',
                effect: 'light',
            },
            splitting: {
                type:   '',
                title:  '数据切割中',
                effect: 'light',
            },
            building_task: {
                type:   '',
                title:  '生成task中',
                effect: 'light',
            },
            wait_run: {
                type:   '',
                title:  '等待运行',
                effect: 'light',
            },
            running: {
                type:   '',
                title:  '运行中',
                effect: 'dark',
            },
            stop_on_running: {
                type:   'warning',
                title:  '人为关闭（结束任务）',
                effect: 'light',
            },
            error_on_running: {
                type:   'danger',
                title:  '任务异常关闭',
                effect: 'dark',
            },
            reject_on_auditing: {
                type:   'warning',
                title:  '审批未通过关闭',
                effect: 'light',
            },
            success: {
                type:   'success',
                title:  '成功（正常结束）',
                effect: 'dark',
            },
            deleted: {
                type:   'info',
                title:  '已删除',
                effect: 'dark',
            },
            timeout: {
                type:   'danger',
                title:  '任务超时',
                effect: 'dark',
            },
        };

        if (this.status) {
            this.style = map[this.status];
        }
    },
};
</script>
