<template>
    <div class="project-setting-icon">
        <el-icon
            v-if="!project.is_exited && project.audit_status !== 'disagree'"
            class="el-icon-setting"
            @click="showDialogForProjectSetting=true"
        >
            <elicon-setting />
        </el-icon>
        <el-dialog
            title="项目设置"
            v-model="showDialogForProjectSetting"
            destroy-on-close
            width="400px"
        >
            <h3>退出此项目</h3>
            <p
                class="pt10 pb10"
                style="color: #ea5169;"
            >
                注意：退出后，您已授权的数据资源会收回授权。
            </p>
            <el-button
                type="danger"
                @click="exitProject"
            >
                退出此项目
            </el-button>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        inject: ['refresh'],
        props:  {
            project: Object,
        },
        data() {
            return {
                showDialogForProjectSetting: false,
            };
        },
        methods: {
            async exitProject() {
                const vm = this;

                vm.$confirm('确定要退出此次项目合作吗', '警告', {
                    type:        'warning',
                    customClass: 'audit_dialog',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await vm.$http.post({
                                url:  '/project/exit',
                                data: {
                                    project_id: vm.project.project_id,
                                },
                            });

                            if(code === 0) {
                                vm.refresh();
                                vm.$message.success('已退出项目!');
                            }
                        }
                    });
            },
        },
    };
</script>
