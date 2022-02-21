<template>
    <div class="page">
        <div class="empty-data">
            <img
                class="empty-data-img"
                src="@assets/images/empty_data.png"
            >
            <p class="empty-data-message">哦豁，啥也没有~</p>
            <router-link
                class="mt10"
                :to="{ name: 'index' }"
            >
                返回首页
            </router-link>
        </div>
    </div>
</template>

<style lang="scss" scoped>
    .empty-data{
        width: 400px;
        height: 400px;
        padding: 50px;
        margin: 0 auto;
        text-align: center;
    }
    .empty-data-message{
        margin-top: 30px;
    }
</style>
