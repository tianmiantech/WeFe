<template>
    <div class="empty-data">
        <slot name="default">
            <img
                class="empty-data-img"
                src="../../assets/images/empty_data.png"
            >
            <p class="empty-data-message mt20">哦豁，啥也没有~</p>
        </slot>
    </div>
</template>

<script>
    export default {
        name: 'EmptyData',
    };
</script>

<style lang="scss" scoped>
    .empty-data {
        padding: 50px 0;
        text-align: center;
    }
    .empty-data-img {max-height: 180px;}
    .empty-data-message {
        font-size: 20px;
        font-weight: bold;
    }
</style>
