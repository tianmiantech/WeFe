<template>
    <el-tag
        v-if="role === 'promoter'"
        class="job_member_role_tag"
        type="success"
        effect="dark"
        size="small"
    >
        <el-icon>
            <elicon-star />
        </el-icon>
        promoter
    </el-tag>
    <el-tag
        v-else-if="role === 'provider'"
        class="job_member_role_tag"
        effect="dark"
        size="small"
    >
        <el-icon>
            <elicon-medal />
        </el-icon>
        provider
    </el-tag>
    <el-tag
        v-else
        class="job_member_role_tag"
        effect="dark"
        size="small"
    >
        <el-icon>
            <elicon-connection />
        </el-icon>
        arbiter
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
.board-tag.job_member_role_tag {
    width: 80px;
    display: inline-block;
    text-align: center;
}
</style>
