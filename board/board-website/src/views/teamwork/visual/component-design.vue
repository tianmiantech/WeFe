<template>
    <div class="component-box">
        <Binning />
    </div>
</template>

<script>
    import Binning from './component-list/Binning/param';

    export default {
        components: {
            Binning,
        },
        data() {
            return {
                draggable: false,
                unfold:    true,
            };
        },
        methods: {
            mousedown(e, item) {
                if(item.isFolder) {
                    this.unfold = !this.unfold;
                } else {
                    this.draggable = true;
                }
            },
            mouseup() {
                this.draggable = false;
            },
            dragstart(event, item) {
                this.$emit('ready-to-drag');

                /* set dataTransfer */
                event.dataTransfer.setData(
                    'dragComponent',
                    JSON.stringify({
                        label: item.name,
                        id:    item.id,
                        data:  {
                            componentType: item.id,
                            ...item.$cfg,
                        },
                    }),
                );
            },
            dragend() {
                this.draggable = false;
                this.$emit('drag-to-end');
            },
            readyToDrag() {
                this.$emit('ready-to-drag');
            },
        },
    };
</script>

<style lang="scss" scoped>
    .component-box{
        width:500px;
        padding:8px;
        border: 1px solid #333;
    }
</style>
