<template>
    <div
        v-if="member.member_hidden || !member.member_allow_public_data_set"
        class="public-tips"
    >
        <p
            v-if="member.member_hidden"
            class="tips"
        >
            您当前在联邦中为 <strong>隐身状态</strong>，所以数据集不会被其他成员看到。
        </p>
        <p
            v-if="!member.member_allow_public_data_set"
            class="tips"
        >
            您在全局设置中 <strong>禁止了对外公开数据集基础信息</strong>，所以数据集不会被其他成员看到。
        </p>
    </div>
</template>

<script>
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        props:  {
            tableLoading: Boolean,
            sourceType:   String,
            searchField:  {
                type:    Object,
                default: _ => {},
            },
        },
        data() {
            return {
                member: {
                    member_allow_public_data_set: true,
                    member_hidden:                false,
                },
            };
        },
        async created() {
            await this.getMemberInfo();
        },
        methods: {
            async getMemberInfo(){
                const { code, data } = await this.$http.get({
                    url: '/member/detail',
                });

                if (code === 0) {
                    this.member = data;
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .public-tips{
        color: #ff5757;
        font-size: 12px;
        position: absolute;
        left: 30px;
        bottom: 20px;

        .tips{
            padding: 5px 0 0 0;

            strong{
                text-decoration: underline;
            }
        }
    }

</style>
