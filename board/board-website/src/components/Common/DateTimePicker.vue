<template>
    <el-date-picker
        v-model="vData.value"
        :type="type"
        unlink-panels
        :format="format"
        range-separator="-"
        :value-format="valueFormat"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        :shortcuts="shortcuts ? vData.shortcuts : []"
        @change="timeChange"
    />
</template>

<script>
    import { reactive } from 'vue';

    export default {
        name:  'DateTimePicker',
        props: {
            type: {
                type:    String,
                default: 'daterange',
            },
            format: {
                type:    String,
                default: 'YYYY-MM-DD HH:mm:ss',
            },
            valueFormat: {
                type:    String,
                default: 'YYYY-MM-DD HH:mm:ss',
            },
            shortcuts: Boolean,
        },
        emits: ['change'],
        setup(props, context) {
            const vData = reactive({
                value:     '',
                shortcuts: [{
                    text:  '最近一周',
                    value: (() => {
                        const end = new Date();
                        const start = new Date();

                        start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                        return [start, end];
                    })(),
                }, {
                    text:  '最近一个月',
                    value: (() => {
                        const end = new Date();
                        const start = new Date();

                        start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
                        return [start, end];
                    })(),
                }, {
                    text:  '最近三个月',
                    value: (() => {
                        const end = new Date();
                        const start = new Date();

                        start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
                        return [start, end];
                    })(),
                }],
            });
            const timeChange = (value) => {
                context.emit('change', value);
            };

            return {
                vData,
                timeChange,
            };
        },
    };
</script>
