<template>
    <el-breadcrumb
        v-if="menus.length"
        separator="/"
    >
        <template v-for="(item, index) in menus">
            <el-breadcrumb-item
                v-if="index + 1 !== menus.length"
                :key="index"
            >
                <i
                    v-if="item.meta.icon"
                    :class="item.meta.icon"
                />
                {{ item.meta.title }}
            </el-breadcrumb-item>
            <el-breadcrumb-item
                v-else
                :key="index"
            >
                <i
                    v-if="item.meta.icon"
                    :class="item.meta.icon"
                />
                {{ item.meta.title }}
            </el-breadcrumb-item>
        </template>
    </el-breadcrumb>
</template>

<script>
    import {
        reactive,
        onBeforeMount,
        watch,
    } from 'vue';
    import { useRoute } from 'vue-router';

    export default {
        setup() {
            let menus = reactive([]);
            const route = useRoute();

            onBeforeMount(() => {
                menus = route.matched;
            });

            watch(
                () => route.path,
                () => {
                    menus = route.matched;
                },
            );

            return {
                menus,
            };
        },
    };
</script>

<style lang="scss">
    .el-breadcrumb {
        padding-bottom: 10px;
        .el-breadcrumb__inner {font-weight: bold;}
    }
</style>
