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
                                v-if="item.meta.tooltip"
                                placement="top-start"
                                effect="light"
                            >
                                <template #content>
                                    <p class="color-danger" v-html="item.meta.tooltip"></p>
                                </template>
                                <el-icon class="ml5">
                                    <elicon-info-filled />
                                </el-icon>
                            </el-tooltip>
                        </span>
                    </template>
                    <el-menu-item-group>
                        <menu-temp :menus="item.children" />
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

            <template v-else-if="!item.meta.hidden && (userInfo.admin_role || (!userInfo.admin_role && item.meta.normalUserCanSee !== false))">
                <el-menu-item
                    :key="index"
                    :index="item.path"
                >
                    <el-icon v-if="item.meta.icon" class="icon">
                        <component :is="`elicon-${item.meta.icon}`" />
                    </el-icon>
                    <template #title>
                        <span class="pl10">{{ item.meta.title }}</span>
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
        },
        setup() {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);

            return {
                userInfo,
            };
        },
    };
</script>
