<template>
    <div class="project-member-setting-btn">
        <el-tooltip placement="top">
            <template #content>
                移除当前成员
            </template>
            <el-icon
                class="el-icon-delete color-danger f16"
                @click="showDialogForMemberSetting=true"
            >
                <elicon-delete />
            </el-icon>
        </el-tooltip>

        <el-dialog
            :title="member.member_name"
            v-model="showDialogForMemberSetting"
            destroy-on-close
            append-to-body
            width="450px"
        >
            <h3>移除成员</h3>
            <p class="mt10 mb10 color-danger">注意：成员被移除后其数据集将变为不可用</p>
            <el-button
                type="danger"
                @click="removeMember"
            >
                移除
            </el-button>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        inject: ['refresh'],
        props:  {
            form:   Object,
            member: Object,
        },
        data() {
            return {
                showDialogForMemberSetting: false,
            };
        },
        methods: {
            async removeMember() {
                const vm = this;

                vm.$confirm('确定要删除该协作方吗?', '警告', {
                    type: 'warning',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await vm.$http.post({
                                url:  '/project/member/remove',
                                data: {
                                    project_id:  vm.form.project_id,
                                    member_id:   vm.member.member_id,
                                    member_role: vm.member.member_role,
                                },
                            });

                            if(code === 0) {
                                vm.refresh();
                                vm.$message.success('成员已移除!');
                            }
                        }
                    });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .project-member-setting-btn{
        position: absolute;
        right: 0;
        top:0;
        cursor: pointer;
    }
</style>
