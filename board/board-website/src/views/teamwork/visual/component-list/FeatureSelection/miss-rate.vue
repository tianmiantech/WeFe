<template>
    <el-dialog
        width="600px"
        v-model="vData.showDialog"
        :close-on-click-modal="false"
        title="特征筛选 - 缺失率"
        destroy-on-close
        append-to-body
    >
        <el-form
            inline
            style="margin-top: -20px;"
            @submit.prevent
        >
            <el-form-item>
                <label class="el-form-item__label">缺失率: (<i class="iconfont icon-more-than" />)</label>
                <el-input
                    v-model="vData.params.miss_rate"
                    style="width:100px;"
                />
                %
            </el-form-item>
            <el-button
                class="ml10 mr10"
                @click="methods.filter"
            >
                筛选
            </el-button>
            筛选结果 ({{ vData.list.length }}/{{ vData.total || 0 }})
        </el-form>

        <el-table
            :data="vData.list"
            max-height="600px"
            border
            stripe
        >
            <el-table-column
                prop="name"
                label="特征"
                width="100"
            />
            <el-table-column
                prop="member_name"
                label="所属成员"
                width="140"
            />
            <el-table-column
                prop="miss_rate"
                label="缺失率"
            />
        </el-table>

        <div class="text-r mt20">
            <el-button
                type="primary"
                :disabled="vData.list.length === 0"
                @click="methods.confirm"
            >
                确定
            </el-button>
        </div>
    </el-dialog>
</template>

<script>
    import { reactive } from 'vue';
    import featureSelection from './feature-selection-mixin';

    export default {
        props: {
            ...featureSelection.props,
        },
        emits: [...featureSelection.emits],
        setup (props, context) {
            let vData = reactive({
                params: {
                    select_type: 'miss_rate',
                    miss_rate:   30,
                },
            });

            let methods = {};

            const { $data, $methods } = featureSelection.mixin({
                vData,
                methods,
                props,
                context,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
            };
        },
    };
</script>
