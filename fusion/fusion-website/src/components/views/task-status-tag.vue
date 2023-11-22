<template>
    <el-tag
        size="mini"
        :type="style.type"
        :title="style.title"
        :effect="style.effect"
    >
        {{ content }}
    </el-tag>
</template>

<script>
    const map = {
        Pending: {
            type:   '',
            title:  '待我方审核',
            effect: 'plain',
        },
        Await: {
            type:   '',
            title:  '等待合作方审核',
            effect: 'light',
        },
        Ready: {
            type:   '',
            title:  '等待合作方审核',
            effect: 'light',
        },
        Running: {
            type:   '',
            title:  '对齐中',
            effect: 'dark',
        },
        Interrupt: {
            type:   'warning',
            title:  '任务中断',
            effect: 'light',
        },
        Failure: {
            type:   'danger',
            title:  '任务异常关闭',
            effect: 'dark',
        },
        Success: {
            type:   'success',
            title:  '成功',
            effect: 'dark',
        },
    };

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
        watch: {
            status: {
                handler(val) {
                    const result = map[this.status];

                    console.log(val);
                    this.style.type = result.type;
                    this.style.title = result.title;
                    this.style.effect = result.effect;
                    this.content = result.title;
                },
            },
        },
        created() {

            this.content = map[this.status] ? map[this.status].title : this.status;
            if (this.status) {
                this.style = map[this.status];
            }
        },
    };
</script>
