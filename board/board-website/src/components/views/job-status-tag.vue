<template>
    <el-tag
        size="small"
        :type="style.type"
        :title="style.title"
        :effect="style.effect"
    >
        {{ content }}
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
                content: '',
            };
        },
        created() {
            const map = {
                created: {
                    type:   '',
                    title:  '已创建',
                    effect: 'plain',
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
                wait_stop: {
                    type:   '',
                    title:  '等待结束',
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
                success: {
                    type:   'success',
                    title:  '成功（正常结束）',
                    effect: 'dark',
                },
            };

            this.content = map[this.status] ? map[this.status].title : this.status;
            if (this.status) {
                this.style = map[this.status];
            }
        },
    };
</script>
