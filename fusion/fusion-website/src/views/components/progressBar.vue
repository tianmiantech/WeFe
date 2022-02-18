<template>
    <el-dialog
        :title="title"
        :visible.sync="progressDialog"
        :close-on-click-modal="false"
        :show-close="false"
        class="uploading-dialog"
        width="30%"
        top="25vh"
    >
        <el-progress
            type="circle"
            :percentage="processData.percentage || 0"
            :color="colorsMethods"
        />
    </el-dialog>
</template>

<script>
export default {
    name:  'ProgressBar',
    props: {
        processData: Object,
    },
    data() {
        return {
            progressDialog: false,
            title:          '正在存储...',
        };
    },
    watch: {
        progressDialog(val) {
            this.title = this.processData.text;
        },
    },
    methods: {
        colorsMethods(percentage) {
            if (percentage < 30) {
                return '#909399';
            } else if (percentage < 60) {
                return '#e6a23c';
            } else if (percentage < 90) {
                return '#1989fa';
            } else {
                return '#67c23a';
            }
        },
        showDialog() {
            this.progressDialog = true;
        },
        hideDialog() {
            this.progressDialog = false;
        },
    },
};
</script>

<style lang="scss">
.uploading-dialog {
    .el-dialog__header {
        background: #f5f7fa;
    }
    .el-dialog__title {
        font-size: 14px;
    }
    .el-dialog__body {
        text-align: center;
    }
}

</style>
