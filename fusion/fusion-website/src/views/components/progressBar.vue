<template>
    <el-dialog
        title="正在存储数据集..."
        :visible.sync="progressDialog"
        width="30%"
        :close-on-click-modal="false"
        :show-close="false"
        class="uploading-dialog"
        top="25vh"
    >
        <el-progress
            type="circle"
            :percentage="processData.percentage"
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
        };
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
