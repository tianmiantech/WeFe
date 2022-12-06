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
    import { downLoadFileTool } from '@src/utils/tools'; 

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
                downLoadFileTool('/job/log/download', {
                    job_id: this.jobId,
                });
            },
        },
    };
</script>
