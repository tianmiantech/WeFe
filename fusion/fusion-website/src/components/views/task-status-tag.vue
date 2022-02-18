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

        this.content = map[this.status] ? map[this.status].title : this.status;
        if (this.status) {
            this.style = map[this.status];
        }
    },
};
</script>
