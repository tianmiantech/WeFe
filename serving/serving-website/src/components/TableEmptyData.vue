<template>
    <div
        slot="empty"
        class="empty-data"
    >
        <img
            class="empty-data-img"
            src="../assets/images/empty_data.png"
        >
        <slot v-if="message">
            <p class="empty-data-message">{{ message }}</p>
        </slot>
        <p
            v-else
            class="empty-data-message"
        >
            哦豁，啥也没有~
        </p>
    </div>
</template>
<script>
export default {
    name:  'TableEmptyData',
     props: {
        message: String,
    },
};
</script>

