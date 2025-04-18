<template>
    <div
        v-loading="loading"
        style="min-height: 200px;"
    >
        <c-grid
            v-if="!loading"
            :theme="gridTheme"
            :data="table_data.rows"
            :frozen-col-count="1"
            font="12px sans-serif"
            :style="{height:`${gridHeight}px`}"
            ref="cgrid"
        >
            <c-grid-column
                v-for="(item, index) in table_data.header"
                :key="index"
                :field="item"
                min-width="100"
                :width="item === table_data.header[0] ? 240 : 'auto'"
                :column-style="{textOverflow: 'ellipsis'}"
            >
                {{ item }}{{featureList[item] ? `(${featureList[item]})`: ''}}
            </c-grid-column>
        </c-grid>
    </div>
</template>

<script>
    import { getDataFeatureType } from '@src/service';

    export default {
        props: {
            featureType: {
                type:    Object,
                default: () => ({}),
            },
        },
        data() {
            return {
                loading:    true,
                table_data: {
                    header: [],
                    rows:   [],
                },
                gridTheme: {
                    color:       '#6C757D',
                    borderColor: '#EBEEF5',
                },
                gridHeight: 0,
                features:   {},
            };
        },
        watch: {
            featureList() {
                this.$nextTick(() => {
                    if(this.$refs['cgrid']){
                        this.$refs['cgrid'].invalidate();
                    }
                });

            },
        },
        computed: {
            featureList(){
                return JSON.stringify(this.featureType) === "{}" ? this.features : this.featureType; 
            },
        },
        methods: {
            // data_set preview
            async loadData(id) {
                this.loading = true;

                const res = await getDataFeatureType(id);
                const obj = {};

                res.forEach(item => {
                    obj[item.name] = item.data_type;
                });

                this.features = obj;

                const { code, data } = await this.$http.get({
                    url: '/storage/table_data_set/preview?id=' + id,
                });

                this.loading = false;

                if (code === 0) {
                    if(data && data.list){
                        const rows = data.list;

                        let { length } = data.list;

                        if(length >= 15) length = 15;

                        this.resize(length);

                        this.table_data.rows = rows;
                        this.table_data.header = data.header;
                    }
                }
            },
            resize(length) {
                this.gridHeight = 41 * (length + 1) + 1;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .c-grid{
        border: 1px solid #EBEEF5;
        position: relative;
        z-index: 1;
    }
</style>
