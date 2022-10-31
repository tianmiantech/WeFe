<template>
    <ul class="sub-menu-list">
        <template v-for="(item, index) in menus">
            <template v-if="!item.meta.asmenu && item.children && !item.meta.hidden">
                <el-sub-menu
                    :key="item.name"
                    :index="`${item.path}`"
                    popper-class="sidebar-menu-popover"
                >
                    <template #title>
                        <el-icon
                            v-if="item.meta.icon"
                            class="icon"
                        >
                            <component :is="`elicon-${item.meta.icon}`" />
                        </el-icon>
                        <span>{{ item.meta.title }}
                            <el-tooltip
                                v-if="!isCollapsed && item.meta.globalTooltip"
                                placement="top-start"
                                effect="light"
                            >
                                <template #content>
                                    <p class="color-danger" v-html="item.meta.globalTooltip"></p>
                                </template>
                                <el-icon class="ml5">
                                    <elicon-info-filled />
                                </el-icon>
                            </el-tooltip>
                        </span>
                    </template>
                    <el-menu-item-group>
                        <menu-temp :menus="item.children" :is-collapsed="isCollapsed" />
                    </el-menu-item-group>
                </el-sub-menu>
            </template>

            <template v-else-if="item.meta.asmenu && !item.meta.hidden && item.children">
                <el-menu-item
                    :key="index"
                    :index="item.children[0].path"
                >
                    <el-icon class="icon">
                        <component :is="`elicon-${item.children[0].meta.icon}`" />
                    </el-icon>
                    <template #title>
                        <span>{{ item.children[0].meta.title }}</span>
                    </template>
                </el-menu-item>
            </template>

            <template v-else-if="showMenuItem(item)">
                <el-menu-item
                    :key="index"
                    :index="item.path"
                >
                    <el-icon v-if="item.meta.icon" class="icon">
                        <component :is="`elicon-${item.meta.icon}`" />
                    </el-icon>
                    <template #title>
                        <span class="pl10">
                            {{ item.meta.title }}
                            <el-tooltip
                                v-if="isCollapsed && item.meta.globalTooltip"
                                placement="top-start"
                                effect="light"
                            >
                                <template #content>
                                    <p class="color-danger" v-html="item.meta.globalTooltip"></p>
                                </template>
                                <el-icon class="ml5">
                                    <elicon-info-filled />
                                </el-icon>
                            </el-tooltip>
                        </span>
                    </template>
                </el-menu-item>
            </template>
        </template>
    </ul>
</template>

<script>
    import { computed } from 'vue';
    import { useStore } from 'vuex';

    export default {
        name:  'MenuTemp',
        props: {
            menus: {
                type:    Array,
                default: () => [],
            },
            isCollapsed: Boolean,
        },
        setup() {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);

            return {
                userInfo,
            };
        },
        methods: {
            showMenuItem(item){
                if(item.meta.hidden){
                    return false;
                }

                if(item.meta.normalUserCanSee !== false){
                    return true;
                }

                // 仅超级管理员可见
                if(item.meta.onlySuperAdmin && this.userInfo.super_admin_role){
                    return true;
                }

                // 仅管理员可见
                if(!item.meta.normalUserCanSee && this.userInfo.admin_role){
                    return true;
                }

                return false;
            },
        },
    };
</script>
