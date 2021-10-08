<template>
    <el-button
        type="primary"
        :size="size"
        @click="downloadLog"
    >
        下载日志
    </el-button>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        props: {
            jobId: String,
            size:  {
                type:    String,
                default: '',
            },
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            async downloadLog(){
                const api = `${window.api.baseUrl}/job/log/download?job_id=${this.jobId}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = api;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
            },
        },
    };
</script>
