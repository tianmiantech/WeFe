<template>
    <el-tag
        v-if="role === 'promoter'"
        size="mini"
        class="job_member_role_tag"
        type="success"
        effect="dark"
    >
        <i class="el-icon-star-off" /> promoter
    </el-tag>
    <el-tag
        v-else-if="role === 'provider'"
        size="mini"
        class="job_member_role_tag"
        effect="dark"
    >
        <i class="el-icon-medal" /> provider
    </el-tag>
    <el-tag
        v-else
        size="mini"
        class="job_member_role_tag"
        effect="dark"
    >
        <i class="el-icon-connection" /> arbiter
    </el-tag>
</template>

<script>
export default {
    props: {
        role: String,
    },
};
</script>
<style lang="scss">
.el-tag.job_member_role_tag {
    display: inline-block;
    width: 80px;
    text-align: center;
}
</style>
