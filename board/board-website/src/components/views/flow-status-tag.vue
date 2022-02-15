<template>
    <el-tag
        size="small"
        :type="style.type"
        :title="style.title"
        :effect="style.effect"
        :disable-transitions="disableTransitions"
    >
        {{ content }}
    </el-tag>
</template>

<script>
    export default {
        props: {
            status:             String,
            disableTransitions: Boolean,
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
                editing: {
                    type:   '',
                    title:  'editing',
                    effect: 'light',
                },
                running: {
                    type:   'info',
                    title:  'running',
                    effect: 'dark',
                },
                wait_stop: {
                    type:   'warning',
                    title:  'wait_stop',
                    effect: 'dark',
                },
                wait_run: {
                    type:   'warning',
                    title:  'wait_run',
                    effect: 'dark',
                },
                wait_success: {
                    type:   'success',
                    title:  'wait_success',
                    effect: 'dark',
                },
                finished: {
                    type:   'success',
                    title:  'finished',
                    effect: 'dark',
                },
                success: {
                    type:   'success',
                    title:  'success',
                    effect: 'dark',
                },
                stop_on_running: {
                    type:   'warning',
                    title:  'stop_on_running',
                    effect: 'dark',
                },
                error_on_running: {
                    type:   'danger',
                    title:  'error_on_running',
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
