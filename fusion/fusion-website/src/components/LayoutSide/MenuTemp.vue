<template>
    <ul class="sub-menu-list">
        <template v-for="(item, index) in menus">
            <template v-if="!item.meta.asmenu && item.children && !item.meta.hidden">
                <el-submenu
                    :key="item.name"
                    :index="`${item.path}`"
                >
                    <template slot="title">
                        <i
                            v-if="item.meta.icon"
                            :class="['icon', item.meta.icon]"
                        />
                        {{ item.meta.title }}
                    </template>
                    <el-menu-item-group>
                        <menu-temp :menus="item.children" />
                    </el-menu-item-group>
                </el-submenu>
            </template>

            <template v-else-if="item.meta.asmenu && !item.meta.hidden && item.children">
                <el-menu-item
                    :key="index"
                    :index="item.children[0].path"
                >
                    <i :class="['icon', item.children[0].meta.icon]" />
                    {{ item.children[0].meta.title }}
                </el-menu-item>
            </template>

            <template v-else-if="!item.meta.hidden">
                <el-menu-item
                    :key="index"
                    :index="item.path"
                    :name="item.name"
                >
                    <i :class="['icon', item.meta.icon]" />
                    {{ item.meta.title }}
                    <i
                        v-if="item.meta.tips"
                        class="numTip"
                    >{{ item.meta.tips }}</i>
                </el-menu-item>
            </template>
        </template>
    </ul>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        name:  'MenuTemp',
        props: {
            menus: {
                type:    Array,
                default: () => [],
            },
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
    };
</script>

<style lang="scss" scoped>
.numTip {
    display: inline-block;
    margin-left: 6px;
    min-width:16px;
    height:16px;
    line-height:16px;
    text-align: center;
    border-radius: 10px;
    background:#FF5757;
    font-size:12px !important;
    padding:0 4px;
    color:#fff;
}
</style>
