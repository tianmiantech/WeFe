<template>
    <div class="project-setting-icon">
        <el-icon
            class="el-icon-setting f20 ml10"
            @click="showProjectSetting"
        >
            <elicon-setting />
        </el-icon>

        <el-dialog
            v-model="showDialogForProjectSetting"
            destroy-on-close
            title="项目设置"
            width="400px"
        >
            <el-form class="flex-form" @submit.prevent :disabled="!form.is_project_admin">
                <el-form-item
                    label="项目名称："
                    label-width="100px"
                    class="is-required"
                >
                    <el-input
                        v-model="project.name"
                        maxlength="40"
                        show-word-limit
                    />
                </el-form-item>
                <el-form-item
                    label="项目描述："
                    label-width="100px"
                    class="is-required"
                >
                    <el-input
                        v-model="project.desc"
                        type="textarea"
                        rows="4"
                    />
                </el-form-item>
                <div v-if="form.is_project_admin" class="text-r">
                    <el-button
                        type="primary"
                        @click="saveBaseInfo"
                    >
                        保存
                    </el-button>
                </div>
            </el-form>

            <template v-if="form.is_project_admin">
                <h3>关闭项目</h3>
                <div class="ml10">
                    <p class="mt10 mb10 color-danger">
                        项目关闭后不可恢复! 请谨慎操作!
                    </p>
                    <el-button
                        type="danger"
                        @click="closeProject"
                    >
                        确认关闭
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        inject: ['refresh'],
        props:  {
            form: Object,
        },
        data() {
            return {
                showDialogForProjectSetting: false,
                project:                     {
                    name: '',
                    desc: '',
                },
            };
        },
        methods: {
            showProjectSetting() {
                this.showDialogForProjectSetting = true;
                this.project.name = this.form.name;
                this.project.desc = this.form.desc;
            },
            // update project base info
            async saveBaseInfo($event) {
                if(this.project.name === '') {
                    return this.$message.error('项目名称不能为空');
                }

                const { code } = await this.$http.post({
                    url:  '/project/update',
                    data: {
                        projectId: this.form.project_id,
                        name:      this.project.name,
                        desc:      this.project.desc,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.refresh();
                    this.$message.success('保存成功!');
                }
            },

            closeProject() {
                this.$confirm('项目关闭后不可恢复!', '警告', {
                    type: 'warning',
                }).then(async () => {
                    const { code } = await this.$http.post({
                        url:  '/project/close',
                        data: {
                            projectId: this.form.project_id,
                        },
                    });

                    if(code === 0) {
                        this.refresh();
                        this.editSetting = false;
                        this.$message.success('保存成功!');
                    }
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{
        :deep(.el-input__inner) {
            padding-right: 60px;
        }
    }
</style>
