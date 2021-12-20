<template>
    <el-tree
        v-loading="vData.loading"
        accordion
        :indent="0"
        :lazy="true"
        :data="vData.members"
        :load="loadNode"
        :props="vData.treeProps"
        :highlight-current="true"
        class="user-list pt10"
    >
        <template v-slot="{ node, data }">
            <div
                class="tree-node"
                @click="chat(node, data)"
            >
                <div class="tree-node-label f14">{{ node.label }}</div>
                <el-icon
                    v-if="node.level > 1 && data.id !== userInfo.id"
                    class="el-icon-chat-round"
                >
                    <elicon-chat-round />
                </el-icon>
            </div>
        </template>
    </el-tree>
</template>

<script>
    import {
        computed,
        getCurrentInstance,
        onBeforeUnmount,
        onBeforeMount,
        reactive,
    } from 'vue';
    import { useStore } from 'vuex';

    export default {
        emits: ['start-chat', 'check-account'],
        setup(props, context) {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const { appContext } = getCurrentInstance();
            const { $bus, $http } = appContext.config.globalProperties;
            const vData = reactive({
                members:   [],
                loading:   false,
                treeProps: {
                    label:    'label',
                    children: 'children',
                    isLeaf:   'isLeaf',
                },
            });
            const loadNode = async (node, resolve) => {
                if(node.level === 1 && node.childNodes.length === 0) {
                    const { code, data } = await $http.get({
                        url:    '/account/query_by_member_id',
                        params: {
                            memberId:  node.data.id,
                            page_size: 200, // default number 200
                        },
                    });

                    if(code === 0) {
                        resolve(
                            data.list.map(row => {
                                return {
                                    ...row,
                                    label:           row.nickname,
                                    member_email:    node.data.email,
                                    member_mobile:   node.data.mobile,
                                    to_account_name: row.nickname,
                                    to_member_id:    node.data.id,
                                    to_member_name:  node.data.name,
                                    to_account_id:   row.id,
                                    isLeaf:          true,
                                };
                            }),
                        );
                    } else {
                        resolve([]);
                    }
                } else {
                    resolve([]);
                }
            };
            const getContacts = async () => {
                vData.loading = true;
                const { code, data } = await $http.post({
                    url:  '/union/member/query',
                    data: {
                        page_size:          100,
                        requestFromRefresh: true,
                    },
                });

                vData.loading = false;
                if(code === 0) {
                    vData.members = data.list.map(member => {
                        return {
                            ...member,
                            label: member.name,
                        };
                    });
                }
            };
            const chat = (node, data) => {
                if(node.level > 1 && data.id !== userInfo.value.id) {
                    context.emit('start-chat', node.data);
                }
            };

            onBeforeMount(() => {
                getContacts();
                $bus.$on('loginAndRefresh', () => {
                    getContacts();
                });
            });

            onBeforeUnmount(() => {
                $bus.$off('loginAndRefresh');
            });

            return {
                vData,
                chat,
                userInfo,
                getContacts,
                loadNode,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .user-list{
        height: 100%;
        overflow: auto;
    }
    .tree-node{
        width: 100%;
        display: flex;
        padding-right: 20px;
    }
    .tree-node-label{
        flex: 1;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        max-width: 140px;
    }
    .el-icon-chat-round{
        cursor: pointer;
        &:hover{color:$color-link-base-hover;}
    }
</style>
