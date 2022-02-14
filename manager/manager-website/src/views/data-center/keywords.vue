<template>
    <el-card
        class="page"
        shadow="never"
    >
        <div class="mb20">
            <el-button
                class="mb10"
                type="primary"
                native-type="submit"
                @click="methods.addKeywords"
            >
                新增关键词
            </el-button>
        </div>

        <el-table
            v-loading="vData.loading"
            style="width: 400px;"
            :data="vData.list"
            stripe
            border
        >
            <el-table-column label="关键词" prop="tag_name" width="100" />
            <el-table-column label="操作">
                <template v-slot="scope">
                    <el-button
                        type="primary"
                        @click="methods.updateTag($event, scope.row)"
                    >
                        编辑
                    </el-button>
                    <el-button
                        type="danger"
                        @click="methods.deleteTag($event, scope.row)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog
            title="编辑关键词"
            v-model="vData.dialogKeywords"
            custom-class="card-dialog"
            destroy-on-close
            width="400px"
            top="30vh"
        >
            <el-form class="flex-form" @submit.prevent>
                <el-form-item label="标签名称" style="width:330px;" required>
                    <el-input v-model.trim="vData.tagName"></el-input>
                </el-form-item>
                <el-button
                    class="mt10"
                    type="primary"
                    :disabled="!vData.tagName"
                    style="margin-left: 80px;width:100px;"
                    @click="methods.submitKeywords"
                >
                    提交
                </el-button>
            </el-form>
        </el-dialog>
    </el-card>
</template>

<script>
    import {
        reactive,
        onMounted,
        nextTick,
        getCurrentInstance,
    } from 'vue';
    import table from '@src/mixins/table.js';

    export default {
        inject: ['refresh'],
        mixins: [table],
        setup() {
            const { ctx, appContext } = getCurrentInstance();
            const { $http, $confirm } = appContext.config.globalProperties;
            const vData = reactive({
                loading:    true,
                getListApi: '/default_tag/query',
                search:     {
                    name: '',
                },
                list:           [],
                dialogKeywords: false,
                tagName:        '',
                tagId:          '',
            });

            const methods = {
                addKeywords() {
                    vData.dialogKeywords = true;
                },
                async submitKeywords($event) {
                    const params = {
                        tagName: vData.tagName,
                    };

                    if(vData.tagId) {
                        params.tagId = vData.tagId;
                    }

                    const { code } = await $http.post({
                        url:      vData.tagId ? '/default_tag/update' : '/default_tag/add',
                        data:     params,
                        btnState: {
                            target: $event,
                        },
                    });

                    nextTick(() => {
                        if(code === 0) {
                            vData.dialogKeywords = false;
                            vData.tagName = '';
                            vData.tagId = '';
                            ctx.refresh();
                        }
                    });
                },
                async updateTag($event, tag) {
                    vData.dialogKeywords = true;
                    vData.tagName = tag.tag_name;
                    vData.tagId = tag.id;
                },
                async deleteTag($event, tag) {
                    $confirm('确定要删除该标签吗?', '警告', {
                        type:              'warning',
                        cancelButtonText:  '取消',
                        confirmButtonText: '确定',
                    })
                        .then(async _ => {
                            const { code } = await $http.post({
                                url:  '/default_tag/delete',
                                data: {
                                    tagId: tag.id,
                                },
                            });

                            nextTick(() => {
                                if(code === 0) {
                                    vData.loading = true;
                                    setTimeout(() => {
                                        ctx.refresh();
                                        vData.loading = false;
                                    }, 300);
                                }
                            });
                        });
                },
            };

            onMounted(async () => {
                await ctx.getList();
                vData.loading = false;
            });

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .tag-list{
        li{
            float:left;
            width: 20%;
            min-width: 100px;
            margin:20px 20px 0 0;
        }
    }
    .el-tag{cursor: pointer;}
</style>
