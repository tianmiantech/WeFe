<template>
    <el-dialog
        :visible.sync="show"
        class="dataset-dialog"
        title="请选择数据集"
    >
        <el-form
            inline
            class="mb10"
            style="margin: -20px 0 -10px;"
            @submit.native.prevent
        >
            <el-form-item
                label="名称:"
                label-width="60px"
            >
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="ID:"
                label-width="60px"
            >
                <el-input
                    v-model="search.id"
                    clearable
                />
            </el-form-item>
            <el-button
                class="mb20"
                type="primary"
                @click="loadDataList()"
            >
                查询
            </el-button>
        </el-form>

        <DataSetList
            ref="raw"
            :search-field="search"
            @close-dialog="closeDialog"
            @selectDataSet="selectDataSet"
        />
    </el-dialog>
</template>

<script>
import DataSetList from './data-set-list.vue';

export default {
    components: {
        DataSetList,
    },
    props: {
        callbackFunc: {
            type:    Function,
            default: null,
        },
    },
    data() {
        return {
            show:   false,
            search: {
                id:   '',
                name: '',
            },
            // hideRelateSourceTab: false,
        };
    },
    watch: {
        show: {
            handler(val) {
                if (val) {
                    this.resetSearch();
                }
            },
        },
    },

    methods: {
        closeDialog() {
            this.show = false;
        },
        resetSearch() {
            this.$nextTick(() => {
                const ref = this.$refs['raw'];

                this.search = {
                    id:   '',
                    name: '',
                };

                ref.list = [];
                ref.pagination.page_index = 1;
                ref.pagination.page_size = 20;
            });
        },

        async loadDataList() {
            this.$nextTick(() => {
                const ref = this.$refs['raw'];

                ref.getDataList();
            });
        },

        selectDataSet(item) {
            this.$emit('selectDataSet', item);
        },
        selectDataSets(data) {
            this.$emit('selectDataSets', data);
        },
    },
};
</script>

<style lang="scss" scoped>
.dataset-dialog {
    ::v-deep .el-dialog {
        width: 70%;
        min-width: 800px;
    }
}
</style>
