<template>

    <span v-if="member.exited" class="member-exited">{{ member.member_name }}</span>

    <template v-else>
        {{ member.member_name }}
        <span
            v-if="member.audit_status === 'auditing'"
            class="f12 color-danger"
        >
            (非正式成员)
        </span>
        <span
            v-if="member.audit_status === 'disagree'"
            class="f12 color-danger"
        >
            ({{ member.audit_status_from_myself === 'disagree' ? '已' : '被' }}拒绝)
        </span>

        <MemberServiceStatus :status="member.$serviceStatus" :onlyIcon="true" />
    </template>

</template>

<script>
    import MemberServiceStatus from './member-service-status';

    export default{
        props: {
            form:   Object,
            member: Object,
        },
        components: {
            MemberServiceStatus,
        },
    };
</script>
