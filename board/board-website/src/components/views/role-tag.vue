<template>
    <el-tag
        v-if="role === 'promoter'"
        class="job_member_role_tag"
        type="success"
        effect="dark"
        size="mini"
    >
        <i class="el-icon-star-off" /> promoter
    </el-tag>
    <el-tag
        v-else-if="role === 'provider'"
        class="job_member_role_tag"
        effect="dark"
        size="mini"
    >
        <i class="el-icon-medal" /> provider
    </el-tag>
    <el-tag
        v-else
        class="job_member_role_tag"
        effect="dark"
        size="mini"
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
    width: 80px;
    display: inline-block;
    text-align: center;
}
</style>
