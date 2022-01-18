<template>
    <el-container>
        <el-main>
            <router-view
                :key="$route.name"
                v-slot="{ Component }"
            >
                <template v-if="Component">
                    <Suspense>
                        <template #default>
                            <component :is="Component" />
                        </template>
                        <template #fallback>
                            Loading ...
                        </template>
                    </Suspense>
                </template>
            </router-view>
        </el-main>
    </el-container>
</template>

<script>
    export default {
        setup() {

            return{

            };
        },
    };
</script>
