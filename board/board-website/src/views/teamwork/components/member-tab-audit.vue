<template>
    <p class="f12" style="color: #999;">
        点击刷新服务状态:
        <MemberServiceStatus :status="member.$serviceStatus" />
    </p>

    <p
        v-if="member.$error"
        class="service-offline f12 mb10"
    >
        {{ member.$error }}
    </p>

    <div v-if="member.exited">
        <span class="f13">
            <span class="member-exited">{{ member.member_name }}</span>
            已退出该项目
        </span>
        <span v-if="member.audit_comment" class="f12 color-danger"> ({{member.audit_name}}意见为: {{ member.audit_comment }})</span>
    </div>
    <p
        v-else-if="member.audit_status === 'auditing'"
        class="color-danger mb10 f14"
    >
        非正式成员, 待{{ member.audit_status_from_myself !== 'agree' && member.audit_status_from_others === 'auditing' ? '他人' : '' }}审核
    </p>
</template>

<script>
    import MemberServiceStatus from './member-service-status';

    export default{
        props: {
            member:      Object,
            memberIndex: Number,
        },
        components: {
            MemberServiceStatus,
        },
    };
</script>

<style lang="scss" scoped>
    .service-offline{
        color: $--color-danger;
        cursor: pointer;
    }
</style>
